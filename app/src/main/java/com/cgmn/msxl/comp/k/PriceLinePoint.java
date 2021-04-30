package com.cgmn.msxl.comp.k;

public class PriceLinePoint {
    public float[] pstartPt;
    public float[] pendPt;
    public String price;

    public PriceLinePoint(float[] sPt, float[] ePt, String p){
        pstartPt = sPt;
        pendPt = ePt;
        price = p;
    }
}
