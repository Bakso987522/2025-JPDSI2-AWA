package com.example.fieldcard.dto.auth;

public class AuthenticationResponse {
    private String token;
    private String name;

    public AuthenticationResponse() {}

    public AuthenticationResponse(String token, String name) {
        this.token = token;
        this.name = name;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public  String getName() { return name; }
    public void setName(String name) { this.name = name; }
}