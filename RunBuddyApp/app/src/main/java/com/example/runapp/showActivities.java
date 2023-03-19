package com.example.runapp;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class showActivities extends AppCompatActivity {
    Button back3;
    RecyclerView recyclerView;
    List<MyModel> myModelList;
    CustomAdapter customAdapter;
    Button notifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_activities);
        back3 = findViewById(R.id.back3);
        myModelList = new ArrayList<>();

        try {
            displayItems();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        //set notifications.
        notifyButton = findViewById(R.id.notifyButton);
        createNotificationChannel();

        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(showActivities.this, "notification set!", LENGTH_SHORT).show();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(showActivities.this, "notify");
                builder.setContentTitle("radius met!");
                builder.setContentText("a runner within your radius was found!");
                builder.setAutoCancel(true);
                builder.setSmallIcon(R.drawable.ic_launcher_background);
                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(showActivities.this);
                notificationManager.notify(1, builder.build());
            }
        });

        back3.setOnClickListener(v -> {
            Intent i = new Intent(showActivities.this, FirstActivity.class);
            startActivity(i);
        });
    }

    private void displayItems() throws IOException, JSONException {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        fetchActivities();
        customAdapter = new CustomAdapter(this, myModelList);
        recyclerView.setAdapter(customAdapter);
    }

    private void fetchActivities() {
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
                        JSONObject jsonObject = new JSONObject(responseBody);

                        // Extract the radius and location data from the JSON object
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");
                        float radius = (float) jsonObject.getDouble("radius");
                        JSONArray locationArray = jsonObject.getJSONObject("location").getJSONArray("coordinates");
                        float longitude = (float) locationArray.getDouble(0);
                        float latitude = (float) locationArray.getDouble(1);

                        //get the activities within the user's radius
                        Request activitiesRequest = new Request.Builder()
                                .url("http://10.0.2.2:3000/users/"+ LoginActivity.loggedInUser.username + "/activities/location/" + longitude + "/" + latitude + "/" + radius)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        String finalFirstName = firstName;
                        String finalLastName = lastName;
                        client.newCall(activitiesRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    String responseBody = response.body().string();
                                    try {
                                        JSONArray jsonActivities = new JSONArray(responseBody);

                                        // Iterate through the JSON array and extract the activity data
                                        List<List> activities = new ArrayList<>();
                                        for (int i = 0; i < jsonActivities.length(); i++) {
                                            JSONObject jsonActivity = jsonActivities.getJSONObject(i);

                                            // Extract the activity data from the JSON object
                                            String username = jsonActivity.getString("username");
                                            int distance = jsonActivity.getInt("distance");
                                            int time = jsonActivity.getInt("time");

                                            // Create an activity object and add it to the list
                                            myModelList.add(new MyModel(finalFirstName + " " + finalLastName, time, distance));
                                        }
                                        for (int i = 0; i < myModelList.size(); i++) {
                                            System.out.println(myModelList.get(i));
                                        }
                                    } catch (JSONException e) {
                                        Log.e("parsingError", "error parsing json", e);
                                    }
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("parsingError", "error parsing json", e);
                    }
                }
            }
        });
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notify";
            String description = "snooze every time radius is met";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notify", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}