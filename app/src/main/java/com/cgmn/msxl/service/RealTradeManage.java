package com.cgmn.msxl.service;

import com.cgmn.msxl.comp.k.KLine;
import com.cgmn.msxl.comp.k.KlineGroup;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;

import java.text.DecimalFormat;

public class RealTradeManage {
    public static String OPEN = "OPEN";
    public static String CLOSE = "CLOSE";

    private KlineSet klineset;
    DecimalFormat format = new DecimalFormat("0.00%");

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
            if(num < 0 && currentK.getUpRate().indexOf("-") != -1){
                currentK.setOpenrate(format.format(num));
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
}
