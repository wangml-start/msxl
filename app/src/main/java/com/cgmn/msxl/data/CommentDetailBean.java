package com.cgmn.msxl.data;

import java.util.List;

/**
 * Created by moos on 2018/4/20.
 */

public class CommentDetailBean {
    private int id;
    private int no;
    private String nickName;
    private byte[] userLogo;
    private byte[] picture;
    private String content;
    private String approve;
    private int replyTotal;
    private String createDate;
    private int myApprove;
    private int myComment;
    private int userId;
    private List<ReplyDetailBean> replyList;

    public CommentDetailBean(String nickName, String content, String createDate) {
        this.nickName = nickName;
        this.content = content;
        this.createDate = createDate;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public String getNickName() {
        return nickName;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }

    public void setReplyTotal(int replyTotal) {
        this.replyTotal = replyTotal;
    }
    public int getReplyTotal() {
        return replyTotal;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    public String getCreateDate() {
        return createDate;
    }

    public void setReplyList(List<ReplyDetailBean> replyList) {
        this.replyList = replyList;
    }
    public List<ReplyDetailBean> getReplyList() {
        return replyList;
    }

    public byte[] getUserLogo() {
        return userLogo;
    }

    public void setUserLogo(byte[] userLogo) {
        this.userLogo = userLogo;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getApprove() {
        if(approve == null){
            return "0";
        }else{
            return approve;
        }
    }

    public void setApprove(String approve) {
        this.approve = approve;
    }

    public int getMyApprove() {
        return myApprove;
    }

    public void setMyApprove(int myApprove) {
        this.myApprove = myApprove;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getMyComment() {
        return myComment;
    }

    public void setMyComment(int myComment) {
        this.myComment = myComment;
    }
}
