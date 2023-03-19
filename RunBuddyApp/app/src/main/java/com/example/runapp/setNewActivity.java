package com.example.runapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class setNewActivity extends AppCompatActivity {
    Button back2;
    Button submit;
    TextView setTime;
    TextView setDistance;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_activity);
        back2 = findViewById(R.id.back2);
        submit = findViewById(R.id.submit);
        setTime = findViewById(R.id.setTime);
        setDistance = findViewById(R.id.setDistance);

        //get values from user (key, default value)
        SharedPreferences prefs = getSharedPreferences("MY_DATA", MODE_PRIVATE);
        Float time = prefs.getFloat("time", 0);
        Float distance = prefs.getFloat("distance", 0);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData(v);
            }
        });
        back2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(setNewActivity.this, FirstActivity.class);
                startActivity(i);
            }
        });
    }

    public void saveData(View view) {
        //get input text
        float time = Float.parseFloat(setTime.getText().toString());
        float distance = Float.parseFloat(setDistance.getText().toString());
        String username = LoginActivity.loggedInUser.username;

        // get the user's location and radius settings
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder().url("http://10.0.2.2:3000/users/" + LoginActivity.loggedInUser.username + "/location")
                .addHeader("Content-Type", "application/json").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        // Parse the response body as a JSON object
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(responseBody);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        JSONArray locationArray = jsonObject.getJSONObject("location").getJSONArray("coordinates");
                        float longitude = (float) locationArray.getDouble(0);
                        float latitude = (float) locationArray.getDouble(1);

                        // enter the new activity into the db
                        Request request = new Request.Builder()
                                .url("http://10.0.2.2:3000/newactivity/" + username + "/" + distance + "/" + time + "/" + longitude + "/" + latitude)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    // registration is successful, we go back to first activity
                                    Intent i = new Intent(setNewActivity.this, FirstActivity.class);
                                    startActivity(i);
                                }

                            }
                        });
                    } catch ( JSONException e) {
                        Log.e("error", "error");
                    }

                }
            }
        });
    }
}