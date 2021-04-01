package com.cgmn.msxl.server_interface;

import com.cgmn.msxl.data.VipItem;

import java.util.Date;
import java.util.List;

public class VipDataSetting {
    public static String DAY_TYPE = "DAY";
    public static String MONTH_TYPE = "MONTH";

    private List<VipItem> list;
    private Float rate;
    private Integer status;
    private String error;
    private String level;
    private String expireDate;

    private Integer upGradeAmt;


    public List<VipItem> getList() {
        return list;
    }

    public void setList(List<VipItem> list) {
        this.list = list;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public Integer getUpGradeAmt() {
        return upGradeAmt;
    }

    public void setUpGradeAmt(Integer upGradeAmt) {
        this.upGradeAmt = upGradeAmt;
    }
}
