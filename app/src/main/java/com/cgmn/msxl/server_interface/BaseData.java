package com.cgmn.msxl.server_interface;

import com.cgmn.msxl.data.User;

public class BaseData {
    private Integer status;

    private String error;

    private User user;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
