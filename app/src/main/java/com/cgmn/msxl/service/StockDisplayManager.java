package com.cgmn.msxl.service;

import com.cgmn.msxl.comp.k.KLine;
import com.cgmn.msxl.comp.k.KlineGroup;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.List;

public class StockDisplayManager {
    KlineGroup group;
    private StockDetail lastK;
    private List<StockDetail> stocks;

    public void resetManager(){
        group = null;
        stocks = null;
        lastK = null;
    }

    public void repacklineNode() {
        if(!CommonUtil.isEmpty(stocks)){
            this.group = new KlineGroup();
            for (int i = 0; i < stocks.size(); i++) {
                StockDetail detail = stocks.get(i);
                float last = 0;
                if(lastK != null){
                    last = lastK.getEnd();
                }
                group.addKline(new KLine(
                        detail.getHigh(),
                        detail.getLow(),
                        detail.getStart(),
                        detail.getEnd(),
                        last,
                        detail.getVol()));
                lastK = detail;
            }
        }
        setDatas();
    }

    public void setDatas() {
        group.calcAverageMACD();
    }

    public KlineGroup getGroup() {
        return group;
    }

    public void setGroup(KlineGroup group) {
        this.group = group;
    }

    public List<StockDetail> getStocks() {
        return stocks;
    }

    public void setStocks(List<StockDetail> stocks) {
        this.stocks = stocks;
    }
}
