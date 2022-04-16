package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainListActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    EditText passwordAgain;
    EditText firstname;
    EditText lastname;
    EditText email;
    EditText phone;

    Button b_send;

    String filename = "User.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        passwordAgain = (EditText) findViewById(R.id.passwordAgain);
        firstname = (EditText) findViewById(R.id.name);
        lastname = (EditText) findViewById(R.id.lastname);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        b_send= (Button) findViewById(R.id.send);

        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(password.getText().toString().equals(passwordAgain.getText().toString()))
                {
                    if(control(username.getText().toString()))
                    {

                    }
                }
                else
                {
                    Toast.makeText(MainListActivity.this, "Passwords unmatched", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean control(String username)
    {
        try
        {
            /*FileInputStream fis = this.openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fis,StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line =   reader.readLine();
            while(line != null)
            {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
            String contents = stringBuilder.toString();
            */

            Reader reader = Files.newBufferedReader(Paths.get("user.json"));
            Gson gson = new Gson();
            ArrayList<?> map = gson.fromJson(reader, ArrayList.class);
            for(Object i : map)
            {
                ((User) i).getUsername().equals(username);
            }

            //reader.close();
            //inputStreamReader.close();
            //fis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return true;
    }
}