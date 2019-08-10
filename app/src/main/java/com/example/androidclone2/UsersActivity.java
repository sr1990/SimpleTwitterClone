package com.example.androidclone2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UsersActivity extends AppCompatActivity {
    ArrayList<String> users = new ArrayList<>(); // contains users
    Map<String,String> following = new TreeMap<>();
    ArrayAdapter adapter;
    DatabaseReference databaseTweets;
    ListView lv;

    private static String TAG = "UsersActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG,"onCreateOptionsMenu");
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.tweet_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.tweet) {
            Log.d(TAG,"Tweet Selected");
            tweet();
        } else if (item.getItemId() == R.id.feed) {
            Log.d(TAG,"Feed Selected");
            showFeed();

        } else if (item.getItemId() == R.id.logout) {
            Log.d(TAG,"Logout selected");
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private void showFeed() {

        Intent intent = new Intent(this,FeedActivity.class);
        startActivity(intent);
    }

    private void tweet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send Tweet!");
        final EditText tweetEditText = new EditText(this);
        builder.setView(tweetEditText);
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,"Sending tweet : "+tweetEditText.getText().toString());
                //Send the tweet
                databaseTweets = FirebaseDatabase.getInstance()
                        .getReference("Tweets")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                String id = databaseTweets.push().getKey();
                databaseTweets.child(id).setValue(tweetEditText.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

         lv = findViewById(R.id.listView);
        lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, users);

        lv.setAdapter(adapter);

        //Populate following and users
        populateFollowing();
        populateUsers();

        //List view on item click listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;

                //Add to database - following section
                if (checkedTextView.isChecked()) {
                    Log.d(TAG, "Checked!  " + checkedTextView.getText().toString());

                    String userToFollow = checkedTextView.getText().toString();
                    Log.d(TAG, "userToFollow: " + userToFollow);
                    String userId = userToFollow.substring(userToFollow.lastIndexOf(":") + 1);
                    String email = userToFollow.substring(0, userToFollow.indexOf("UID") - 1);
                    Log.d(TAG, "user: " + email);

                    FirebaseDatabase.getInstance().getReference()
                            .child("Following")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(userId)
                            .setValue(email);

                } else {
                    Log.d(TAG, "UNCHECKED");
                    //Remove from following in database
                    String userToFollow = checkedTextView.getText().toString();
                    String userId = userToFollow.substring(userToFollow.lastIndexOf(":") + 1);

                    FirebaseDatabase.getInstance().getReference()
                            .child("Following")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(userId).removeValue();
                }
            }
        });
    }

    private void populateFollowing() {
        FirebaseDatabase.getInstance().getReference().child("Following").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG,"Key -> "+dataSnapshot.getKey().toString());
                Log.d(TAG,"Value -> "+dataSnapshot.getValue().toString());
                following.put(dataSnapshot.getKey(),dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    //At the beginning when the users are populated
     private void populateUsers() {
         FirebaseDatabase.getInstance().getReference().child("users").addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                 if (dataSnapshot != null && dataSnapshot.child("email") != null && dataSnapshot.child("email").getValue() != null) {
                     String firstName = dataSnapshot.child("email").getValue().toString();
                     String uid = dataSnapshot.getKey().toString();
                     users.add(firstName + "  UID:" + uid);

                     adapter.notifyDataSetChanged();

                     if (following.containsKey(uid)) {
                         lv.setItemChecked(users.indexOf(firstName + "  UID:" + uid),true);
                     }
                 }
             }
             @Override
             public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
             @Override
             public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
             @Override
             public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {}
         });
     }

   }
