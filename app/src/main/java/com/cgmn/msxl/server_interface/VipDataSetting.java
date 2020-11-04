package com.cgmn.msxl.server_interface;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class VipDataSetting {
    public static String DAY_TYPE = "DAY";
    public static String MONTH_TYPE = "MONTH";

    private List<Map<String, Object>> daySetting;
    private List<Map<String, Object>> monthSetting;
    private Float rate;
    private Integer status;
    private String error;
    private String level;
    private Date expireDate;


    public List<Map<String, Object>> getDaySetting() {
        return daySetting;
    }

    public void setDaySetting(List<Map<String, Object>> daySetting) {
        this.daySetting = daySetting;
    }

    public List<Map<String, Object>> getMonthSetting() {
        return monthSetting;
    }

    public void setMonthSetting(List<Map<String, Object>> monthSetting) {
        this.monthSetting = monthSetting;
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

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public List<Map<String, Object>> getList(){
        List<Map<String, Object>> temp = new ArrayList<>();
        temp.addAll(daySetting);
        temp.addAll(monthSetting);

        return temp;
    }
}
