package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_sign_up);

        int defaultStatusColor = getWindow().getStatusBarColor();

        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        passwordAgain = (EditText) findViewById(R.id.passwordAgain);
        firstname = (EditText) findViewById(R.id.name);
        lastname = (EditText) findViewById(R.id.lastname);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        b_send= (Button) findViewById(R.id.send);

        b_send.setOnClickListener(view -> {
            if(username.getText().toString().equals("") || password.getText().toString().equals("") ||
                firstname.getText().toString().equals("") || lastname.getText().toString().equals(""))
            {
                Toast.makeText(this, "Please do not leave spaces empty", Toast.LENGTH_SHORT).show();
            }
            else if(phone.getText().toString().length() != 11)
                Toast.makeText(this, "Please add 0 to start of your number and it should have 11 number", Toast.LENGTH_SHORT).show();
            else if(!emailControl(email)){
            }
            else if(password.getText().toString().equals(passwordAgain.getText().toString()))
            {
                if(control(new User(username.getText().toString(),password.getText().toString(),firstname.getText().toString(),
                        lastname.getText().toString(),email.getText().toString(),phone.getText().toString())))
                {
                    Toast.makeText(SignUpActivity.this, "Mail is sent to your email.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    Toast.makeText(SignUpActivity.this, "This username is already used!", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(SignUpActivity.this, "Passwords unmatched", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean emailControl(EditText email)
    {
        String EmailText = email.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        // onClick of button perform this simplest code.
        if (EmailText.matches(emailPattern))
        {
            Toast.makeText(getApplicationContext(),"valid email address",Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean control(User user)
    {
        boolean flag = true;
        try
        {
            File fileJson = new File(this.getFilesDir(),filename);
            if(fileJson.exists())
            {
                FileInputStream fis = this.openFileInput(filename);
                InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line =   reader.readLine();
                while(line != null)
                {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
                String contents = stringBuilder.toString();

                reader.close();
                inputStreamReader.close();
                fis.close();

                Gson gson = new Gson();
                Type typeOfUsers = new TypeToken<ArrayList<User>>() {}.getType();
                ArrayList<User> users = gson.fromJson(contents,typeOfUsers);

                for(User i : users)
                {
                    if(i.getUsername().equals(user.getUsername()))
                    {
                        flag = false;
                        break;
                    }
                }
                if(flag)
                {
                    users.add(user);
                    writeNewJson(users);
                }
                else
                {
                    return false;
                }
            }
            else
            {
                ArrayList<User> newUsers = new ArrayList<>();
                newUsers.add(user);
                writeNewJson(newUsers);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return flag;
    }

    public void writeNewJson(ArrayList<User> users)
    {
        try
        {
            Gson gson = new Gson();
            FileOutputStream fos = this.openFileOutput(filename, MODE_PRIVATE);
            fos.write(gson.toJson(users).getBytes());
            System.out.println(gson.toJson(users));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}