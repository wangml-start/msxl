package com.cgmn.msxl.server_interface;

import com.cgmn.msxl.utils.CommonUtil;

import java.util.Date;

public class StockDetail {
    private Integer id;

    private String stackCode;

    private Date quoteDate;

    private Float start;

    private Float end;

    private Float upDown;

    private String upRate;

    private Float low;

    private Float high;

    private Integer vol;

    private Float amount;

    private String exchageRate;

    private String openrate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStackCode() {
        return stackCode;
    }

    public void setStackCode(String stackCode) {
        this.stackCode = stackCode == null ? null : stackCode.trim();
    }

    public Date getQuoteDate() {
        return quoteDate;
    }

    public void setQuoteDate(Date quoteDate) {
        this.quoteDate = quoteDate;
    }

    public Float getStart() {
        return start;
    }

    public void setStart(Float start) {
        this.start = start;
    }

    public Float getEnd() {
        return end;
    }

    public void setEnd(Float end) {
        this.end = end;
    }

    public Float getUpDown() {
        return upDown;
    }

    public void setUpDown(Float upDown) {
        this.upDown = upDown;
    }

    public String getUpRate() {
        return upRate;
    }

    public void setUpRate(String upRate) {
        this.upRate = upRate == null ? null : upRate.trim();
    }

    public Float getLow() {
        return low;
    }

    public void setLow(Float low) {
        this.low = low;
    }

    public Float getHigh() {
        return high;
    }

    public void setHigh(Float high) {
        this.high = high;
    }

    public Integer getVol() {
        return vol;
    }

    public void setVol(Integer vol) {
        this.vol = vol;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getExchageRate() {
        if(exchageRate!=null){
            Double num = Double.valueOf(exchageRate.replace("%", "")) / 100;
            return CommonUtil.formatPercent(num);
        }
        return "";
    }

    public void setExchageRate(String exchageRate) {
        this.exchageRate = exchageRate == null ? null : exchageRate.trim();
    }

    public String getOpenrate() {
        return openrate;
    }

    public void setOpenrate(String openrate) {
        this.openrate = openrate;
    }
}