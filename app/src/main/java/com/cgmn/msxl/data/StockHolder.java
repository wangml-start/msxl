package com.cgmn.msxl.data;

import com.cgmn.msxl.utils.CommonUtil;

import java.util.LinkedList;
import java.util.List;

public class StockHolder {
    private final String holdAmtLb = "总市值";
    private final String totAmtLb = "总资产";
    private final String head1 = "代码/市值";
    private final String head2 = "持仓/可用";
    private final String head3 = "现价/成本";
    private final String holdPlLb = "持仓盈亏";
    private final String avaiAmtLb = "可用";
    private final String exchangeLb = "手续费";
    private final String plLb = "盈亏";
    private final String rateLb = "盈亏率";

    public final static int LEADING_STRATEGY = 100;
    public final static int NORMAL_STRATEGY = 101;

    private final float brokerRate = 0.00025f;
    private final float yinhuaRate = 0.001f;
    private final float guohuRate = 0.001f;

    LinkedList<Trade> nodes;

    //交易相关参数
    private String code = "";

    private Float initTotAmt = 0.0f;

    private Float totAmt = 0.0f;
    private Float holdAmt = 0.0f;
    private Float holdPl = 0.0f;
    private Float avaiAmt = 0.0f;
    private Float costPrice = 0.0f;
    private Float price = 0.0f;
    private Float pl = 0.0f;

    private int holdShare = 0;
    private int avaiLabelShare = 0;

    private Float exchange = 0.0f;

    //1代表已结算
    private int settleStatus=0;

    //设置的模式
    List<SettingItem> modeList;
    //违反的原则
    private List<Integer> unprinciple;
    private int trainType;
    private int modelRecordId;

    public int getTrainType() {
        return trainType;
    }

    public void setTrainType(int trainType) {
        this.trainType = trainType;
    }

    public List<SettingItem> getModeList() {
        return modeList;
    }

    public void setModeList(List<SettingItem> modeList) {
        this.modeList = modeList;
    }

    public int getModelRecordId() {
        return modelRecordId;
    }

    public void setModelRecordId(int modelRecordId) {
        this.modelRecordId = modelRecordId;
    }

    public String getPlLb() {
        return plLb;
    }

    public String getHead1() {
        return head1;
    }

    public String getHead2() {
        return head2;
    }

    public String getHead3() {
        return head3;
    }

    public StockHolder() {
        nodes = new LinkedList();
    }

    public LinkedList<Trade> getNodes() {
        return nodes;
    }

    public void setNodes(LinkedList<Trade> nodes) {
        this.nodes = nodes;
    }

    public Float getTotAmt() {
        return totAmt;
    }

    public void setTotAmt(Float totAmt) {
        this.totAmt = totAmt;
    }

    public String getTotAmtLb() {
        return totAmtLb;
    }

    public String getRateLb() {
        return rateLb;
    }

    public Float getHoldAmt() {
        return holdAmt;
    }

    public void setHoldAmt(Float holdAmt) {
        this.holdAmt = holdAmt;
    }

    public String getHoldAmtLb() {
        return holdAmtLb;
    }


    public Float getHoldPl() {
        return holdPl;
    }

    public void setHoldPl(Float holdPl) {
        this.holdPl = holdPl;
    }

    public String getHoldPlLb() {
        return holdPlLb;
    }


    public Float getAvaiAmt() {
        return avaiAmt;
    }

    public void setAvaiAmt(Float avaiAmt) {
        this.avaiAmt = avaiAmt;
    }

    public String getAvaiAmtLb() {
        return avaiAmtLb;
    }

