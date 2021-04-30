package com.cgmn.msxl.comp.k.time;

public class CMinute {
    float price;
    float opPrice;
    String opChar=null;
    String timeMinute; //hour-minute 便于查找

    public CMinute(float p, String str){
        price = p;
        timeMinute = str;
    }
}
