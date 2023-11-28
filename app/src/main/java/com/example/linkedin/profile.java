package com.example.linkedin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class profile extends AppCompatActivity {

    private EditText shortBioEdt, skillsEdt;
    private Button profileBtn;
    private ImageView profilePicture;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private Uri imageUri;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        shortBioEdt = findViewById(R.id.shortBio);
        skillsEdt = findViewById(R.id.skills);
        profileBtn = findViewById(R.id.profilebtn);
        profilePicture = findViewById(R.id.profileImg);

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shortBio = shortBioEdt.getText().toString().trim();
                String skills = skillsEdt.getText().toString().trim();

                if (TextUtils.isEmpty(shortBio) || TextUtils.isEmpty(skills)) {
                    Toast.makeText(getApplicationContext(), "Fill in all the necessary details", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        String email = user.getEmail();
                        updateUserDetails(email, shortBio, skills);
                        uploadImage(); // Call the method to upload the image
                    } else {
                        Toast.makeText(getApplicationContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateUserDetails(String email, String shortBio, String skills) {
        // Create a user map with additional details (short bio, skills, etc.)
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("shortBio", shortBio);
        user.put("skills", skills);

        // Add/update the user details in Firestore
        DocumentReference userRef = firestore.collection("users").document(email);
        userRef.update(user) // Use set instead of update if the document doesn't exist yet
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "User details updated in Firestore", Toast.LENGTH_SHORT).show();
                            // Redirect to profile activity or perform any other necessary action
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to update user details in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadImage() {
        storageReference = FirebaseStorage.getInstance().getReference();
        if (imageUri != null) {
            storageReference.child("profile_images").child(auth.getCurrentUser().getUid()).putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // You can also set the image to an ImageView if needed
            // profilePicture.setImageURI(imageUri);
        }
    }
}
