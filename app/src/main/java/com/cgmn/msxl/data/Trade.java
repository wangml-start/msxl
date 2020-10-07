package com.cgmn.msxl.data;

import java.util.Date;
import java.util.List;

public class Trade {

    private String code;
    private Date TradeDate;
    private Long time;
    private float pl;
    private float exchange;
    private List<Integer> unprinciple;

    public Trade(String code, float pl, float exchange, List<Integer> list){
        this.TradeDate = new Date();
        this.time = TradeDate.getTime();
        this.code = code;
        this.pl = pl;
        this.exchange = exchange;
        this.unprinciple = list;
    }


}
