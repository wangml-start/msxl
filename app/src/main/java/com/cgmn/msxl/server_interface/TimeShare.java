package com.cgmn.msxl.server_interface;

public class TimeShare {
    private String stockCode;
    private String timeStr;

    private Float price;

    private Integer volume;

    private Integer amt;

    private String charType;
    private Float lastClose;
    private String tradeDate;

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode == null ? null : stockCode.trim();
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr == null ? null : timeStr.trim();
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getAmt() {
        return amt;
    }

    public void setAmt(Integer amt) {
        this.amt = amt;
    }

    public String getCharType() {
        return charType;
    }

    public void setCharType(String charType) {
        this.charType = charType == null ? null : charType.trim();
    }

    public Float getLastClose() {
        return lastClose;
    }

    public void setLastClose(Float lastClose) {
        this.lastClose = lastClose;
    }

    public String getShowTime(){
        String hour = timeStr.substring(0, 2);
        String munite = timeStr.substring(2, 4);

        return String.format("%s:%s", hour, munite);
    }
}