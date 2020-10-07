package com.cgmn.msxl.service;

import com.cgmn.msxl.comp.k.KLine;
import com.cgmn.msxl.comp.k.KlineGroup;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.utils.CommonUtil;


public class RealTradeManage {
    public final static String OPEN = "OPEN";
    public final static String CLOSE = "CLOSE";

    private KlineSet klineset;

    KlineGroup group;

    private StockDetail lastK;
    private StockDetail currentK;
    private String kStatus;

    public KlineSet getKlineset() {
        return klineset;
    }

    public void setKlineset(KlineSet klineset) {
        this.klineset = klineset;
    }

    public StockDetail getCurrentK() {
        return currentK;
    }

    public KlineGroup getGroup() {
        return group;
    }

    public void fixInitDate() {
        this.group = new KlineGroup();
        for (int i = 0; i < klineset.getInitList().size(); i++) {
            StockDetail detail = klineset.getInitList().get(i);
            group.addKline(new KLine(
                    detail.getHigh(),
                    detail.getLow(),
                    detail.getStart(),
                    detail.getEnd(),
                    detail.getVol()));
            lastK = detail;
        }
    }

    public boolean showNextOpen() {
        fixInitDate();
        if (klineset.getFutureList().size() > 0) {
            currentK = klineset.getFutureList().get(0);
            float num = (currentK.getStart() - lastK.getEnd()) / lastK.getEnd();
            if(num > -0.2){
                currentK.setOpenrate(CommonUtil.formatPercent(num));
            }else{
                currentK.setOpenrate("--");
            }
            group.addKline(new KLine(currentK.getStart()));

            kStatus = OPEN;
            setDatas();
            return true;
        }

        return false;
    }

    public void showNextClose() {
        fixInitDate();
        group.addKline(new KLine(
                currentK.getHigh(),
                currentK.getLow(),
                currentK.getStart(),
                currentK.getEnd(),
                currentK.getVol()
        ));
        klineset.getFutureList().remove(0);
        klineset.getInitList().add(currentK);
        setDatas();
        lastK = currentK;
        kStatus = CLOSE;
    }

    public void setDatas() {
        group.calcAverageMACD();
    }

    public int getLeftDay() {
        return klineset.getFutureList().size() - 1;
    }

    public String getkStatus() {
        return kStatus;
    }

    public boolean openWithUp(){
        return  (currentK.getStart() - lastK.getEnd()) > 0;
    }

    public boolean openWithDown(){
        return  (currentK.getStart() - lastK.getEnd()) < 0;
    }


    public String getCurenPrice(){
        if(kStatus.equals(OPEN)){
            return CommonUtil.formatNumer(currentK.getStart());
        }else{
            return CommonUtil.formatNumer(currentK.getEnd());
        }
    }

    /**
     * 0 可以买入
     * -10 跌停
     * 10 涨停
     * @return
     */
    public int canTradingStatus(){
        int flag = 0;
        if(kStatus == OPEN){
            String rate = currentK.getOpenrate().replace("%", "").replaceAll("_", "");
            if(CommonUtil.isEmpty(rate)){
                return flag;
            }
            if(Double.valueOf(rate) > 9.85){
                flag = 10;
            }
            if(Double.valueOf(rate) < -9.85){
                flag = -10;
            }
        }else{
            String rate = currentK.getUpRate().replace("%", "");
            if(CommonUtil.isEmpty(rate)){
                return flag;
            }
            if(Double.valueOf(rate) > 9.85 && currentK.getHigh() - currentK.getEnd() < 0.001){
                flag = 10;
            }
            if(Double.valueOf(rate) < -9.85 && currentK.getEnd() - currentK.getLow()  < 0.001){
                flag = -10;
            }
        }
        return flag;
    }

}
