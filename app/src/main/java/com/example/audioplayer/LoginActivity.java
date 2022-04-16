package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText userName;
    EditText password;
    TextView attemptCount;
    Button b_login;
    Button b_signup;

    int attemptCountRemaining;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        attemptCountRemaining = 3;

        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        attemptCount = (TextView) findViewById(R.id.attemptcount);
        b_login = (Button) findViewById(R.id.login);
        b_signup = (Button) findViewById(R.id.signup);

        attemptCount.setText("Remaining attempt count: " + attemptCountRemaining);

        b_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = userName.getText().toString();
                String password = LoginActivity.this.password.getText().toString();
                String[] usernames;

                if(username.equals("Admin") && password.equals("Admin"))
                {
                    Toast.makeText(LoginActivity.this, "Correct", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    attemptCountRemaining--;
                    attemptCount.setText("Remaining attempt: " + attemptCountRemaining);
                    Toast.makeText(LoginActivity.this, "Incorrect", Toast.LENGTH_SHORT).show();
                }
                if(attemptCountRemaining == 0)
                {
                    startActivity(new Intent(view.getContext(),SignUpActivity.class));
                }
            }
        });

        b_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(),SignUpActivity.class));
            }
        });
    }
}