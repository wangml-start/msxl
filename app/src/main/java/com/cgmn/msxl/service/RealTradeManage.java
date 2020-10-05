package com.cgmn.msxl.service;

import com.cgmn.msxl.comp.k.KLine;
import com.cgmn.msxl.comp.k.KlineGroup;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;

import java.text.DecimalFormat;

public class RealTradeManage {

    private KlineSet klineset;
    DecimalFormat format = new DecimalFormat("0.00%");

    KlineGroup group;

    private StockDetail lastK;
    private StockDetail currentK;


    public RealTradeManage() {
        this.group = new KlineGroup();
    }

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

    public void fixInitDate(){
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

    public boolean showNextOpen(){
        if(klineset.getFutureList().size() > 0){
            currentK = klineset.getFutureList().get(0);
            currentK.setOpenrate(format.format(currentK.getStart()/lastK.getEnd()));
            group.addKline(new KLine(currentK.getStart()));
            return true;
        }

        return false;
    }

    public void showNextClose(){
        group.getNodes().removeLast();
        group.addKline(new KLine(
                currentK.getHigh(),
                currentK.getLow(),
                currentK.getStart(),
                currentK.getEnd(),
                currentK.getVol()
        ));
        klineset.getFutureList().remove(0);
    }

    public void setDatas(){
        group.calcAverageMACD();
    }

    public int getLeftDay(){
        return klineset.getFutureList().size();
    }

}
