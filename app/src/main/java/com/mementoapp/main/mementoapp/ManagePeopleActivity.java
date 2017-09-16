package com.mementoapp.main.mementoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import KairosBackend.KairosClient;
import KairosBackend.KairosUtils;
import KairosBackend.POJO_Kairos.Requests.Enroll.KairosRequest_Enroll;
import KairosBackend.POJO_Kairos.Requests.GalleryView.KairosRequest_GalleryView;
import KairosBackend.POJO_Kairos.Responses.Enroll.KairosResponse_Enroll;
import KairosBackend.POJO_Kairos.Responses.GalleryView.KairosResponse_GalleryView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManagePeopleActivity extends AppCompatActivity {

    //Firebase
    private FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;

    private KairosClient kairosClient;

    //ListView stuff
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_people);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        InitializeAPI();

        Button btnUpdatePeople = (Button) findViewById(R.id.btn_ManagePeople_AddNew);
        btnUpdatePeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity(AddNewKairosActivity.class);
            }
        });

        ListView listPeople = (ListView) findViewById(R.id.listView_people);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listPeople.setAdapter(adapter);
        listPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String entry= (String) adapterView.getAdapter().getItem(i);
                String nameFromList = firebasePersonMementosData.get(i);
                System.out.println("You clicked on: "+entry+" "+nameFromList);
                StartManagePersonActivity(nameFromList);
            }
        });

        ArrayList<String> list = new ArrayList<String>();
        list.add("--None--");
        updateUIListView(list);
        APICall_Firebase_GetPersonMementoNames();
    }

    private void updateUIListView(List<String> items) {
        listItems.clear();
        listItems.addAll(items);
        adapter.notifyDataSetChanged();
    }

    private void InitializeAPI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("user_"+user.getUid());

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(KairosClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        kairosClient= retrofit.create(KairosClient.class);
    }

    private void StartActivity(Class<?> tClass) {
        if(tClass == null) return;
        Intent intent = new Intent(this, tClass);
        startActivity(intent);
    }
    private void StartManagePersonActivity(String data) {
        Intent intent = new Intent(this, ManagePersonActivity.class);
        intent.putExtra("selected_person", data);
        startActivity(intent);
    }

    ArrayList<String> firebasePersonMementosData = new ArrayList<String>();
    private void APICall_Firebase_GetPersonMementoNames(){
        firebasePersonMementosData.clear();
        mUserDatabaseReference.child("person_memento").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> names = new ArrayList<String>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Map<String, Object> personMemento = (Map<String, Object>)  child.getValue();
                    String key = child.getKey();
                    String name = ((String)personMemento.get("name")).trim();
                    firebasePersonMementosData.add(key+"\t"+personMemento.get("name"));
                    names.add(name);
                }
                updateUIListView(names);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
