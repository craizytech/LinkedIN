package com.example.linkedin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    EditText userNameEdt, emailEdt, passEdt, cnfPassEdt, phoneNumEdt;
    Button registerBtn;
    Spinner genderSpinner;
    FirebaseAuth nAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userNameEdt = findViewById(R.id.username);
        emailEdt = findViewById(R.id.userEmail);
        passEdt = findViewById(R.id.userPass);
        cnfPassEdt = findViewById(R.id.userCnfPass);
        phoneNumEdt = findViewById(R.id.usrPhone);
        registerBtn = findViewById(R.id.btnRegister);
        genderSpinner = findViewById(R.id.genderSpinner);

        nAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(emailEdt.getText());
                String username = String.valueOf(userNameEdt.getText());
                String password = String.valueOf(passEdt.getText());
                String confirmPassword = String.valueOf(cnfPassEdt.getText());
                String gender = String.valueOf(genderSpinner.getSelectedItem());


                if(!password.equals(confirmPassword)){
                    Toast.makeText(Register.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(username) && TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(Register.this, "Fill in all the necessary details", Toast.LENGTH_SHORT).show();
                } else {
                    nAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Register.this, "Now complete the following steps", Toast.LENGTH_SHORT).show();
                                addUserToFirestore(username, email, phoneNumEdt.getText().toString(), genderSpinner.getSelectedItem().toString());
                                Intent i = new Intent(getApplicationContext(), profile.class);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(Register.this, "User Registration Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void addUserToFirestore(String username, String email, String phoneNumber, String gender) {
        // Create a new user map
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("phone_number", phoneNumber);
        user.put("gender", gender);

        // Add the user to Firestore
        firestore.collection("users").document(email).set(user, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register.this, "User details added to Firestore", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Failed to add user details to Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}