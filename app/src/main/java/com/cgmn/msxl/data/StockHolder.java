package com.cgmn.msxl.data;

import java.text.DecimalFormat;
import java.util.LinkedList;

public class StockHolder {
    private final String holdAmtLb = "总市值";
    private final String totAmtLb = "总资产";
    private final String head1 = "代码/市值";
    private final String head2 = "持仓/可用";
    private final String head3 = "现价/成本";
    private final String head4 = "盈亏";
    private final String holdPlLb = "持仓盈亏";
    private final String avaiAmtLb = "可用";
    private final String exchangeLb = "手续费： ";
    private final DecimalFormat format = new DecimalFormat("#0.00");
    private final DecimalFormat formatP = new DecimalFormat("0.00%");

    LinkedList<Trade> nodes;

    private String code = "0023XX";

    private String totAmt = "200000.00";
    private String holdAmt = "50000.00";
    private String holdPl = "-5000.00";
    private String avaiAmt = "150000.00";

    private float costPrice =30.51f;
    private float price = 22.51f;


    private float pl = 55555;
    private int holdShare = 20000;
    private int avaiLabelShare = 10000;

    private float exchange = 300.25f;

    public String getHead1() {
        return head1;
    }

    public String getHead2() {
        return head2;
    }

    public String getHead3() {
        return head3;
    }

    public String getHead4() {
        return head4;
    }

    public StockHolder(){
        nodes = new LinkedList();
    }

    public LinkedList<Trade> getNodes() {
        return nodes;
    }

    public void setNodes(LinkedList<Trade> nodes) {
        this.nodes = nodes;
    }

    public String getTotAmt() {
        return totAmt;
    }

    public void setTotAmt(String totAmt) {
        this.totAmt = totAmt;
    }

    public String getTotAmtLb() {
        return totAmtLb;
    }


    public String getHoldAmt() {
        return holdAmt;
    }

    public void setHoldAmt(String holdAmt) {
        this.holdAmt = holdAmt;
    }

    public String getHoldAmtLb() {
        return holdAmtLb;
    }


    public String getHoldPl() {
        return holdPl;
    }

    public void setHoldPl(String holdPl) {
        this.holdPl = holdPl;
    }

    public String getHoldPlLb() {
        return holdPlLb;
    }


    public String getAvaiAmt() {
        return avaiAmt;
    }

    public void setAvaiAmt(String avaiAmt) {
        this.avaiAmt = avaiAmt;
    }

    public String getAvaiAmtLb() {
        return avaiAmtLb;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCostPrice() {
        return format.format(costPrice);
    }

    public void setCostPrice(float costPrice) {
        this.costPrice = costPrice;
    }

    public String getPrice() {
        return format.format(price);
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getPl() {
        return pl;
    }

    public void setPl(float pl) {
        this.pl = pl;
    }

    public String getHoldShare() {
        return format.format(holdShare);
    }

    public void setHoldShare(int holdShare) {
        this.holdShare = holdShare;
    }

    public String getAvaiLabelShare() {
        return format.format(avaiLabelShare);
    }

    public void setAvaiLabelShare(int avaiLabelShare) {
        this.avaiLabelShare = avaiLabelShare;
    }

    public String getExchange() {
        return format.format(exchange);
    }

    public void setExchange(float exchange) {
        this.exchange = exchange;
    }

    public String getPlRate(){
        return formatP.format((price-costPrice)/costPrice);
    }

    public String getExchangeLb() {
        return exchangeLb;
    }
}
