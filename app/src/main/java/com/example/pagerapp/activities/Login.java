package com.example.pagerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pagerapp.R;
import com.example.pagerapp.databinding.ActivityLoginBinding;
import com.example.pagerapp.utilities.Keys;
import com.example.pagerapp.utilities.PreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 120;
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private PreferenceManager preferenceManager; //Instantiating preferenceManager class under utilities to get the user details//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Keys.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        //FireBase Auth//
        mAuth = FirebaseAuth.getInstance();
    }

    private void setListeners() {
        binding.googleButton.setOnClickListener(v->{
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
        binding.signUpButton.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),SignUp.class);
            startActivity(intent);
            finish();
        });
        binding.loginButton.setOnClickListener(v->{
            if(isValidLogInCredentials()){
                logIn();
            }
        });
        binding.forgotPassword.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),Reset.class);
            startActivity(intent);
            finish();
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Exception exception = task.getException();
            if(task.isSuccessful()) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d("Login Activity", "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("Login Activity", "Google sign in failed", e);
                }
            }else{
                assert exception != null;
                Log.w("Login Activity", exception.toString());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login Activity", "signInWithCredential:success");
                        Intent intent =  new Intent(getApplicationContext(),SignUp.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login Activity", "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private void logIn(){

//        byte[] base64 = binding.password.getText().toString().trim();
//        byte[] password = Base64.decode(base64, Base64.DEFAULT);
        byte[] password = binding.password.getText().toString().getBytes();
        String encodedPassword = Base64.encodeToString(password,Base64.DEFAULT);

        loadingAnimation(true);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Keys.COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_EMAIL,binding.email.getText().toString().trim())
                .whereEqualTo(Keys.KEY_PASSWORD,encodedPassword)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() >0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0); //used to fetch data from the Firestore  which can be later checked in the given data//
                        preferenceManager.putBoolean(Keys.KEY_IS_SIGNED_IN,true); //assigning that the user has logged in and initialising all preference variables//
                        preferenceManager.putString(Keys.USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Keys.KEY_NAME,documentSnapshot.getString(Keys.KEY_NAME));
                        preferenceManager.putString(Keys.IMAGE,documentSnapshot.getString(Keys.IMAGE));
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        loadingAnimation(false);
                        Snackbar.make(binding.loginLayout,"Invalid Credentials",Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadingAnimation(Boolean isLoading){ //to set the loading of  the progress bar//
        if(isLoading){
            binding.loginButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.loginButton.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isValidLogInCredentials(){ //to check if the user has given proper credentials//
        if(binding.email.getText().toString().trim().isEmpty()) {
            Snackbar.make(binding.loginLayout, "Enter email", Snackbar.LENGTH_SHORT).show();
            return (false);
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()) {
            Snackbar.make(binding.loginLayout, "Invalid Email", Snackbar.LENGTH_SHORT).show();
            return (false);
        }else if(binding.password.getText().toString().trim().isEmpty()){
            Snackbar.make(binding.loginLayout,"Enter password",Snackbar.LENGTH_SHORT).show();
            return (false);
        }else {
            return (true);
        }
    }
}