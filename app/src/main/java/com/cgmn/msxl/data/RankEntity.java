package com.cgmn.msxl.data;

public class RankEntity {

    private Integer userId;
    private Integer myRank;
    private Double stAmt;
    private Double rate;
    private String phone;
    private String userName;
    private String smallCut;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMyRank() {
        return myRank;
    }

    public void setMyRank(Integer myRank) {
        this.myRank = myRank;
    }

    public Double getStAmt() {
        return stAmt;
    }

    public void setStAmt(Double stAmt) {
        this.stAmt = stAmt;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSmallCut() {
        return smallCut;
    }

    public void setSmallCut(String smallCut) {
        this.smallCut = smallCut;
    }
}
