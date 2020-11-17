package com.cgmn.msxl.server_interface;

public class SimpleResponse {
    Integer status;
    String error;

    Integer approveToMe=0;
    Integer commentToMe=0;

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

    public Integer getApproveToMe() {
        return approveToMe;
    }

    public void setApproveToMe(Integer approveToMe) {
        this.approveToMe = approveToMe;
    }

    public Integer getCommentToMe() {
        return commentToMe;
    }

    public void setCommentToMe(Integer commentToMe) {
        this.commentToMe = commentToMe;
    }
}
