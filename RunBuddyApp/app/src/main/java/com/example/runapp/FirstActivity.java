package com.example.runapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstActivity extends AppCompatActivity {
    Button radius;
    Button activities;
    Button setActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        radius = findViewById(R.id.radius);
        activities = findViewById(R.id.activities);
        setActivity = findViewById(R.id.setActivity);

        // navigate to radius settings screen
        radius.setOnClickListener(view -> {
            Intent i = new Intent(FirstActivity.this, MapsActivity.class);
            startActivity(i);
        });

        // navigate to activities screen
        activities.setOnClickListener(view -> {
            Intent i = new Intent(FirstActivity.this, showActivities.class);
            startActivity(i);
        });

        // navigate to set new activity screen
        setActivity.setOnClickListener(view -> {
            Intent i = new Intent(FirstActivity.this, setNewActivity.class);
            startActivity(i);
        });
    }
}