package com.cgmn.msxl.data;

/**
 * Created by moos on 2018/4/20.
 */

public class ReplyDetailBean {
    private String replayFrom;
    private String replayTo;
    private String userLogo;
    private Integer id;
    private Integer no;
    private String commentId;
    private String content;
    private String status;
    private String createDate;
    private Integer userId;
    private Integer replayUserId;
    private byte[] picture;

    public ReplyDetailBean(String content) {
        this.content = content;
    }

    public String getReplayFrom() {
        return replayFrom;
    }

    public void setReplayFrom(String replayFrom) {
        this.replayFrom = replayFrom;
    }

    public String getReplayTo() {
        return replayTo;
    }

    public void setReplayTo(String replayTo) {
        this.replayTo = replayTo;
    }

    public void setUserLogo(String userLogo) {
        this.userLogo = userLogo;
    }
    public String getUserLogo() {
        return userLogo;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return id;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    public String getCommentId() {
        return commentId;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    public String getCreateDate() {
        return createDate;
    }

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public Integer getReplayUserId() {
        return replayUserId;
    }

    public void setReplayUserId(Integer replayUserId) {
        this.replayUserId = replayUserId;
    }

    public String getReplayUserName(Integer userId){
        if(userId == this.getUserId()){
            return this.getReplayFrom();
        }else{
            return this.getReplayTo();
        }
    }
}
