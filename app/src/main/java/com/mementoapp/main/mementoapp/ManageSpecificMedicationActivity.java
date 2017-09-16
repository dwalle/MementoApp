package com.mementoapp.main.mementoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

import FirebaseBackend.DownloadImageFileInBackground;
import FirebaseBackend.POJO.Medication_Memento;
import KairosBackend.KairosUtils;

public class ManageSpecificMedicationActivity extends AppCompatActivity {

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

    //Data
    String passed_firebaseKey;
    String passed_MedicationName;
    boolean cameraUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_specific_medication);

        InitializeAPI();

        Bundle bundle = getIntent().getExtras();
        String selected_medication = bundle.getString("selected_medication");

        System.out.println("SELECTED MEDICATION: "+selected_medication);
        ParsePassedData(selected_medication);
        GetMementoData(passed_firebaseKey, passed_MedicationName);

        this.imageView = (ImageView) findViewById(R.id.imageView_ManageSpecficMedication);
        this.tf_medicationName = (EditText) findViewById(R.id.tf_ManageSpecficMedication_Name);
        this.tf_medicationInstructions= (EditText) findViewById(R.id.tf_ManageSpecficMedication_Instructions);
        this.textView_Message = (TextView) findViewById(R.id.textView_ManageSpecficMedication_message);

        Button btn_Camera = (Button) findViewById(R.id.btn_ManageSpecficMedication_Camera);
        Button btn_Update = (Button) findViewById(R.id.btn_ManageSpecficMedication_Update);
        Button btn_Delete = (Button) findViewById(R.id.btn_ManageSpecficMedication_Delete);

        btn_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateMedication();
            }
        });
        btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteMedication();
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

    private void ParsePassedData(String parse){

        try{
            String[] data = parse.split("\t");
            passed_firebaseKey = data[0].trim();
            passed_MedicationName = data[1].trim();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private Medication_Memento firebase_retrieved_MedicationMemento;
    private void GetMementoData(String firebaseID, String name) {
        //Make sure to reset values
        firebase_retrieved_MedicationMemento = null;

        mUserDatabaseReference.child("medication_memento").child(firebaseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> medicationMemento = (Map<String, Object>)  dataSnapshot.getValue();

                firebase_retrieved_MedicationMemento = new Medication_Memento();
                firebase_retrieved_MedicationMemento.setName((String)medicationMemento.get("name"));
                firebase_retrieved_MedicationMemento.setAudio_url((String)medicationMemento.get("audio_url"));
                firebase_retrieved_MedicationMemento.setImage_url((String)medicationMemento.get("image_url"));
                firebase_retrieved_MedicationMemento.setInstructions((String)medicationMemento.get("instructions"));

                UpdateUIWithFirebaseData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void UpdateUIWithFirebaseData(){
        if(firebase_retrieved_MedicationMemento == null){
            System.out.println("Cannot continue as medication memento does not exist");
            return;
        }

        String name = firebase_retrieved_MedicationMemento.getName();
        String instru = firebase_retrieved_MedicationMemento.getInstructions();
        if(instru == null || instru.equals("-101")){
            instru = "";
        }
        tf_medicationInstructions.setText(instru);
        tf_medicationName.setText(name);

        DisplayFullImage(firebase_retrieved_MedicationMemento.getImage_url());
    }

    private DownloadImageFileInBackground downloadedImageTask = new DownloadImageFileInBackground();
    private void DisplayFullImage(String imageURL) {
        Glide.with(this).load(imageURL.trim()).into(imageView);

        try {
            Glide.with(this)
            .load(imageURL)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            mCurrentImage = bitmap;
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
        }


        if(downloadedImageTask.downloaded){
            downloadedImageTask = new DownloadImageFileInBackground();
        }

        downloadedImageTask.act = this;
        downloadedImageTask.execute(new String[]{imageURL});

    }

    private void DeleteMedication(){
        //*******/Remove from firebase database
        DatabaseReference myRef = mUserDatabaseReference.child("medication_memento").child(passed_firebaseKey);
        myRef.removeValue();

        //*******/Remove from storage
        StorageReference mySRef = mMedicationPhotosStorageReference.child(firebase_retrieved_MedicationMemento.getName()+".png");
        mySRef.delete();

        finish();
    }

    private void UpdateMedication(){
        if(mCurrentImage == null && downloadedImageTask.imageFile == null){
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

        //TODO, instead of deleting and reuploading,
        //we can check for what changes were made
        //If just instructions were changed, we can just update that
        //Or if just the image, we can just upload the new version of that

        //*******/Remove from firebase database
        DatabaseReference myRef = mUserDatabaseReference.child("medication_memento").child(passed_firebaseKey);
        myRef.removeValue();

        //*******/Remove from storage
        StorageReference mySRef = mMedicationPhotosStorageReference.child(firebase_retrieved_MedicationMemento.getName()+".png");
        mySRef.delete();

        //Upload new versions
        AddDataToFirebase_Storage(mediName, mediInstructions);
    }

    private void AddDataToFirebase_Storage(final String medication_name, final String medication_instructions) {

        //Upload to storage
        UploadTask uploadTask = null;
        if(cameraUsed) {
            //Upload image from camera path
            Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
            StorageReference fileRef = mMedicationPhotosStorageReference.child(medication_name + ".png");
            uploadTask = fileRef.putFile(file);

        }else{//downloadedImageTask
            //Upload image from path
            Uri file = Uri.fromFile(downloadedImageTask.imageFile);
            StorageReference fileRef = mMedicationPhotosStorageReference.child(medication_name + ".png");
            uploadTask = fileRef.putFile(file);
        }
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
                System.out.println("Uploaded to firebase storage: " + downloadUrl + "\n\t\t\t" + taskSnapshot.getUploadSessionUri());
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

        textView_Message.setText("Successfully updated medication: "+medication_name);


        //Since this was updated, we want to display the new data
        String newKey = key;
        String newName = medication_name;

        ParsePassedData(key+"\t"+newName);
        GetMementoData(newKey, newName);

    }
    public byte[] getImageData(Bitmap bmp) {

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao); // bmp is bitmap from user image file
        bmp.recycle();
        byte[] byteArray = bao.toByteArray();
        byte[] toReturn = Base64.encode(byteArray, Base64.DEFAULT);
        return toReturn;
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
            cameraUsed = true;
        }
    }

    private static final int MY_CAMERA_REQUEST_CODE = 100;
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
