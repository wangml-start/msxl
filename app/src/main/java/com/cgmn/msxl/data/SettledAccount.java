package com.cgmn.msxl.data;

public class SettledAccount {
    private Integer id;

    private Integer userId;

    private Float principal;

    private Float pl;

    private Float cashAmt;

    private Float fee;

    private Integer vipPermission;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Float getPrincipal() {
        return principal;
    }

    public void setPrincipal(Float principal) {
        this.principal = principal;
    }

    public Float getPl() {
        return pl;
    }

    public void setPl(Float pl) {
        this.pl = pl;
    }

    public Float getCashAmt() {
        return cashAmt;
    }

    public void setCashAmt(Float cashAmt) {
        this.cashAmt = cashAmt;
    }

    public Float getFee() {
        return fee;
    }

    public void setFee(Float fee) {
        this.fee = fee;
    }

    public Integer getVipPermission() {
        return vipPermission;
    }

    public void setVipPermission(Integer vipPermission) {
        this.vipPermission = vipPermission;
    }
}