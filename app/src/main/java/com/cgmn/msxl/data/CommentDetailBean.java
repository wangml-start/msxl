package com.cgmn.msxl.data;

import java.util.List;

/**
 * Created by moos on 2018/4/20.
 */

public class CommentDetailBean {
    private Integer id;
    private Integer no;
    private String nickName;
    private byte[] userLogo;
    private byte[] picture;
    private String content;
    private String approve;
    private Integer replyTotal;
    private String createDate;
    private Integer myApprove;
    private Integer myComment;
    private Integer userId;
    private String phone;
    private List<ReplyDetailBean> replyList;

    public CommentDetailBean(String nickName, String content, String createDate) {
        this.nickName = nickName;
        this.content = content;
        this.createDate = createDate;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getId() {
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

    public void setReplyTotal(Integer replyTotal) {
        this.replyTotal = replyTotal;
    }
    public Integer getReplyTotal() {
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

    public Integer getMyApprove() {
        return myApprove;
    }

    public void setMyApprove(Integer myApprove) {
        this.myApprove = myApprove;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    public Integer getMyComment() {
        return myComment;
    }

    public void setMyComment(Integer myComment) {
        this.myComment = myComment;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
