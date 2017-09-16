package com.mementoapp.main.mementoapp;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

import FirebaseBackend.POJO.Person_Kairos;
import FirebaseBackend.POJO.Person_Kairos_Extra;
import FirebaseBackend.POJO.Person_Memento;
import KairosBackend.ImageListAdapter;
import KairosBackend.KairosClient;
import KairosBackend.KairosUtils;
import KairosBackend.POJO_Kairos.Requests.Enroll.KairosRequest_Enroll;
import KairosBackend.POJO_Kairos.Requests.GalleryRemoveSubject.KairosRequest_GalleryRemoveSubject;
import KairosBackend.POJO_Kairos.Requests.GalleryRemoveSubjectFace.KairosRequest_GalleryRemoveSubjectFace;
import KairosBackend.POJO_Kairos.Responses.Enroll.KairosResponse_Enroll;
import KairosBackend.POJO_Kairos.Responses.GalleryRemoveSubjectFace.KairosResponse_GalleryRemoveSubjectFace;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManagePersonActivity extends AppCompatActivity {

    //Passed data
    private String passed_name = "";
    private String passed_firebaseID = "";

    //Firebase
    private FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPeoplePhotosStorageReference;

    private KairosClient kairosClient;


    //UI
    ListView listView_FaceImages;
    TextView textView_photosOfTitle;
    ImageView imageView_selectedImage;
    Button btnAddNewImage;
    Button btnRemoveSelectedImage;
    EditText tf_Description;
    Button btnUpdate;
    Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_person);

        Bundle bundle = getIntent().getExtras();
        String selected_person = bundle.getString("selected_person");

        ParsePassedData(selected_person);
        InitializeAPI();

        //UI assignments
        listView_FaceImages = (ListView) findViewById(R.id.listView_faceImages);
        textView_photosOfTitle =(TextView) findViewById(R.id.textView_PhotosOfTitle);
        imageView_selectedImage = (ImageView) findViewById(R.id.imageView_ManagePerson_SelectedImage);
        btnAddNewImage = (Button) findViewById(R.id.btn_ManagePerson_AddNewImage);
        btnRemoveSelectedImage = (Button) findViewById(R.id.btn_ManagePerson_RemoveImage);
        tf_Description = (EditText) findViewById(R.id.editText_ManagePerson_Description);
        btnUpdate = (Button) findViewById(R.id.btn_ManagePerson_Update);
        btnDelete = (Button) findViewById(R.id.btn_ManagePerson_Delete);


        ActionBar ab = getActionBar();
        if(ab != null) {
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle("Manage " + passed_name);
        }

        btnAddNewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Add new image!");
                StartAddNewSpecificKairosActivity();
            }
        });
        btnRemoveSelectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Remove selected image!");
                RemoveSelectedImage();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdatePersonInfo();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Delete!");
            }
        });


        //Get memento data
        GetMementoData(passed_firebaseID, passed_name);


    }

    private void StartAddNewSpecificKairosActivity() {
        Intent intent = new Intent(this, AddNewSpecificKairosActivity.class);
        intent.putExtra("name", passed_name);
        startActivity(intent);
    }

    private ArrayList<Person_Kairos_Extra> firebase_retrieved_PersonKairos_List = null;
    private Person_Memento firebase_retrieved_PersonMemento = null;
    private void GetMementoData(String firebaseID, String name) {
        //Make sure to reset values
        firebase_retrieved_PersonMemento = null;
        firebase_retrieved_PersonKairos_List = null;

        mUserDatabaseReference.child("person_memento").child(firebaseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> personMemento = (Map<String, Object>)  dataSnapshot.getValue();

                firebase_retrieved_PersonMemento = new Person_Memento();
                firebase_retrieved_PersonMemento.setName((String)personMemento.get("name"));
                firebase_retrieved_PersonMemento.setAudio_url((String)personMemento.get("audio_url"));
                firebase_retrieved_PersonMemento.setImage_url((String)personMemento.get("image_url"));
                firebase_retrieved_PersonMemento.setDescription((String)personMemento.get("description"));

                UpdateUIWithFirebaseData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUserDatabaseReference.child("kairos_people").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebase_retrieved_PersonKairos_List = new ArrayList<Person_Kairos_Extra>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Map<String, Object> personKairos = (Map<String, Object>)  child.getValue();
                    Person_Kairos_Extra pk = new Person_Kairos_Extra();
                    pk.setImage_url((String)personKairos.get("image_url"));
                    pk.setKairos_faceid((String)personKairos.get("kairos_faceid"));
                    pk.setFirebase_uid(child.getKey());
                    firebase_retrieved_PersonKairos_List.add(pk);
                }
                UpdateUIWithFirebaseData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void UpdateUIWithFirebaseData(){
        if(firebase_retrieved_PersonMemento == null || firebase_retrieved_PersonKairos_List == null || firebase_retrieved_PersonKairos_List.size() == 0){
            System.out.println("Waiting for both calls to complete");
            return;
        }

        String desc = firebase_retrieved_PersonMemento.getDescription();
        if(desc == null || desc.equals("-101")){
            desc = "";
        }
        tf_Description.setText(desc);

        String[] imgURLs = new String[firebase_retrieved_PersonKairos_List.size()];
        String dataOut = firebase_retrieved_PersonMemento.getName()+"\t"+firebase_retrieved_PersonMemento.getImage_url()+"\n";
        int i =0;
        for(Person_Kairos_Extra pk: firebase_retrieved_PersonKairos_List) {
            dataOut += "\t"+pk.getKairos_faceid()+"\t"+pk.getImage_url()+"\n";
            imgURLs[i++] = pk.getImage_url();
        }

        System.out.println("Data: "+dataOut);

        if(imgURLs.length == 1) {
            textView_photosOfTitle.setText(imgURLs.length + " photo of " + passed_name);
        }else{
            textView_photosOfTitle.setText(imgURLs.length + " photos of " + passed_name);
        }
        listView_FaceImages.setAdapter(new ImageListAdapter(ManagePersonActivity.this, imgURLs));
        listView_FaceImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String imageInfo = (String) adapterView.getAdapter().getItem(i);
                Person_Kairos_Extra pk = firebase_retrieved_PersonKairos_List.get(i);
                DisplayFullImage(pk, imageInfo, view);
            }
        });
    }

    Person_Kairos_Extra selectedFace;
    private void DisplayFullImage(Person_Kairos_Extra pk, String imageInfo, View view) {
        selectedFace = pk;
        Glide.with(this).load(selectedFace.getImage_url().trim()).into(imageView_selectedImage);
    }

    private void ParsePassedData(String selected_person) {
        try{//try/catch in case string is null or not formatted correctly
            String[] parts = selected_person.split("\t");
            passed_firebaseID = parts[0].trim();
            passed_name = parts[1].trim();
        }catch(Exception e){
            e.printStackTrace();
            finish();
            return;
        }
    }


    private void UpdatePersonInfo() {

        //TODO, this should update the audio file as well?
        String description = tf_Description.getText().toString().trim();
        if(description == null || description.length() <= 0){
            return;
        }else if(description.trim().length() > 50){
            Toast.makeText(this,"Your description is too long. ",Toast.LENGTH_LONG);
        }

        mUserDatabaseReference.child("person_memento").child(passed_firebaseID).child("description").setValue(description);

    }

    private void UpdatePersonImageURL(String newImageURL) {
        mUserDatabaseReference.child("person_memento").child(passed_firebaseID).child("image_url").setValue(newImageURL);
    }


    private void RemoveSelectedImage() {

        //*******/Verify there will still be at least one image left
        if(firebase_retrieved_PersonKairos_List.size()<=1){
            Toast.makeText(this, "There must always be at least one image. You cannot delete this until you add a new image first!", Toast.LENGTH_LONG).show();
            return;
        }

        System.out.println("Remove: firebase db/kairos_people/"+passed_name+": "+selectedFace.getFirebase_uid());
        System.out.println("Remove: firebase storage/"+passed_name+": "+selectedFace.getFirebase_uid());

        //*******/make sure it is not the "featured" image for this person
        if(firebase_retrieved_PersonMemento.getImage_url().equals(selectedFace.getImage_url())){
            for(Person_Kairos_Extra pk: firebase_retrieved_PersonKairos_List){
                if(!(pk.getImage_url().equals(firebase_retrieved_PersonMemento.getImage_url()))){
                    UpdatePersonImageURL(pk.getImage_url());
                }
            }

        }

        //*******/remove it from firebaseDatabase section: kairos_people
        DatabaseReference myRef = mUserDatabaseReference.child("kairos_people").child(passed_name).child(selectedFace.getFirebase_uid());
        myRef.removeValue();

        //*******/Remove from storage
        StorageReference mySRef = mPeoplePhotosStorageReference.child(passed_name).child(selectedFace.getKairos_faceid()+".png");
        mySRef.delete();

        //*******/Remove it from kairos under its faceId
        String subjectID = passed_name.trim();
        String faceID = selectedFace.getKairos_faceid().trim();

        KairosRequest_GalleryRemoveSubjectFace request = new KairosRequest_GalleryRemoveSubjectFace();
        request.setGalleryName(user.getUid());
        request.setSubjectId(subjectID);
        request.setFaceId(faceID);
        System.out.println("Kairos call: "+user.getUid()+"\t"+subjectID+"\t"+faceID);
        Call<KairosResponse_GalleryRemoveSubjectFace> call =  kairosClient.postGalleryRemoveSubjectFace(request);
        call.enqueue(new Callback<KairosResponse_GalleryRemoveSubjectFace>() {
            @Override
            public void onResponse(Call<KairosResponse_GalleryRemoveSubjectFace> call, Response<KairosResponse_GalleryRemoveSubjectFace> response) {
                System.out.println("Response: "+response.raw().toString());
                if(response.body()!= null) {
                    System.out.println("Response: " + response.body().getStatus() + "\n" + response.body().getMessage());
                }
                RefreshActivity();
            }

            @Override
            public void onFailure(Call<KairosResponse_GalleryRemoveSubjectFace> call, Throwable t) {
                Toast.makeText(ManagePersonActivity.this, "FAILURE! Call: "+call.toString(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });





    }


    private void RefreshActivity(){
        recreate();
    }



    private void InitializeAPI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("user_"+user.getUid());

        mFirebaseStorage = FirebaseStorage.getInstance();
        mPeoplePhotosStorageReference = mFirebaseStorage.getReference().child("user_"+user.getUid()+"/people_images");


        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(KairosClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        kairosClient= retrofit.create(KairosClient.class);
    }
}
