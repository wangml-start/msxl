package com.cgmn.msxl.server_interface;

import java.util.List;

public class KlineSet {

    private String stockName;

    private String stocCode;

    private String startDate;

    private String endDate;

    private List<StockDetail> initList;

    private List<StockDetail> futureList;

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getStocCode() {
        return stocCode;
    }

    public void setStocCode(String stocCode) {
        this.stocCode = stocCode;
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
