package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    EditText userName;
    EditText password;
    TextView attemptCount;
    Button b_login;
    Button b_signup;

    int attemptCountRemaining;

    String filename = "User.json";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        attemptCountRemaining = 3;

        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        attemptCount = (TextView) findViewById(R.id.attemptCount);
        b_login = (Button) findViewById(R.id.login);
        b_signup = (Button) findViewById(R.id.signup);

        attemptCount.setText("Remaining attempt count: " + attemptCountRemaining);

        b_login.setOnClickListener(view -> {

            if (control(userName.getText().toString(), password.getText().toString()))
            {
                Toast.makeText(LoginActivity.this, "Correct", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,MainListActivity.class));
            }
            else
            {
                attemptCountRemaining--;
                attemptCount.setText("Remaining attempt: " + attemptCountRemaining);
                Toast.makeText(LoginActivity.this, "Incorrect", Toast.LENGTH_SHORT).show();
            }
            if (attemptCountRemaining == 0)
            {
                startActivity(new Intent(view.getContext(), SignUpActivity.class));
            }
        });

        b_signup.setOnClickListener(view -> startActivity(new Intent(view.getContext(), SignUpActivity.class)));
    }

    public boolean control(String username, String password) {
        boolean flag = false;
        File fileJson = new File(this.getFilesDir(),filename);
        if(fileJson.exists()) {
            try {
                FileInputStream fis = this.openFileInput(filename);
                InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
                String contents = stringBuilder.toString();

                reader.close();
                inputStreamReader.close();
                fis.close();

                Gson gson = new Gson();
                Type typeOfUsers = new TypeToken<ArrayList<User>>() {}.getType();
                ArrayList<User> users = gson.fromJson(contents, typeOfUsers);

                for (User i : users) {
                    if (i.getUsername().equals(username) && i.getPassword().equals(password)) {
                        flag = true;
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return flag;
        }
        else
        {
            return false;
        }
    }

}