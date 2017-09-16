package com.mementoapp.main.mementoapp;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import FirebaseBackend.DownloadImageFileInBackground;
import FirebaseBackend.POJO.Object_Memento;

public class ManageObjectActivity extends AppCompatActivity {

    private ImageView imageView;

    private Button btnUpdate = null;
    private Button btnRemove = null;
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

    //ListView stuff
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    //
    private Bitmap mCurrentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_object);

        InitializeAPI();

        btnUpdate = (Button) findViewById(R.id.btn_ManageObjectActivity_Update);
        btnRemove = (Button) findViewById(R.id.btn_ManageObjectActivity_Remove);
        this.imageView = (ImageView)this.findViewById(R.id.imageView_ManageObjectActivity);
        tfName = (EditText) findViewById(R.id.tf_ManageObjectActivity_Name);
        tfDescription = (EditText) findViewById(R.id.tf_ManageObjectActivity_Description);
        tfMessage = (TextView) findViewById(R.id.textView_ManageObjectActivity_message);
        lvVisionList = (ListView) findViewById(R.id.listView_ManageObjectActivity);


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateObjectMemento();
            }
        });
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveObjectMemento();
            }
        });


        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        lvVisionList.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();
        String selected_object = bundle.getString("selected_object");
        ParsePassedData(selected_object);
        System.out.println("SELECTED OBJECT: "+selected_object);



        GetObjectMemento(passed_ObjectName, passed_firebaseKey);

    }


    private void UpdateObjectMemento() {
        //Currently, we are only allowing the Description to be updated
        String description = (tfDescription.getText().toString().trim());
        final String name = (tfName.getText().toString().trim());
        boolean descriptionChanged = false;//
        boolean nameChanged = false;
        if(!description.equals(firebase_retrieved_ObjectMemento.getDescription().trim())){
            descriptionChanged = true;
        }
        if(!name.equals(firebase_retrieved_ObjectMemento.getName().trim())){
            nameChanged = true;
        }


        if(descriptionChanged){
            UpdateObjectDescription(description);
        }

        if(nameChanged){

            //Reupload image file under new name
            UploadTask uploadTask = null;
            Uri file = Uri.fromFile(downloadedImageTask.imageFile);
            StorageReference fileRef = mObjectsPhotosStorageReference.child(name + ".png");
            uploadTask = fileRef.putFile(file);
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
                    RemoveImageFromStorage(firebase_retrieved_ObjectMemento.getName());
                    UpdateObjectName(name);
                    UpdateObjectImageURL(downloadUrl.toString());
                    ParsePassedData(passed_firebaseKey+"\t"+name);
                    GetObjectMemento(passed_ObjectName, passed_firebaseKey);
                }
            });





        }




    }

    private void RemoveImageFromStorage(String imageName) {
        StorageReference mySRef = mObjectsPhotosStorageReference.child(imageName+".png");
        mySRef.delete();
    }
    private void UpdateObjectName(String newName) {
        mUserDatabaseReference.child("object_memento").child(passed_firebaseKey).child("name").setValue(newName);
    }
    private void UpdateObjectImageURL(String newimage_url) {
        mUserDatabaseReference.child("object_memento").child(passed_firebaseKey).child("image_url").setValue(newimage_url);
    }
    private void UpdateObjectDescription(String newDescription) {
        mUserDatabaseReference.child("object_memento").child(passed_firebaseKey).child("description").setValue(newDescription);
    }

    private void RemoveObjectMemento() {

        //*******/Remove from firebase database
        DatabaseReference myRef = mUserDatabaseReference.child("object_memento").child(passed_firebaseKey);
        myRef.removeValue();

        //*******/Remove from storage
        StorageReference mySRef = mObjectsPhotosStorageReference.child(firebase_retrieved_ObjectMemento.getName()+".png");
        mySRef.delete();

        finish();
    }



    private void InitializeAPI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mUserDatabaseReference = mFirebaseDatabase.getReference().child("user_"+user.getUid());
        mObjectsPhotosStorageReference = mFirebaseStorage.getReference().child("user_"+user.getUid()+"/object_images");
    }


    private void updateUIListView(List<String> items) {
        if(items.size() == 0) return;
        listItems.clear();
        listItems.addAll(items);
        adapter.notifyDataSetChanged();
    }

    String passed_firebaseKey = "-101", passed_ObjectName = "-101";
    private void ParsePassedData(String parse){

        try{
            String[] data = parse.split("\t");
            passed_firebaseKey = data[0].trim();
            passed_ObjectName = data[1].trim();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private Object_Memento firebase_retrieved_ObjectMemento;
    private void GetObjectMemento(String name, String firebaseID) {
//Make sure to reset values
        firebase_retrieved_ObjectMemento = null;

        mUserDatabaseReference.child("object_memento").child(firebaseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> objectMemento = (Map<String, Object>)  dataSnapshot.getValue();

                firebase_retrieved_ObjectMemento = new Object_Memento();
                firebase_retrieved_ObjectMemento.setName((String)objectMemento.get("name"));
                firebase_retrieved_ObjectMemento.setAudio_url((String)objectMemento.get("audio_url"));
                firebase_retrieved_ObjectMemento.setImage_url((String)objectMemento.get("image_url"));
                firebase_retrieved_ObjectMemento.setDescription((String)objectMemento.get("description"));
                firebase_retrieved_ObjectMemento.setLabelsList((ArrayList<String>) objectMemento.get("labelsList"));

                UpdateUIWithFirebaseData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void UpdateUIWithFirebaseData(){
        if(firebase_retrieved_ObjectMemento == null){
            System.out.println("Cannot continue as medication memento does not exist");
            return;
        }

        String name = firebase_retrieved_ObjectMemento.getName();
        String description = firebase_retrieved_ObjectMemento.getDescription();
        if(description == null || description.equals("-101")){
            description = "";
        }
        tfDescription.setText(description);
        tfName.setText(name);
        updateUIListView(firebase_retrieved_ObjectMemento.getLabelsList());

        DisplayFullImage(firebase_retrieved_ObjectMemento.getImage_url());
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


}
