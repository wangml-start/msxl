package com.cgmn.msxl.server_interface;

import java.util.Date;
import java.util.List;

public class ChatAddRecord {
    private Integer id;

    private Integer creatorId;

    private Integer approve;

    private Integer picId;

    private Integer parentId;

    private Integer replyId;

    private Integer replyUserId;

    private Integer viewIt;

    private String createdAt;

    private Date updatedAt;

    private Integer status;

    private String content;

    private String bitContent;
    private String userName;
    private String replayUname;
    private String phone;
    private String smallCut;

    private Integer myApprove;
    private Integer myComment;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getApprove() {
        return approve;
    }

    public void setApprove(Integer approve) {
        this.approve = approve;
    }

    public Integer getPicId() {
        return picId;
    }

    public void setPicId(Integer picId) {
        this.picId = picId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getReplyId() {
        return replyId;
    }

    public void setReplyId(Integer replyId) {
        this.replyId = replyId;
    }

    public Integer getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(Integer replyUserId) {
        this.replyUserId = replyUserId;
    }

    public Integer getViewIt() {
        return viewIt;
    }

    public void setViewIt(Integer viewIt) {
        this.viewIt = viewIt;
    }


    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getBitContent() {
        return bitContent;
    }

    public void setBitContent(String bitContent) {
        this.bitContent = bitContent;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSmallCut() {
        return smallCut;
    }

    public void setSmallCut(String smallCut) {
        this.smallCut = smallCut;
    }

    public Integer getMyApprove() {
        return myApprove;
    }

    public void setMyApprove(Integer myApprove) {
        this.myApprove = myApprove;
    }

    public Integer getMyComment() {
        return myComment;
    }

    public void setMyComment(Integer myComment) {
        this.myComment = myComment;
    }

    public String getReplayUname() {
        return replayUname;
    }

    public void setReplayUname(String replayUname) {
        this.replayUname = replayUname;
    }
}
