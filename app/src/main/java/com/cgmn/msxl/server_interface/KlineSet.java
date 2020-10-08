package com.cgmn.msxl.server_interface;

import java.util.List;

public class KlineSet {

    private float totalAmount = 100000.00f;

    private String stockName;

    private String stockCode;

    private String startDate;

    private String endDate;

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    private List<StockDetail> initList;

    private List<StockDetail> futureList;

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<StockDetail> getInitList() {
        return initList;
    }

    public void setInitList(List<StockDetail> initList) {
        this.initList = initList;
    }

    public List<StockDetail> getFutureList() {
        return futureList;
    }

    public void setFutureList(List<StockDetail> futureList) {
        this.futureList = futureList;
    }
}