    public String getCode() {
        if(CommonUtil.isEmpty(code)){
            return code;
        }
        return String.format("%sXX", code.substring(0, 4));
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Float getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Float costPrice) {
        this.costPrice = costPrice;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getPl() {
        return pl;
    }

    public void setPl(Float pl) {
        this.pl = pl;
    }

    public int getHoldShare() {
        return holdShare;
    }

    public void setHoldShare(int holdShare) {
        this.holdShare = holdShare;
    }

    public int getAvaiLabelShare() {
        return avaiLabelShare;
    }

    public void setAvaiLabelShare(int avaiLabelShare) {
        this.avaiLabelShare = avaiLabelShare;
    }

    public Float getExchange() {
        return exchange;
    }

    public void setExchange(Float exchange) {
        this.exchange = exchange;
    }

    public String getPlRate() {
        return CommonUtil.formatPercent((price - costPrice) / costPrice);
    }

    public String getExchangeLb() {
        return exchangeLb;
    }

    public Float getInitTotAmt() {
        return initTotAmt;
    }

    public void setInitTotAmt(Float initTotAmt) {
        this.initTotAmt = initTotAmt;
    }

    public String getRealRate(){
        if(initTotAmt.intValue() == 0){
            return "";
        }
        return CommonUtil.formatPercent(pl/initTotAmt);
    }

    public void buyStock(int count, Float pri, String scode) {
        code = scode;
        holdShare += count;
        price = pri;
        float buyAmt = pri * count;
        float fee = 0;
        if(scode.startsWith("6")){
            float ghfee = count * guohuRate;
            if (ghfee < 1) {
                ghfee = 1;
            }
            fee += ghfee;
        }
        float brofee = buyAmt * brokerRate;
        if (brofee < 5) {
            brofee = 5;
        }
        fee += brofee;
        exchange += fee;
        totAmt -= fee;
        holdAmt += buyAmt;
        holdPl -= fee;
        avaiAmt = totAmt - holdAmt;
        costPrice = (holdAmt-holdPl) / holdShare;
        pl = totAmt - initTotAmt;
    }

    public void sellStock(int count, Float pri) {
        holdShare -= count;
        avaiLabelShare -= count;
        float sellAmt = price * count;
        float yhfee = sellAmt * yinhuaRate;
        if (yhfee < 5) {
            yhfee = 5;
        }
        float brofee = sellAmt * brokerRate;
        if (brofee < 5) {
            brofee = 5;
        }
        float fee = brofee + yhfee;
        exchange += fee;
        holdPl -= fee;
        holdAmt -= sellAmt;
        totAmt -= fee;
        avaiAmt = totAmt - holdAmt;
        if(holdShare == 0){
            costPrice = 0f;
            holdPl = 0.f;
        }else{
            costPrice = (holdAmt-holdPl) / holdShare;
        }
        pl = totAmt - initTotAmt;
    }

    public int getAvaiBuyCount(String price) {
        float priceNum = CommonUtil.castFloatFromString(price);
        float avaiAmt = getAvaiAmt();
        int avaiCount = (int) (avaiAmt / priceNum / 100);

        return 100 * avaiCount;
    }

    public int getAvaiBuyCount(String price, float percent) {
        float priceNum = CommonUtil.castFloatFromString(price);
        float avaiAmt = getAvaiAmt() * percent;
        int avaiCount = (int) (avaiAmt / priceNum / 100);

        return 100 * avaiCount;
    }

    public int getAvaiSellCount(float percent) {
        int avaiCount = (int) (avaiLabelShare  * percent / 100);
        return 100 * avaiCount;
    }

    public void nextPrice(float pr, Boolean changeDay){
        price = pr;
        if(changeDay){
            avaiLabelShare = holdShare;
        }
        holdPl = (price - costPrice) * holdShare;
        holdAmt = price * holdShare;
        totAmt = holdAmt + avaiAmt;
        pl = totAmt - initTotAmt;
    }

    public void settleTrading(Float price){
        if(CommonUtil.floatNumEqual(exchange , 0)){
            return;
        }
        if(settleStatus == 1){
            nodes.clear();
            return;

        }
        settleStatus = 1;
        if(holdShare > 0){
            sellStock(holdShare, price);
        }
        Trade trade = new Trade(code, pl, exchange,trainType,
                modelRecordId, unprinciple);
        nodes.addLast(trade);
    }

}