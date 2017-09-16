package com.mementoapp.main.mementoapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ManageMementosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_mementos);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user==null){
            System.out.println("You are not signed in!");
            finish();
            return;
        }

        System.out.println("Manage Mementos: Signed in as: "+user.getDisplayName()+"   "+user.getUid());

        Button btnUpdatePeople = (Button) findViewById(R.id.btn_ManageMementos_UpdatePeople);
        Button btnUpdateObjects = (Button) findViewById(R.id.btn_ManageMementos_UpdateObjects);
        Button btnUpdateMedication = (Button) findViewById(R.id.btn_ManageMementos_UpdateMedication);


        btnUpdatePeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity(ManagePeopleActivity.class);
            }
        });

        btnUpdateObjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity(ManageObjectsActivity.class);
            }
        });

        btnUpdateMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity(ManageMedicationActivity.class);
            }
        });

    }

    private void StartActivity(Class<?> tClass) {
        if(tClass == null) return;
        Intent intent = new Intent(this, tClass);
        startActivity(intent);
    }


}
