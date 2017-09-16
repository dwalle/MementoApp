package com.mementoapp.main.mementoapp;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import CloudVisionBackend.CloudVisionAPI;
import CloudVisionBackend.PackageManagerUtils;
import FirebaseBackend.POJO.Medication_Memento;
import FirebaseBackend.POJO.Object_Memento;
import FirebaseBackend.POJO.Person_Memento;
import KairosBackend.ImageListAdapter;
import KairosBackend.KairosClient;
import KairosBackend.KairosUtils;
import KairosBackend.POJO_Kairos.Requests.Recognize.KairosRequest_Recognize;
import KairosBackend.POJO_Kairos.Responses.Recognize.KairosResponse_Recognize;
import KairosBackend.POJO_Kairos.Responses.Recognize.KairosResponse_Recognize_Candidate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecognizeActivity extends AppCompatActivity {

    ImageView imageView_Camera, imageView_SelectedMemento;
    ListView listView_PossibleMementos;
    AutoCompleteTextView tf_MedicaitonName;
    TextView textView_SelectedMementoData;

    //Firebase stuff
    FirebaseUser user;
    private FirebaseDatabase  mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private FirebaseStorage   mFirebaseStorage;
    private StorageReference mMedicationPhotosStorageReference;
    private StorageReference mObjectsPhotosStorageReference;
    private StorageReference mPeoplePhotosStorageReference;

    //Kairos stuff
    KairosClient kairosClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        Button btn_Camera = (Button) findViewById(R.id.btn_RecognizeActivity_Camera);
        Button btn_Face = (Button) findViewById(R.id.btn_RecognizeActivity_RecognizeFace);
        Button btn_Object = (Button) findViewById(R.id.btn_RecognizeActivity_RecognizeObject);
        Button btn_Medication = (Button) findViewById(R.id.btn_RecognizeActivity_Medication);

        imageView_Camera = (ImageView) findViewById(R.id.imageView_RecognizeActivity_CameraPhoto);
        imageView_SelectedMemento = (ImageView) findViewById(R.id.imageView_RecognizeActivity_Selected);
        listView_PossibleMementos = (ListView) findViewById(R.id.listView_RecognizeActivity);
        tf_MedicaitonName = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_RecognizeActivity_MedicationName);
        textView_SelectedMementoData = (TextView) findViewById(R.id.textView_RecognizeActivity_MementoData);


        btn_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        btn_Face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               IdentifyFace();
            }
        });
        btn_Object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCloudVision();
            }
        });
        btn_Medication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {DisplaySelectedMedicationMemento();
            }
        });


        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }


        InitializeAPI();

        GetObjectMementoData();
        GetMedicationMementoData();
    }


    private void GetPersonMemento(final String topName) {
        mUserDatabaseReference.child("person_memento").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> personMemento = (Map<String, Object>) snapshot.getValue();
                    Person_Memento mm = new Person_Memento();
                    mm.setName((String) personMemento.get("name"));
                    mm.setAudio_url((String) personMemento.get("audio_url"));
                    mm.setImage_url((String) personMemento.get("image_url"));
                    mm.setDescription((String) personMemento.get("description"));
                    System.out.println("Object!: "+mm.getName());

                    if(mm.getName().trim().equals(topName)){
                        DisplaySelectedMemento(mm.getName(), mm.getDescription(), mm.getImage_url());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<Object_Memento> ObjectMementoList = new ArrayList<>();
    private void GetObjectMementoData() {
        //Make sure to reset values
        ObjectMementoList.clear();

        mUserDatabaseReference.child("object_memento").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> objectMemento = (Map<String, Object>) snapshot.getValue();
                    Object_Memento mm = new Object_Memento();
                    mm.setName((String) objectMemento.get("name"));
                    mm.setAudio_url((String) objectMemento.get("audio_url"));
                    mm.setImage_url((String) objectMemento.get("image_url"));
                    mm.setDescription((String) objectMemento.get("description"));
                    mm.setLabelsList((ArrayList<String>) objectMemento.get("labelsList"));
                    ObjectMementoList.add(mm);
                    System.out.println("Object!: "+mm.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<Medication_Memento> MedicationMementoList = new ArrayList<>();
    private void GetMedicationMementoData() {
        //Make sure to reset values
        MedicationMementoList.clear();

        mUserDatabaseReference.child("medication_memento").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> medicationMemento = (Map<String, Object>) snapshot.getValue();
                    Medication_Memento mm = new Medication_Memento();
                    mm.setName((String) medicationMemento.get("name"));
                    mm.setAudio_url((String) medicationMemento.get("audio_url"));
                    mm.setImage_url((String) medicationMemento.get("image_url"));
                    mm.setInstructions((String) medicationMemento.get("instructions"));
                    System.out.println("ADD: "+mm.getName());
                    MedicationMementoList.add(mm);
                }

                DisplayMedicationNamesForUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void DisplaySelectedMedicationMemento() {

        String selectedName = tf_MedicaitonName.getText().toString().trim();

        Medication_Memento selectedMedicationMemento = null;
        for(Medication_Memento mm: MedicationMementoList){
            if(selectedName.equals(mm.getName().trim())){
                selectedMedicationMemento = mm;
                break;
            }
        }

        if(selectedMedicationMemento == null){
            System.out.println("Cannot find medication memento with name: "+selectedName);
            return;
        }
        DisplaySelectedMemento(selectedMedicationMemento.getName(), selectedMedicationMemento.getInstructions(), selectedMedicationMemento.getImage_url());



    }


    Bitmap mCurrentMementoImage;
    private void DisplaySelectedMemento(String name, String description, String imageURL) {
        Glide.with(this).load(imageURL.trim()).into(imageView_SelectedMemento);

        try {
            Glide.with(this)
                    .load(imageURL)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            mCurrentMementoImage = bitmap;
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
        }

        String displayData = name+System.lineSeparator()+description;
        textView_SelectedMementoData.setText(displayData);



    }


    private void DisplayMedicationNamesForUI() {
        System.out.println("DisplayMedicationNamesForUI");
        String[] medNames = new String[MedicationMementoList.size()];
        int i = 0;
        for(Medication_Memento mm: MedicationMementoList){
            medNames[i++] = mm.getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_singlechoice, medNames);
        tf_MedicaitonName.setThreshold(1);
        tf_MedicaitonName.setAdapter(adapter);
    }


    private void InitializeAPI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mUserDatabaseReference = mFirebaseDatabase.getReference().child("user_"+user.getUid());
        mPeoplePhotosStorageReference = mFirebaseStorage.getReference().child("user_"+user.getUid()+"/people_images");
        mObjectsPhotosStorageReference = mFirebaseStorage.getReference().child("user_"+user.getUid()+"/object_images");
        mMedicationPhotosStorageReference = mFirebaseStorage.getReference().child("user_"+user.getUid()+"/medication_images");

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(KairosClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        kairosClient= retrofit.create(KairosClient.class);

    }

    private void IdentifyFace() {



        KairosRequest_Recognize request = new KairosRequest_Recognize();
        request.setGalleryName(user.getUid());
        String base64 = KairosUtils.ConvertImageToBase64(mCurrentCameraImage, Bitmap.CompressFormat.PNG);
        request.setImage(base64);
        Call<KairosResponse_Recognize> call =  kairosClient.postRecognize(request);
        call.enqueue(new Callback<KairosResponse_Recognize>() {
            @Override
            public void onResponse(Call<KairosResponse_Recognize> call, Response<KairosResponse_Recognize> response) {
                System.out.println("Response: "+response.raw().toString());
                if(response.body()!= null) {
                    System.out.println("Response: " + response.message());
                    String topName = null;
                    try {
                        topName = response.body().getImages().get(0).getCandidates().get(0).getSubjectId();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    for(KairosResponse_Recognize_Candidate cand:  response.body().getImages().get(0).getCandidates()){
                        System.out.println(cand.getSubjectId()+"\t"+cand.getConfidence());
                    }


                    GetPersonMemento(topName);

                }
            }

            @Override
            public void onFailure(Call<KairosResponse_Recognize> call, Throwable t) {
                Toast.makeText(RecognizeActivity.this, "FAILURE! Call: "+call.toString(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }

        });



    }




    public void callCloudVision() {

        final Bitmap bitmap = mCurrentCameraImage;
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
                    ProcessCloudVisionResults(CloudVisionAPI.convertResponseToString(response));

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


    ArrayList<Object_Memento> ObjectMementoListMatched = new ArrayList<>();
    private void ProcessCloudVisionResults(final ArrayList<String> result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                for(Object_Memento om: ObjectMementoList){
                    int match = 0, miss = 0;
                    for(String label: om.getLabelsList()){
                        boolean matched = false;
                        for(String prediction: result){
                            String pred = prediction.split("\t")[1].trim();
                            if(label.trim().equals(pred)){
                                matched = true;
                                break;
                            }
                        }//
                        if(matched) match++;
                        else miss++;
                    }//
                    if(match > 1){//TODO make this scoring better, into a ranking system
                        ObjectMementoListMatched.add(om);
                    }
                }///////
                DisplayCloudVisionResults();
            }
        });


    }

    private void DisplayCloudVisionResults() {

        if(ObjectMementoListMatched.size() == 0){
            System.out.println("No matches found!");
            return;
        }

        String[] imgURLs = new String[ObjectMementoListMatched.size()];

        int i =0;
        for(Object_Memento om: ObjectMementoListMatched) {
            imgURLs[i++] = om.getImage_url();
        }

        listView_PossibleMementos.setAdapter(new ImageListAdapter(this, imgURLs));
        listView_PossibleMementos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String imageInfo = (String) adapterView.getAdapter().getItem(i);
                Object_Memento om = ObjectMementoListMatched.get(i);
                DisplaySelectedMemento(om.getName(), om.getDescription(), om.getImage_url());
            }
        });



    }


    //code to take photo and display it
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private Bitmap mCurrentCameraImage;

    private void dispatchTakePictureIntent() {

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
            mCurrentCameraImage = KairosUtils.setPic(imageView_Camera, mCurrentPhotoPath);
            if(mCurrentCameraImage == null){
                System.out.println("onActivityResult: Could not set the picture");
            }
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
