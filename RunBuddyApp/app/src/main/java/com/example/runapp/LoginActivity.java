package com.example.runapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class LoginActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    Button registerButton;
    Button loginButton;
    TextView errorMessage;
    public static UserInfo loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        errorMessage = findViewById(R.id.errorMessage);

        loginButton.setOnClickListener(v -> checkUsername());
        registerButton.setOnClickListener(view -> {
            Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(i);
        });
    }

    boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    void checkUsername() {
        boolean isValid = true;
        if (isEmpty(username)) {
            username.setError("You must enter username to login!");
            isValid = false;
        } else {
            if (!isEmail(username)) {
                username.setError("Enter valid email!");
                isValid = false;
            }
        }
        if (isEmpty(password)) {
            password.setError("You must enter password to login!");
            isValid = false;
        } else {
            if (password.getText().toString().length() < 4) {
                password.setError("Password must be at least 4 chars long!");
                isValid = false;
            }
        }
        //check that email and password match a user.
        if (isValid) {
            String usernameValue = username.getText().toString();
            String passwordValue = password.getText().toString();
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"username\":\"" + usernameValue + "\",\"password\":\"" + passwordValue + "\"}");
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/login")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        // login is successful, we open the first activity
                        loggedInUser = new UserInfo(usernameValue);
                        Intent i = new Intent(LoginActivity.this, FirstActivity.class);
                        startActivity(i);
                    }
                }
        });

}}

}

