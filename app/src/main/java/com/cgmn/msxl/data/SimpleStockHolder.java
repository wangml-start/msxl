package com.cgmn.msxl.data;

import com.cgmn.msxl.utils.CommonUtil;

public class SimpleStockHolder {
    //交易相关参数
    private String code = "";
    private Float costPrice = 0.0f;
    private Float price = 0.0f;
    private Float rate=0f;
    private Integer holdDay = 0;

    public void resetHolder(){
        code = "";
        costPrice = 0.0f;
        price = 0.0f;
        rate= 0f;
        holdDay = 0;
    }

    public void onBuy(float pr, String stockCode){
        costPrice = pr;
        code = stockCode;
    }

    public void nextPrice(float pr, Boolean ChangeDay) {
        price = pr;
        if(costPrice > 0){
            rate = (price - costPrice)/costPrice;
            if(ChangeDay){
                holdDay += 1;
            }
        }
    }


    public String getCode() {
        return code;
    }

    public Float getRate() {
        return rate;
    }

    public String getRateStr() {
        if(rate != 0){
            return CommonUtil.formatPercent(rate);
        }else{
            return "0%";
        }
    }

    public Boolean canSell(){
        return holdDay>=1;
    }

    public boolean needSettle(){
        return costPrice>0;
    }

    public Float getCostPrice() {
        return costPrice;
    }
}