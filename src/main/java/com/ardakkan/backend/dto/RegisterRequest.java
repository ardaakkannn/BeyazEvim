package com.ardakkan.backend.dto;


public class RegisterRequest {
    private String name;
    private String password;
    private String email;

    // Getter ve Setter'lar
    public String getName() {
        return name;
    }
    public void setName(String username) {
        this.name = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
