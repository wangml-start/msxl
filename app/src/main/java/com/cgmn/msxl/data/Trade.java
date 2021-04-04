package com.cgmn.msxl.data;

import com.cgmn.msxl.utils.CommonUtil;

import java.util.Date;
import java.util.List;

public class Trade {

    private String stockCode;
    private String tradeDate;
    private float pl;
    private float fee;
    private List<Integer> unprinciple;
    private int trainType;
    private int modelRecordId;


    public Trade(String code, float pl, float exchange,
                 int type, int model, List<Integer> list){
        this.tradeDate = CommonUtil.formartTimeString(new Date(), null);
        this.stockCode = code;
        this.pl = pl;
        this.fee = exchange;
        this.trainType = type;
        this.modelRecordId = model;
        this.unprinciple = list;
    }


}
