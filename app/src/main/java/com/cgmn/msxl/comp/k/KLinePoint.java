package com.cgmn.msxl.comp.k;

public class KLinePoint {
    float[] openPt;
    float[] closePt;
    float[] highPt;
    float[] lowPt;
    Integer state;
    Boolean isOpen;

    float[] volumePt;
    float[] volBPt;

    float[] line5Pt;
    float[] line10Pt;
    float[] line20Pt;

    float[] difPt;
    float[] deaPt;
    float[] macdPt;
    float[] macdBPt;
    Integer macdState;

    float[] maxPt;
    float[] minPt;

    String opChar;
}

class PriceLinePoint {
    float[] pstartPt;
    float[] pendPt;
    String price;

    public PriceLinePoint(float[] sPt, float[] ePt, String p){
        pstartPt = sPt;
        pendPt = ePt;
        price = p;
    }
}
