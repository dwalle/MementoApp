package com.mementoapp.main.mementoapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /****Firebase*****/

    //Login-Auth
    private static final int RC_SIGN_IN = 1;
    public static final String ANONYMOUS = "anonymous";
    private String userName;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    //Database

    //Storage

    /*****Google Vision*****/

    /*****Kairos*****/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.userName = ANONYMOUS;

        //Initialize firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();

        Firebase_AuthorizeUser();

        Button btnRecognize = (Button) findViewById(R.id.btn_MainActivity_HelpRecognize);
        btnRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity(RecognizeActivity.class);
            }
        });

    }

    private void StartActivity(Class<?> tClass) {
        if(tClass == null) return;
        Intent intent = new Intent(this, tClass);
        startActivity(intent);
    }


    private void onSignedInInitialize(String username){
        this.userName = username;
        String userID = mFirebaseAuth.getCurrentUser().getUid();
        Toast.makeText(this, "Welcome, "+this.userName, Toast.LENGTH_LONG).show();
        System.out.println("Your id: "+userID);
    }
    private void onSignedOutCleanup(){

        this.userName = ANONYMOUS;
        Toast.makeText(this, "Goodbye, "+this.userName, Toast.LENGTH_LONG).show();
    }


    private void Firebase_AuthorizeUser() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    System.out.println("User is signed in!");
                    //signed in
                    onSignedInInitialize(user.getDisplayName());
                }else{
                    System.out.println("User is NOT signed in!");
                    onSignedOutCleanup();
                    //signed out
                    List providers = Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                    );

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);

                }

            }
        };
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Sign in cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.manage_mementos_menu:
                OpenManageMementosActivity();
                return true;
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void OpenManageMementosActivity(){
        Intent intent = new Intent(this, ManageMementosActivity.class);
        startActivity(intent);
    }
}
