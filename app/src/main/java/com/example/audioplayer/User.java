package com.example.audioplayer;

public class User {
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;

    public String getUsername() {
        return username;
    }

    public User(String username, String password, String firstname, String lastname, String email, String phone) {
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phone = phone;
    }

}
