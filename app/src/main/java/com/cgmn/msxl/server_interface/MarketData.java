package com.cgmn.msxl.server_interface;

import java.util.List;

public class MarketData {
    String tradeDate;
    List<String> points;
    Integer unlocked;
    private List<TrendStock> trendList;

    private List<StockDetail> stocks;

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public List<String> getPoints() {
        return points;
    }

    public void setPoints(List<String> points) {
        this.points = points;
    }

    public Integer getUnlocked() {
        return unlocked;
    }

    public void setUnlocked(Integer unlocked) {
        this.unlocked = unlocked;
    }

    public List<TrendStock> getTrendList() {
        return trendList;
    }

    public void setTrendList(List<TrendStock> trendList) {
        this.trendList = trendList;
    }

    public List<StockDetail> getStocks() {
        return stocks;
    }

    public void setStocks(List<StockDetail> stocks) {
        this.stocks = stocks;
    }
}
