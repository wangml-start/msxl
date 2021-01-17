package com.cgmn.msxl.server_interface;

import java.util.Date;

public class RelatedToMe {
    Integer commentId;
    String userName;
    String createdAt;
    String smallCut;
    String myContent;
    String himContent;
    Integer myContentParent;
    Integer himContentParent;
    Integer userId;
    Integer myApprove;

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSmallCut() {
        return smallCut;
    }

    public void setSmallCut(String smallCut) {
        this.smallCut = smallCut;
    }

    public String getMyContent() {
        return myContent;
    }

    public void setMyContent(String myContent) {
        this.myContent = myContent;
    }

    public String getHimContent() {
        return himContent;
    }

    public void setHimContent(String himContent) {
        this.himContent = himContent;
    }

    public Integer getMyContentParent() {
        return myContentParent;
    }

    public void setMyContentParent(Integer myContentParent) {
        this.myContentParent = myContentParent;
    }

    public Integer getHimContentParent() {
        return himContentParent;
    }

    public void setHimContentParent(Integer himContentParent) {
        this.himContentParent = himContentParent;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMyApprove() {
        return myApprove;
    }

    public void setMyApprove(Integer myApprove) {
        this.myApprove = myApprove;
    }
}
