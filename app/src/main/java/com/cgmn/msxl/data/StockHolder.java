package com.cgmn.msxl.data;

import com.cgmn.msxl.utils.CommonUtil;

import java.util.ArrayList;
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
    public final static int KZZ_STRATEGY = 102;
    public final static int RANK_SUMMARY = 99;
    public final static int EARNING_CURVE_SUMMARY = 98;

    private final float brokerRate = 0.00025f;
    private final float yinhuaRate = 0.001f;
    private final float guohuRate = 0.001f;
    private final float kzzRate = (float) (0.5/10000.0);

    LinkedList<Trade> nodes;

    //交易相关参数
    private String code = "";
    private String stackName = "";

    private Double initTotAmt = 0.0;

    private Double totAmt = 0.0;
    private Double holdAmt = 0.0;
    private Double holdPl = 0.0;
    private Double avaiAmt = 0.0;
    private Double costPrice = 0.0;
    private Double price = 0.0;
    private Double pl = 0.0;

    private long holdShare = 0;
    private long avaiLabelShare = 0;

    private Double exchange = 0.0;
    //1代表已结算
    private int settleStatus = 0;
    private int holdDays = 0;

    //设置的模式
    List<SettingItem> modeList;
    //违反的原则
    private List<Integer> unprinciple;
    private int trainType;
    private int modelRecordId=1;

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

    public int getHoldDays() {
        return holdDays;
    }

    public void whenNextDay() {
        if(holdShare>0){
            holdDays += 1;
        }
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

    public Double getTotAmt() {
        return totAmt;
    }

    public void setTotAmt(Double totAmt) {
        this.totAmt = totAmt;
    }

    public String getTotAmtLb() {
        return totAmtLb;
    }

    public String getRateLb() {
        return rateLb;
    }

    public Double getHoldAmt() {
        return holdAmt;
    }

    public void setHoldAmt(Double holdAmt) {
        this.holdAmt = holdAmt;
    }

    public String getHoldAmtLb() {
        return holdAmtLb;
    }


    public Double getHoldPl() {
        return holdPl;
    }

    public void setHoldPl(Double holdPl) {
        this.holdPl = holdPl;
    }

    public String getHoldPlLb() {
        return holdPlLb;
    }


    public Double getAvaiAmt() {
        return avaiAmt;
    }

    public void setAvaiAmt(Double avaiAmt) {
        this.avaiAmt = avaiAmt;
    }

    public String getAvaiAmtLb() {
        return avaiAmtLb;
    }

    public String getCode() {
        if (CommonUtil.isEmpty(code)) {
            return code;
        }
        return String.format("%sXX", code.substring(0, 4));
    }

    public String getStackName() {
        return stackName;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPl() {
        return pl;
    }

    public void setPl(Double pl) {
        this.pl = pl;
    }

    public long getHoldShare() {
        return holdShare;
    }

    public void setHoldShare(int holdShare) {
        this.holdShare = holdShare;
    }

    public long getAvaiLabelShare() {
        return avaiLabelShare;
    }

    public void setAvaiLabelShare(long avaiLabelShare) {
        this.avaiLabelShare = avaiLabelShare;
    }

    public Double getExchange() {
        return exchange;
    }

    public void setExchange(Double exchange) {
        this.exchange = exchange;
    }

    public String getPlRate() {
        return CommonUtil.formatPercent((price - costPrice) / costPrice);
    }

    public float getPlRateNum() {
        return ((Double)((price - costPrice) / costPrice)).floatValue();
    }

    public String getExchangeLb() {
        return exchangeLb;
    }

    public Double getInitTotAmt() {
        return initTotAmt;
    }

    public void setInitTotAmt(Double initTotAmt) {
        this.initTotAmt = initTotAmt;
    }

    public String getRealRate() {
        if (initTotAmt.intValue() == 0) {
            return "";
        }
        return CommonUtil.formatPercent(pl / initTotAmt);
    }

    public void buyStock(long count, Float pri, String scode, String sName) {
        code = scode;
        stackName = sName;
        holdShare += count;
        price = Double.valueOf(pri);
        Double buyAmt = price * count;
        Double fee = 0.0;
        if (scode.startsWith("6")) {
            float ghfee = count * guohuRate;
            if (ghfee < 1) {
                ghfee = 1;
            }
            fee += ghfee;
        }
        Double brofee = buyAmt * brokerRate;
        if (brofee < 5) {
            brofee = 5.0;
        }
        fee += brofee;
        exchange += fee;
        totAmt -= fee;
        holdAmt += buyAmt;
        holdPl -= fee;
        avaiAmt = totAmt - holdAmt;
        costPrice = (holdAmt - holdPl) / holdShare;
        pl = totAmt - initTotAmt;
    }

    public void buyKzz(long count, Float pri, String scode, String sName){
        code = scode;
        stackName = sName;
        holdShare += count;
        avaiLabelShare += count;
        price = Double.valueOf(pri);
        Double buyAmt = price * count;
        Double fee = 0.0;
        Double brofee = buyAmt * kzzRate;
        fee += brofee;
        exchange += fee;
        totAmt -= fee;
        holdAmt += buyAmt;
        holdPl -= fee;
        avaiAmt = totAmt - holdAmt;
        costPrice = (holdAmt - holdPl) / holdShare;
        pl = totAmt - initTotAmt;
    }

    public void sellStock(long count, Float pri) {
        holdShare -= count;
        avaiLabelShare -= count;
        price = Double.valueOf(pri);
        Double sellAmt = price * count;
        Double yhfee = sellAmt * yinhuaRate;
        if (yhfee < 5) {
            yhfee = 5.0;
        }
        Double brofee = sellAmt * brokerRate;
        if (brofee < 5) {
            brofee = 5.0;
        }
        float ghfee=0f;
        if (!CommonUtil.isEmpty(code) && code.startsWith("6")) {
            ghfee = count * guohuRate;
            if (ghfee < 1) {
                ghfee = 1;
            }
        }
        Double fee = brofee + yhfee + ghfee;
        exchange += fee;
        holdPl -= fee;
        holdAmt -= sellAmt;
        totAmt -= fee;
        avaiAmt = totAmt - holdAmt;
        if (holdShare == 0) {
            holdAmt = 0.0;
            costPrice = 0.0;
            holdPl = 0.0;
            holdDays = 0;
        } else {
            costPrice = (holdAmt - holdPl) / holdShare;
        }
        pl = totAmt - initTotAmt;
    }

    public void sellKzz(long count, Float pri) {
        holdShare -= count;
        avaiLabelShare -= count;
        price = Double.valueOf(pri);
        Double sellAmt = price * count;
        Double fee = sellAmt * kzzRate;
        exchange += fee;
        holdPl -= fee;
        holdAmt -= sellAmt;
        totAmt -= fee;
        avaiAmt = totAmt - holdAmt;
        if (holdShare == 0) {
            costPrice = 0.0;
            holdPl = 0.0;
            holdDays = 0;
            holdAmt = 0.0;
        } else {
            costPrice = (holdAmt - holdPl) / holdShare;
        }
        pl = totAmt - initTotAmt;
    }

    public long getAvaiBuyCount(String price, String p_code) {
        float priceNum = CommonUtil.castFloatFromString(price);
        Double avaiAmt = getAvaiAmt();
        int uom = 100;
        if(CommonUtil.isKzz(p_code)){
            if(p_code.startsWith("11")) {
                uom = 10;
            }
        }
        long avaiCount = (long) (avaiAmt / priceNum / uom);
        return uom * avaiCount;
    }

    public long getAvaiBuyCount(String price, Float percent, String p_code) {
        long total = getAvaiBuyCount(price, p_code);
        float priceNum = CommonUtil.castFloatFromString(price);
        Double avaiAmt = getAvaiAmt() * percent;
        int uom = 100;
        if(CommonUtil.isKzz(p_code)){
            if(p_code.startsWith("11")){
                uom = 10;
            }
        }
        long avaiCount = (long) (avaiAmt / priceNum / uom);
        if(avaiCount == 0 && total > 0){
            avaiCount = 1;
        }
        return uom * avaiCount;
    }

    public long getAvaiSellCount(Float percent) {
        int uom = 100;
        if(CommonUtil.isKzz(code)){
            if(code.startsWith("11")){
                uom = 10;
            }
        }
        long avaiCount = (long) (avaiLabelShare * Double.valueOf(percent) / uom);
        return uom * avaiCount;
    }

    public void nextPrice(Float pr, Boolean changeDay) {
        price = Double.valueOf(pr);
        if (changeDay) {
            avaiLabelShare = holdShare;
        }
        holdPl = (price - costPrice) * holdShare;
        holdAmt = price * holdShare;
        totAmt = holdAmt + avaiAmt;
        pl = totAmt - initTotAmt;
    }

    public void settleTrading(Float price) {
        if (CommonUtil.floatNumEqual(exchange, 0.0)) {
            return;
        }
        if (settleStatus == 1) {
            nodes.clear();
            return;

        }
        settleStatus = 1;
        if (holdShare > 0) {
            sellStock(holdShare, price);
        }
        Trade trade = new Trade(code, pl.floatValue(), exchange.floatValue(), trainType,
                modelRecordId, unprinciple);
        nodes.addLast(trade);
    }

    public boolean exists(int type) {
        if (unprinciple == null) {
            unprinciple = new ArrayList<>();
        }
        return unprinciple.contains(type);
    }

    public void addOverType(int type) {
        if (unprinciple == null) {
            unprinciple = new ArrayList<>();
        }
        unprinciple.add(type);
    }


    public float getStartRate(float amt){
        return ((Double)(Double.valueOf(amt) / totAmt)).floatValue();
    }

    public float getHoldRate(){
        return ((Double) (holdAmt / totAmt)).floatValue();
    }

    public float getLossRate(){
        return ((Double)((price - costPrice) / costPrice)).floatValue();
    }

}