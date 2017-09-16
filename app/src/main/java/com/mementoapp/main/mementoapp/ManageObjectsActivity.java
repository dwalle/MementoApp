package com.mementoapp.main.mementoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageObjectsActivity extends AppCompatActivity {

    //Firebase
    private FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;

    //ListView stuff
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    //Consts
    public static final int OPEN_MANAGE_SPECIFIC_ACTIVITY= 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_objects);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        InitializeAPI();

        Button btnAddNewMedication = (Button) findViewById(R.id.btn_ManageObjects_AddNew);
        btnAddNewMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity(AddNewObjectActivity.class);
            }
        });

        ListView listPeople = (ListView) findViewById(R.id.listView_ManageObjects_Objects);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listPeople.setAdapter(adapter);
        listPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(firebaseObjectMementosData.size() == 0) return;
                String entry= (String) adapterView.getAdapter().getItem(i);
                String nameFromList = firebaseObjectMementosData.get(i);
                System.out.println("You clicked on: "+entry+" "+nameFromList);
                StartManageObjectActivity(nameFromList);
            }
        });
        BeginActivity();
    }


    private void BeginActivity() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("--None--");
        updateUIListView(list);
        APICall_Firebase_GetObjectMementoNames();
    }

    private void StartActivity(Class<?> tClass) {
        if(tClass == null) return;
        Intent intent = new Intent(this, tClass);
        startActivity(intent);
    }


    private void StartManageObjectActivity(String data) {
        Intent intent = new Intent(this, ManageObjectActivity.class);
        intent.putExtra("selected_object", data);
        startActivityForResult(intent, OPEN_MANAGE_SPECIFIC_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_MANAGE_SPECIFIC_ACTIVITY) {
            BeginActivity();
        }
    }

    private void updateUIListView(List<String> items) {
        if(items.size() == 0) return;
        listItems.clear();
        listItems.addAll(items);
        adapter.notifyDataSetChanged();
    }

    private void InitializeAPI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("user_"+user.getUid());
    }


    ArrayList<String> firebaseObjectMementosData = new ArrayList<String>();
    private void APICall_Firebase_GetObjectMementoNames(){
        firebaseObjectMementosData.clear();
        mUserDatabaseReference.child("object_memento").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> names = new ArrayList<String>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Map<String, Object> objectMemento = (Map<String, Object>)  child.getValue();
                    String key = child.getKey();
                    String name = ((String)objectMemento.get("name")).trim();
                    firebaseObjectMementosData.add(key+"\t"+objectMemento.get("name"));
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
