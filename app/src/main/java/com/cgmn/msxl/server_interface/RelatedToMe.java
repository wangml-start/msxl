package com.cgmn.msxl.server_interface;

import java.util.Date;

public class RelatedToMe {
    Integer commentId;
    String userName;
    Date createdAt;
    byte[] smallCut;
    String myContent;
    String himContent;
    Integer myContentParent;
    Integer himContentParent;

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public byte[] getSmallCut() {
        return smallCut;
    }

    public void setSmallCut(byte[] smallCut) {
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
}
