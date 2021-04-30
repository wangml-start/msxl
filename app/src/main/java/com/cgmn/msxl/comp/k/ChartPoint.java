package com.cgmn.msxl.comp.k;

public class ChartPoint {
    public float x;
    public float y;

    public String opChar=null;

    public float opX;
    public float opY;

    public ChartPoint(){
        x=0f;
        y=0f;
    }
    public ChartPoint(float xx, float yy){
        x=xx;
        y=yy;
    }

    public void setOpInfo(float xx, float yy, String op){
        opX=xx;
        opY=yy;
        opChar = op;
    }
}
