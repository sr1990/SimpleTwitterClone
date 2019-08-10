package com.example.androidclone2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText userName,password1;
    private static String TAG = "MainActivity";
    private EditText firstName,lastName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = findViewById(R.id.userName);
        password1 = findViewById(R.id.password);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG,"Current user logged in");
        } else {
            Log.d(TAG,"Current user not logged in");
        }
    }

    /*
     * Method called when createAccount button is pressed
     */
    public void createAccount(View view) {
        String email= userName.getText().toString();
        String password = password1.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(getApplicationContext(), "Authentication Successful.", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            String nameFirst = firstName.getText().toString();
                            String nameLast = lastName.getText().toString();
                            User newUser;
                            if(nameFirst.isEmpty() || nameLast.isEmpty()) {
                                 newUser = new User(task.getResult().getUser().getUid(),
                                        "firstName", "lastName",
                                        userName.getText().toString());
                            } else {
                                newUser = new User(task.getResult().getUser().getUid(),
                                        nameFirst, nameLast,
                                        userName.getText().toString());
                            }

                            FirebaseDatabase.getInstance().getReference().child("users")
                                    .child(task.getResult().getUser().getUid())
                                    .setValue(newUser);

                            //Start a new activity here
                            Intent intent = new Intent(getApplicationContext(),UsersActivity.class);
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /*
    *  Method called when SIGN IN Button is pressed
    * */
    public void signIntoAccount(View view) {
        Log.d("Sanil","Here");
        String email= userName.getText().toString();
        String password = password1.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success
                            Log.d(TAG, "Sign in successful");
                            Toast.makeText(getApplicationContext(), "SignIn Successful.", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(user.getEmail() != null)
                            Log.d(TAG,"username: "+user.getEmail());

                            //Start a new activity here
                            Intent intent = new Intent(getApplicationContext(),UsersActivity.class);
                            startActivity(intent);

                        } else {
                            Log.d(TAG, "signIn:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "SignIn failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
