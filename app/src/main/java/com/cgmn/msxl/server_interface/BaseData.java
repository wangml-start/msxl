package com.cgmn.msxl.server_interface;

import com.cgmn.msxl.data.SettledAccount;
import com.cgmn.msxl.data.TradeStatistic;
import com.cgmn.msxl.data.User;

public class BaseData {
    private Integer status;

    private String error;

    private User user;

    private KlineSet klineSet;

    private SettledAccount settledAccount;

    private TradeStatistic statistic;

    private byte[] filebyte;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public KlineSet getKLineSet() {
        return klineSet;
    }

    public void setKLineSet(KlineSet klineSet) {
        this.klineSet = klineSet;
    }

    public SettledAccount getSettledAccount() {
        return settledAccount;
    }

    public void setSettledAccount(SettledAccount settledAccount) {
        this.settledAccount = settledAccount;
    }

    public TradeStatistic getStatistic() {
        return statistic;
    }

    public void setStatistic(TradeStatistic statistic) {
        this.statistic = statistic;
    }

    public byte[] getFilebyte() {
        return filebyte;
    }

    public void setFilebyte(byte[] filebyte) {
        this.filebyte = filebyte;
    }
}
