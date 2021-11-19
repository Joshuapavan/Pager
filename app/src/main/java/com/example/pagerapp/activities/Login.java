package com.example.pagerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pagerapp.R;
import com.example.pagerapp.databinding.ActivityLoginBinding;
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


public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 120;
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            if(binding.email.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty()){
                Snackbar.make(binding.loginLayout,"Please, fill all Credentials",Snackbar.LENGTH_SHORT).show();
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()) {
                Snackbar.make(binding.loginLayout, "Invalid Email", Snackbar.LENGTH_SHORT).show();
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
}