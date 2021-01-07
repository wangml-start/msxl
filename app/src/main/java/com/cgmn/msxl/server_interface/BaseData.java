package com.cgmn.msxl.server_interface;

import com.cgmn.msxl.data.*;

import java.util.List;
import java.util.Map;

public class BaseData {
    private Integer status;

    private Integer recordId;

    private String error;

    private User user;

    private KlineSet klineSet;

    private SettledAccount settledAccount;

    private TradeStatistic statistic;

    private MissModel missModel;

    List<RankEntity> rankList;
    List<ChatAddRecord> chatMain;
    List<ChatAddRecord> chatSub;

    private String filebyte;

    private List<Map<String, Object>> records;

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

    public String getFilebyte() {
        return filebyte;
    }

    public void setFilebyte(String filebyte) {
        this.filebyte = filebyte;
    }

    public List<Map<String, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public MissModel getMissModel() {
        return missModel;
    }

    public void setMissModel(MissModel missModel) {
        this.missModel = missModel;
    }

    public List<RankEntity> getRankList() {
        return rankList;
    }

    public void setRankList(List<RankEntity> rankList) {
        this.rankList = rankList;
    }

    public List<ChatAddRecord> getChatMain() {
        return chatMain;
    }

    public void setChatMain(List<ChatAddRecord> chatMain) {
        this.chatMain = chatMain;
    }

    public List<ChatAddRecord> getChatSub() {
        return chatSub;
    }

    public void setChatSub(List<ChatAddRecord> chatSub) {
        this.chatSub = chatSub;
    }
}
