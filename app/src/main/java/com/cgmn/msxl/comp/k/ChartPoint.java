package com.cgmn.msxl.comp.k;

public class ChartPoint {
    public float x;
    public float y;

    public String opChar=null;

    public float opX;
    public float opY;

    public float volSY;
    public float volY;

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

    public void setVolPos(float sy, float yy){
        volSY=sy;
        volY=yy;
    }
}
