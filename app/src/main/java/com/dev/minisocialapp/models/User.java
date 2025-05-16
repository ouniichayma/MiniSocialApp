package com.dev.minisocialapp.models;


public class User {
    private String fullName;
    private String email;
    private String dateOfBirth;
    private String phone;
    private String profileImageUrl;
    private String uid;

    public User() {
        // Nécessaire pour Firebase
    }

    public User(String fullName, String email, String dateOfBirth, String phone, String profileImageUrl, String uid) {
        this.fullName = fullName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.uid = uid;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getUid() { return uid; }

    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setUid(String uid) { this.uid = uid; }
}
