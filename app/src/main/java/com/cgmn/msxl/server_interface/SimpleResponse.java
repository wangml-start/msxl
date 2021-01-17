package com.cgmn.msxl.server_interface;

import java.util.List;

public class SimpleResponse {
    Integer status;
    String error;



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


}
