package com.cgmn.msxl.data;

import java.util.Date;
import java.util.List;

public class Trade {

    private String stockCode;
    private Date tradeDate;
    private float pl;
    private float fee;
    private List<Integer> unprinciple;
    private int trainType;
    private int modelRecordId;


    public Trade(String code, float pl, float exchange,
                 int type, int model, List<Integer> list){
        this.tradeDate = new Date();
        this.stockCode = code;
        this.pl = pl;
        this.fee = exchange;
        this.trainType = type;
        this.modelRecordId = model;
        this.unprinciple = list;
    }


}
