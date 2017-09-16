package com.mementoapp.main.mementoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import FirebaseBackend.POJO.Medication_Memento;
import KairosBackend.KairosUtils;

public class AddNewMedicationActivity extends AppCompatActivity {

    //Firebase Stuff
    FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mMedicationPhotosStorageReference;

    //UI
    ImageView imageView;
    EditText tf_medicationName;
    EditText tf_medicationInstructions;
    TextView textView_Message;


    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_medication);

        InitializeAPI();

        this.imageView = (ImageView) findViewById(R.id.imageView_AddNewMedication);
        this.tf_medicationName = (EditText) findViewById(R.id.tf_AddNewMedication_Name);
        this.tf_medicationInstructions= (EditText) findViewById(R.id.tf_AddNewMedication_Instructions);
        this.textView_Message = (TextView) findViewById(R.id.textView_AddNewMedication_message);
        Button btn_Camera = (Button) findViewById(R.id.btn_AddNewMedication_Camera);
        Button btn_Enroll = (Button) findViewById(R.id.btn_AddNewMedication_Enroll);

        btn_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        btn_Enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnrollMedication();
            }
        });

    }

    private void InitializeAPI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mUserDatabaseReference = mFirebaseDatabase.getReference().child("user_"+user.getUid());
        mMedicationPhotosStorageReference = mFirebaseStorage.getReference().child("user_"+user.getUid()+"/medication_images");

    }


    private void EnrollMedication(){
        if(mCurrentImage == null){
            Toast.makeText(this, "You must take a photo first!", Toast.LENGTH_LONG).show();
            return;
        }

        String mediName = tf_medicationName.getText().toString().trim();
        if(mediName.length() == 0){
            Toast.makeText(this, "You must enter a name!", Toast.LENGTH_LONG).show();
            return;
        }

        String mediInstructions = tf_medicationInstructions.getText().toString().trim();
        if(mediInstructions.length() == 0){
            Toast.makeText(this, "You must enter a name!", Toast.LENGTH_LONG).show();
            return;
        }else if(mediInstructions.length() > 100){
            Toast.makeText(this, "Instructions must be less than 100 characters. Current length: "+mediInstructions.length(), Toast.LENGTH_LONG).show();
            return;
        }


        AddDataToFirebase_Storage(mediName, mediInstructions);
    }

    private void AddDataToFirebase_Storage(final String medication_name, final String medication_instructions) {

        //Upload to storage
        Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
        StorageReference fileRef = mMedicationPhotosStorageReference.child(medication_name+".png");
        UploadTask uploadTask = fileRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
                e.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                System.out.println("Uploaded to firebase storage: "+downloadUrl+"\n\t\t\t"+taskSnapshot.getUploadSessionUri());
                AddDataToFirebase_Database(medication_name, medication_instructions, downloadUrl.toString());
            }
        });

    }

    private void AddDataToFirebase_Database(String medication_name, String medication_instructions, String imageURL) {
        //TODO make sure to verify that the name is unique in the database

        //Upload to database - as person_memento
        Medication_Memento mm = new Medication_Memento();
        mm.setName(medication_name);
        mm.setImage_url(imageURL);
        mm.setAudio_url("-101");
        mm.setInstructions(medication_instructions);
        String key = mUserDatabaseReference.child("medication_memento").push().getKey();
        mUserDatabaseReference.child("medication_memento").child(key).setValue(mm);

        textView_Message.setText("Successfully enrolled medication: "+medication_name);

        finish();

    }


    //code to take photo and display it
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private Bitmap mCurrentImage;

    private void dispatchTakePictureIntent() {

        //reset text
        textView_Message.setText("(Message will be here)");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = KairosUtils.createImageFile(this, "png");
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (photoFile.exists()) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.mementoapp.main.mementoapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            mCurrentImage = KairosUtils.setPic(imageView, mCurrentPhotoPath);
            if(mCurrentImage == null){
                System.out.println("onActivityResult: Could not set the picture");
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
