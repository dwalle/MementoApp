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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;

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

import FirebaseBackend.POJO.Person_Kairos;
import FirebaseBackend.POJO.Person_Memento;
import KairosBackend.KairosClient;
import KairosBackend.KairosUtils;
import KairosBackend.POJO_Kairos.Requests.Enroll.KairosRequest_Enroll;
import KairosBackend.POJO_Kairos.Responses.Enroll.KairosResponse_Enroll;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddNewKairosActivity extends AppCompatActivity {

    private ImageView imageView;
    private KairosClient kairosClient;

    //UI
    private ProgressBar progressBar = null;
    private EditText tfSubjectID = null;
    private TextView tfMessage = null;
    private LinearLayout llBackground = null;

    //Firebase Stuff
    FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPeoplePhotosStorageReference;

    private static final int MY_CAMERA_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_kairos);

        InitializeAPI();

        Button btnEnroll = (Button) findViewById(R.id.btn_ActivityAddNewKairos_Enroll);
        Button btnPhoto = (Button) findViewById(R.id.btn_ActivityAddNewKairos_Photo);
        this.imageView = (ImageView)this.findViewById(R.id.imageView_ActivityAddNewKairos);
        tfSubjectID = (EditText) findViewById(R.id.editText_ActivityAddNewKairos_PersonName);
        tfMessage = (TextView) findViewById(R.id.txtView_ActivityAddNewKairos_KairosMessage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_ActivityAddNewKairos);
        llBackground = (LinearLayout) findViewById(R.id.background_ActivityAddNewKairos);

        ShowUIProgressBar(false);

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnrollImage();
            }
        });

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
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

    private void ShowUIProgressBar(boolean show){
        if(show) {
            llBackground.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            llBackground.bringToFront();
            progressBar.bringToFront();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }else{
            llBackground.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void InitializeAPI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mUserDatabaseReference = mFirebaseDatabase.getReference().child("user_"+user.getUid());
        mPeoplePhotosStorageReference = mFirebaseStorage.getReference().child("user_"+user.getUid()+"/people_images");

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(KairosClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();

        kairosClient= retrofit.create(KairosClient.class);

    }


    //code to take current image and submit it
    private void EnrollImage(){
        if(mCurrentImage == null){
            Toast.makeText(this, "You must take a photo first!", Toast.LENGTH_LONG).show();
            return;
        }

        String subjectID = tfSubjectID.getText().toString().trim();

        if(subjectID.length() == 0){
            Toast.makeText(this, "You must enter a name!", Toast.LENGTH_LONG).show();
            return;
        }


        ShowUIProgressBar(true);

        System.out.println("-------------------------Build enroll request now!");
        KairosRequest_Enroll request = new KairosRequest_Enroll();
        request.setGalleryName(user.getUid());
        request.setSubjectId(subjectID);
        String base64 = KairosUtils.ConvertImageToBase64(mCurrentImage, Bitmap.CompressFormat.PNG);
        request.setImage(base64);

        Call<KairosResponse_Enroll> call =  kairosClient.postEnroll(request);

        /*
        RequestBody subjectIdPart = RequestBody.create(MultipartBody.FORM, subjectID);
        RequestBody galleryNamePart = RequestBody.create(MultipartBody.FORM, "TestGallery");
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", base64);

        Call<KairosResponse_Enroll> call =  kairosClient.postEnroll_Multipart(imagePart, subjectIdPart, galleryNamePart);
        */

        System.out.println("-------------------------Send enroll now!");
        call.enqueue(new Callback<KairosResponse_Enroll>() {
            @Override
            public void onResponse(Call<KairosResponse_Enroll> call, Response<KairosResponse_Enroll> response) {
                System.out.println("Response: "+response.raw().toString());

                ShowUIProgressBar(false);

                //String kairosSubjectId = response.body().getImages().get(0).getTransaction().getSubjectId();
                String kairosSubjectId = response.body().getImages().get(0).getTransaction().getSubjectId();
                String kairosSubject_FaceID = (response.body().getFaceId()).toString();
                AddDataToFirebase_Storage(kairosSubjectId, kairosSubject_FaceID);
                tfMessage.setText("Successfully enrolled: "+kairosSubjectId);
            }

            @Override
            public void onFailure(Call<KairosResponse_Enroll> call, Throwable t) {
                ShowUIProgressBar(false);
                Toast.makeText(AddNewKairosActivity.this, "FAILURE! Call: "+call.toString(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void AddDataToFirebase_Storage(final String kairosSubjectId, final String kairosSubject_faceID) {

        //Upload to storage
        Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
        StorageReference fileRef = mPeoplePhotosStorageReference.child(kairosSubjectId+"/"+kairosSubject_faceID+".png");
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
                AddDataToFirebase_Database(kairosSubjectId, kairosSubject_faceID, downloadUrl.toString());
            }
        });

    }

    private void AddDataToFirebase_Database(String kairosSubjectId, String kairosSubject_faceID, String imageURL) {
        //TODO make sure to verify that the name is unique in the database
        //Upload to database - as kairos_people
        Person_Kairos pk = new Person_Kairos();
        pk.setKairos_faceid(kairosSubject_faceID);
        pk.setImage_url(imageURL);
        String key = mUserDatabaseReference.child("kairos_people").child(kairosSubjectId).push().getKey();
        mUserDatabaseReference.child("kairos_people").child(kairosSubjectId).child(key).setValue(pk);

        //Upload to database - as person_memento
        Person_Memento pm = new Person_Memento();
        pm.setName(kairosSubjectId);
        pm.setImage_url(imageURL);
        pm.setAudio_url("-101");
        pm.setDescription("-101");
        key = mUserDatabaseReference.child("person_memento").push().getKey();
        mUserDatabaseReference.child("person_memento").child(key).setValue(pm);

    }

    //code to take photo and display it
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private Bitmap mCurrentImage;

    private void dispatchTakePictureIntent() {

        //reset text
        tfMessage.setText("(Message from Kairos API will be here)");

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
            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            //imageView.setImageBitmap(photo);
            mCurrentImage = KairosUtils.setPic(imageView, mCurrentPhotoPath);
            if(mCurrentImage == null){
                System.out.println("onActivityResult: Could not set the picture");
            }
        }
    }

}
