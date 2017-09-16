package com.mementoapp.main.mementoapp;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import CloudVisionBackend.CloudVisionAPI;
import CloudVisionBackend.PackageManagerUtils;
import CloudVisionBackend.UICheckBoxModel;
import CloudVisionBackend.UIListViewAdapter;
import FirebaseBackend.POJO.Object_Memento;
import FirebaseBackend.POJO.Person_Kairos;
import FirebaseBackend.POJO.Person_Memento;
import KairosBackend.KairosUtils;


public class AddNewObjectActivity extends AppCompatActivity {

    private ImageView imageView;

    private Button btnEnroll = null;
    private EditText tfName = null;
    private EditText tfDescription = null;
    private TextView tfMessage = null;
    private ListView lvVisionList = null;

    //Firebase Stuff
    FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mObjectsPhotosStorageReference;

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_object);

        InitializeAPI();

        btnEnroll = (Button) findViewById(R.id.btn_AddNewObjectActivity_Enroll);
        Button btnPhoto = (Button) findViewById(R.id.btn_AddNewObjectActivity_Camera);
        this.imageView = (ImageView)this.findViewById(R.id.imageView_AddNewObjectActivity);
        tfName = (EditText) findViewById(R.id.tf_AddNewObjectActivity_Name);
        tfDescription = (EditText) findViewById(R.id.tf_AddNewObjectActivity_Description);
        tfMessage = (TextView) findViewById(R.id.textView_AddNewObjectActivity_message);
        lvVisionList = (ListView) findViewById(R.id.listView_AddNewObjectActivity);

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

        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }

        btnEnroll.setEnabled(false);


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

    private void InitializeAPI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mUserDatabaseReference = mFirebaseDatabase.getReference().child("user_"+user.getUid());
        mObjectsPhotosStorageReference = mFirebaseStorage.getReference().child("user_"+user.getUid()+"/object_images");
    }


    //code to take current image and submit it
    private void EnrollImage(){
        if(mCurrentImage == null){
            Toast.makeText(this, "You must take a photo first!", Toast.LENGTH_LONG).show();
            return;
        }

        String name = tfName.getText().toString().trim();

        if(name.length() == 0){
            Toast.makeText(this, "You must enter a name!", Toast.LENGTH_LONG).show();
            return;
        }

        String description = tfDescription.getText().toString().trim();

        if(description.length() == 0){
            Toast.makeText(this, "You must enter a description!", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<String> predictionsList = new ArrayList<>();
        for(UICheckBoxModel model: predictionsLabelsList){
            System.out.println("LIST: "+model.getName()+"\t"+model.getValue());
            if(model.getValue() == 1) {
                predictionsList.add(model.getName());
            }
        }
        if(predictionsList.size() == 0){
            Toast.makeText(this, "You must enable one tag at least!", Toast.LENGTH_LONG).show();
            return;
        }

        AddDataToFirebase_Storage(name, description, predictionsList);
    }

    private void AddDataToFirebase_Storage(final String name, final String description, final ArrayList<String> labelsList) {

        //Upload to storage
        Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
        StorageReference fileRef = mObjectsPhotosStorageReference.child(name+".png");
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
                AddDataToFirebase_Database(name, description, labelsList, downloadUrl.toString());
            }
        });

    }

    private void AddDataToFirebase_Database(final String name, String description, ArrayList<String> labelsList, String imageURL) {
        //TODO make sure to verify that the name is unique in the database

        //Upload to database - as person_memento
        Object_Memento om = new Object_Memento();
        om.setName(name);
        om.setImage_url(imageURL);
        om.setLabelsList(labelsList);
        om.setAudio_url("-101");
        om.setDescription(description);
        String key = mUserDatabaseReference.child("object_memento").push().getKey();
        mUserDatabaseReference.child("object_memento").child(key).setValue(om);

        tfMessage.setText("Successfully enrolled: "+name);
    }

    public void callCloudVision(final Bitmap bitmap) throws IOException {

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CloudVisionAPI.CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(CloudVisionAPI.ANDROID_PACKAGE_HEADER, packageName);
                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(CloudVisionAPI.ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    System.out.println("created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    DisplayCloudVisionResults(CloudVisionAPI.convertResponseToString(response));

                    return "Complete";

                } catch (GoogleJsonResponseException e) {
                    System.out.println("failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    System.out.println("failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                System.out.println("Google cloud vision: "+result);
            }
        }.execute();
    }



    UICheckBoxModel[] predictionsLabelsList;
    private void DisplayCloudVisionResults( ArrayList<String> result) {

        final Context context = this;
         predictionsLabelsList = new UICheckBoxModel[result.size()];
        int i = 0;
        for(String r: result){
            String desc = r.split("\t")[1].trim();
            UICheckBoxModel mod = new UICheckBoxModel(desc, 0);
             predictionsLabelsList[i] = mod;
            i++;
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIListViewAdapter adapter = new UIListViewAdapter(context,  predictionsLabelsList);
                lvVisionList.setAdapter(adapter);
                btnEnroll.setEnabled(true);

            }
        });


    }


    //code to take photo and display it
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private Bitmap mCurrentImage;

    private void dispatchTakePictureIntent() {

        //reset text
        tfMessage.setText("(Message will be here)");

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
                return;
            }
            try {
                callCloudVision(mCurrentImage);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
