package com.example.androidclone2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class FeedActivity extends AppCompatActivity {
    ArrayList<String> tweets = new ArrayList<>();
    ArrayAdapter adapter;
    private static String TAG = "FeedActivity";
    Map<String,String> following = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        ListView lv = findViewById(R.id.feedList);

        populateFollowing();
        populateList();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,tweets);
        lv.setAdapter(adapter);
    }

    private void populateList() {
        FirebaseDatabase.getInstance().getReference().child("Tweets").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(following.containsKey(dataSnapshot.getKey().toString())) {
                    Map<String,String> tweetMap = (Map<String, String>) dataSnapshot.getValue();

                    String[] tweet = tweetMap.values().toArray(new String[tweetMap.size()]);
                    for(int i=0;i<tweet.length;i++) {
                        Log.d(TAG,tweet[i]);
                        //Get userName
                        String email = following.get(dataSnapshot.getKey().toString());
                        tweets.add(email +" : " +tweet[i]);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

}
