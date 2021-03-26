package com.cgmn.msxl.server_interface;

import java.util.List;

public class MarketData {
    List<String> tradeDate;
    List<String> points;
    Integer unlocked;
    String marketPrice;
    private List<TrendStock> trendList;

    private List<StockDetail> stocks;

    public List<String> getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(List<String> tradeDate) {
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

    public String getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(String marketPrice) {
        this.marketPrice = marketPrice;
    }
}
