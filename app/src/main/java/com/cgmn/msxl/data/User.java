package com.cgmn.msxl.data;

public class User {
    int id;
    String phone;
    String password;
    String token;
    String userName;
    int gender;
    String signature;
    String imageCut;


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getSignature() {
        return signature;
    }

    public String getImageCut() {
        return imageCut;
    }

    public void setImageCut(String imageCut) {
        this.imageCut = imageCut;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
