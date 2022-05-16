package com.cgmn.msxl.comp.k.time;

public class CMinute {
    float price;
    float opPrice;
    Integer vol;
    String opChar=null;
    String timeMinute; //hour-minute 便于查找

    public CMinute(float p, String str, Integer volume){
        price = p;
        timeMinute = str;
        vol = volume;
    }
}
