package com.cgmn.msxl.data;

import java.util.List;

public class TradeStatistic {
    private Float fee;
    private Float pl;
    private Float baseAmt;
    private Float cashAmt;
    private Float maxRate;
    private Float minRate;
    private List<Float> list;

    public Float getFee() {
        return fee;
    }

    public void setFee(Float fee) {
        this.fee = fee;
    }

    public Float getPl() {
        return pl;
    }

    public void setPl(Float pl) {
        this.pl = pl;
    }

    public Float getBaseAmt() {
        return baseAmt;
    }

    public void setBaseAmt(Float baseAmt) {
        this.baseAmt = baseAmt;
    }

    public Float getCashAmt() {
        return cashAmt;
    }

    public void setCashAmt(Float cashAmt) {
        this.cashAmt = cashAmt;
    }

    public Float getMaxRate() {
        return maxRate;
    }

    public void setMaxRate(Float maxRate) {
        this.maxRate = maxRate;
    }

    public Float getMinRate() {
        return minRate;
    }

    public void setMinRate(Float minRate) {
        this.minRate = minRate;
    }

    public List<Float> getList() {
        return list;
    }

    public void setList(List<Float> list) {
        this.list = list;
    }
}
