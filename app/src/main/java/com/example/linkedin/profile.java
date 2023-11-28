package com.example.linkedin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class profile extends AppCompatActivity {
    private EditText shortBioEdt, skillsEdt;
    private Button profileBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        shortBioEdt = findViewById(R.id.shortBio);
        skillsEdt = findViewById(R.id.skills);
        profileBtn = findViewById(R.id.profilebtn);

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
                    } else {
                        // Handle the case when the user is not authenticated
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
        userRef.update(user)
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
}
