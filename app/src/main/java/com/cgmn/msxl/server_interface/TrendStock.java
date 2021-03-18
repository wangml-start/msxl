package com.cgmn.msxl.server_interface;

public class TrendStock {
    private String stackCode;
    private String stackName;
    private String end;
    private String upRate;
    private String exchageRate;
    private Float upDown;

    public String getStackCode() {
        return stackCode;
    }

    public void setStackCode(String stackCode) {
        this.stackCode = stackCode;
    }

    public String getStackName() {
        return stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getUpRate() {
        return upRate;
    }

    public void setUpRate(String upRate) {
        this.upRate = upRate;
    }

    public String getExchageRate() {
        return exchageRate;
    }

    public void setExchageRate(String exchageRate) {
        this.exchageRate = exchageRate;
    }

    public Float getUpDown() {
        return upDown;
    }

    public void setUpDown(Float upDown) {
        this.upDown = upDown;
    }
}