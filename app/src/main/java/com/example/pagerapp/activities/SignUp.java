package com.example.pagerapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pagerapp.databinding.ActivitySignUpBinding;
import com.example.pagerapp.utilities.Constants;
import com.example.pagerapp.utilities.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;


public class SignUp extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager; //Instantiating PreferenceManager which is created under Utilities package//
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext()); // initialising preferenceManager object store the data//
        setListeners();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user != null) { //if the user logged in with google's firebase//
            binding.userName.setText(user.getDisplayName());
            binding.email.setText(user.getEmail());
        }
    }

    private void setListeners() {
        binding.backImage.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        });
        binding.signUpButton.setOnClickListener(v->{
            if(isValidSignupDetails()){
                signUp(); //calling Signup method if it is a valid details//
            }
        });
        binding.addImage.setOnClickListener(v->{
            Intent intent =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //Opening the file explorer using intent//
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Giving the intent access to fetch the uri of the image//
            pickImage.launch(intent);//Stating the image intent by calling the pickImage method which is below//
        });
        binding.userImage.setOnClickListener(v -> {
            Intent intent =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //Opening the file explorer using intent//
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Giving the intent access to fetch the uri of the image//
            pickImage.launch(intent);//Stating the image intent by calling the pickImage method which is below//
        });
    }

    private void signUp(){
        loading(true); //activating the loading screen//
        FirebaseFirestore database = FirebaseFirestore.getInstance();  // Creating an instance of firebase to store data//
        HashMap<String,Object> user = new HashMap<>(); //Creating an Hashmap which will hold the data based on the string constants in utilities package//
        user.put(Constants.KEY_NAME,binding.userName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.email.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.password.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> { //when user is added successfully this block will be executed//
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true); //storing that the user is signed in and storing all the values in the preference//
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.userName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }).addOnFailureListener(exception ->{ //this block will be executed when there is an issue with the user's login//
                    loading(false);
                    Snackbar.make(binding.getRoot().getRootView(), Objects.requireNonNull(exception.getMessage()),Snackbar.LENGTH_SHORT).show();
        });

    }

    //Method to Valid if user is entering proper valid Credentials//
    private Boolean isValidSignupDetails(){
        if(encodedImage == null){
            Snackbar.make(binding.getRoot().getRootView(),"Add a profile Image",Snackbar.LENGTH_SHORT).show();
            return (false);
        }else if(binding.userName.getText().toString().trim().isEmpty()){
            Snackbar.make(binding.getRoot().getRootView(),"Username is empty",Snackbar.LENGTH_SHORT).show();
            return (false);
        }else if(binding.email.getText().toString().trim().isEmpty()){
            Snackbar.make(binding.getRoot().getRootView(),"Email is empty",Snackbar.LENGTH_SHORT).show();
            return (false);
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()){
            Snackbar.make(binding.getRoot().getRootView(),"Invalid Email",Snackbar.LENGTH_SHORT).show();
            return (false);
        }else if(binding.password.getText().toString().trim().isEmpty()){
            Snackbar.make(binding.getRoot().getRootView(),"Enter password",Snackbar.LENGTH_SHORT).show();
            return (false);
        }else if(binding.retypePassword.getText().toString().trim().isEmpty()){
            Snackbar.make(binding.getRoot().getRootView(),"Confirm your password",Snackbar.LENGTH_SHORT).show();
            return (false);
        }else if(!(binding.password.getText().toString()).equals(binding.retypePassword.getText().toString())){
            Snackbar.make(binding.getRoot().getRootView(),"Passwords don't match",Snackbar.LENGTH_SHORT).show();
            return (false);
        }else{
            return(true);
        }
    }
    //To encode a given image from the user//
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int preViewHeight = bitmap.getHeight()* previewWidth / bitmap.getWidth();// to get a proper circular Image//
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,preViewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //To get the size and put it in an array//
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);//to compress the Array//
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT); //Returns the Encoded image in base64 format which is of string type//
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData(); //getting image uri if the image is already set//
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri); //getting the image Uri using input Stream//
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream); //building a bitmap from the uri using bitmap Factory//
                            binding.userImage.setImageBitmap(bitmap);//setting up the user Image//
                            binding.addImage.setVisibility(View.INVISIBLE);
                            encodedImage = encodeImage(bitmap);//encodes the image//
                        }catch (FileNotFoundException exception){
                            exception.printStackTrace();
                        }
                    }
                }
            }
    );

    //To set up the Loading progressBar/ Animation//
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.signUpButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.signUpButton.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //On Back Press//
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),Login.class);
        startActivity(intent);
        finish();
    }
}