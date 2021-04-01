package com.cgmn.msxl.comp.k;

import android.graphics.*;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class KlinePaint {
    protected final Paint mDownPaint, mUpPaint,mimdlePaint;
    protected final Paint buyPaint, sellPaint,otPaint;
    protected final Paint mGridPaint, mLabelPaint;
    protected final Paint whitePaint;
    protected final Paint mk5Paint, mk10Paint, mk20Paint;
    private Paint.FontMetrics fontMetrics = new Paint.FontMetrics();

    protected KlineGroup data;
    protected List<KLinePoint> points = new ArrayList<>();
    protected List<PriceLinePoint> pricePts = new ArrayList<>();

    protected RectF contentRect = new RectF();
    protected RectF candleRect = new RectF();
    protected RectF barRect = new RectF();
    protected RectF macdRect = new RectF();

    protected int startx = 0;
    protected int endx = 0;

    public int getStartx() {
        return startx;
    }

    public void setStartx(int startx) {
        this.startx = startx;
    }

    public int getEndx() {
        return endx;
    }

    public void setEndx(int endx) {
        this.endx = endx;
    }


    public void setColors(int klineUpcolor, int klineDowncolor,
                          int ave5, int ave10, int ave20) {
        mUpPaint.setColor(klineUpcolor);
        mDownPaint.setColor(klineDowncolor);
        mk5Paint.setColor(ave5);
        mk10Paint.setColor(ave10);
        mk20Paint.setColor(ave20);
    }

    public KlinePaint() {
        mDownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDownPaint.setStyle(Paint.Style.FILL);
        mDownPaint.setStrokeWidth(KlineStyle.kLineBold);

        mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUpPaint.setStyle(Paint.Style.STROKE);
        mUpPaint.setStrokeWidth(KlineStyle.kLineBold);

        mimdlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mimdlePaint.setStyle(Paint.Style.STROKE);
        mimdlePaint.setStrokeWidth(KlineStyle.kLineBold);
        mimdlePaint.setColor(Color.GRAY);

        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setStrokeWidth(1f);
        mGridPaint.setColor(Color.GRAY);

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(Color.BLACK);

        mk5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mk10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mk20Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mk5Paint.setStyle(Paint.Style.FILL);
        mk10Paint.setStyle(Paint.Style.FILL);
        mk20Paint.setStyle(Paint.Style.FILL);
        mk5Paint.setStrokeWidth(KlineStyle.kLineBold);
        mk10Paint.setStrokeWidth(KlineStyle.kLineBold);
        mk20Paint.setStrokeWidth(KlineStyle.kLineBold);

        buyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        otPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        buyPaint.setStyle(Paint.Style.FILL);
        buyPaint.setStrokeWidth(KlineStyle.kLineBold);
        sellPaint.setStyle(Paint.Style.FILL);
        sellPaint.setStrokeWidth(KlineStyle.kLineBold);
        otPaint.setStyle(Paint.Style.FILL);
        otPaint.setStrokeWidth(KlineStyle.kLineBold);
        buyPaint.setStrokeWidth(KlineStyle.kLineBold);
        sellPaint.setStrokeWidth(KlineStyle.kLineBold);
        otPaint.setStrokeWidth(KlineStyle.kLineBold);
        buyPaint.setColor(Color.parseColor("#DA0505"));
        sellPaint.setColor(Color.parseColor("#1E90FF"));
        otPaint.setColor(Color.parseColor("#ED4713"));
        buyPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        sellPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        otPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

        whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(25);
        whitePaint.setStrokeWidth(2f);

    }

    public void setContentRect(RectF contentRect) {
        float macdTop  = contentRect.bottom - (1-KlineStyle.chartRate) * contentRect.height();
        float barTop = macdTop + KlineStyle.macdRate * contentRect.height();

        this.candleRect.set(contentRect.left, contentRect.top, contentRect.right, macdTop);
        this.macdRect.set(contentRect.left, macdTop, contentRect.right, barTop);
        this.barRect.set(contentRect.left, barTop, contentRect.right, contentRect.bottom);

        this.contentRect = contentRect;
    }

    public void setData(KlineGroup data) {
        startx = 0;
        endx = 0;
        this.data = data;
    }

    /**
     * draw everything.
     */
    public void render(Canvas canvas) {
        // CALC
        calcChartPoint();
        calcPriceLinePoint();
        // DRAW LABELS
        renderLabels(canvas);
//        canvas.drawLine(0, macdRect.top, macdRect.right, macdRect.top, mGridPaint);
//        canvas.drawLine(0, macdRect.bottom, macdRect.right, macdRect.bottom, mGridPaint);

        // set the entry draw area.
        canvas.save();
        canvas.clipRect(candleRect.left, candleRect.top, candleRect.right, contentRect.bottom);
        for (int i = 0; i < points.size(); i++) {
            KLinePoint entry = points.get(i);
            // draw step 0: set color

            // draw Kline
            Paint tempKpint = mimdlePaint;
            if(entry.state == 1){
                tempKpint= mUpPaint;
                canvas.drawRect(entry.openPt[0], entry.closePt[1], entry.openPt[0]+KlineStyle.kWidth, entry.openPt[1], tempKpint);
                canvas.drawLine(entry.highPt[0], entry.highPt[1], entry.highPt[0], entry.closePt[1], tempKpint);
                canvas.drawLine(entry.lowPt[0], entry.lowPt[1], entry.lowPt[0], entry.openPt[1], tempKpint);
            }else if(entry.state == -1){
                tempKpint = mDownPaint;
                canvas.drawRect(entry.openPt[0], entry.closePt[1], entry.openPt[0]+KlineStyle.kWidth, entry.openPt[1], tempKpint);
                canvas.drawLine(entry.highPt[0], entry.highPt[1], entry.lowPt[0], entry.lowPt[1], tempKpint);
            }else{
                canvas.drawRect(entry.openPt[0], entry.closePt[1], entry.openPt[0]+KlineStyle.kWidth, entry.openPt[1], tempKpint);
                canvas.drawLine(entry.highPt[0], entry.highPt[1], entry.lowPt[0], entry.lowPt[1], tempKpint);
            }

            // draw step 3: draw volume
            if(entry.state >= 0){
                canvas.drawRect(entry.volBPt[0], entry.volumePt[1],
                        entry.volBPt[0]+KlineStyle.kWidth, entry.volBPt[1],mUpPaint);
            }else{
                canvas.drawRect(entry.volBPt[0], entry.volumePt[1],
                        entry.volBPt[0]+KlineStyle.kWidth, entry.volBPt[1],mDownPaint);
            }

            //draw step 4: MA5 10 20
            if (i > 0) {
                KLinePoint preEntry = points.get(i - 1);
                if (preEntry.line5Pt != null) {
                    canvas.drawLine(preEntry.line5Pt[0], preEntry.line5Pt[1], entry.line5Pt[0], entry.line5Pt[1], mk5Paint);
                }
                if (preEntry.line10Pt != null) {
                    canvas.drawLine(preEntry.line10Pt[0], preEntry.line10Pt[1], entry.line10Pt[0], entry.line10Pt[1], mk10Paint);
                }
                if (preEntry.line20Pt != null) {
                    canvas.drawLine(preEntry.line20Pt[0], preEntry.line20Pt[1], entry.line20Pt[0], entry.line20Pt[1], mk20Paint);
                }
            }

            //draw step 4: MACD
            if (entry.macdState > 0) {
                canvas.drawLine(entry.macdPt[0],entry.macdPt[1], entry.macdBPt[0], entry.macdBPt[1], mUpPaint);
            } else {
                canvas.drawLine(entry.macdPt[0],entry.macdPt[1], entry.macdBPt[0], entry.macdBPt[1], mDownPaint);
            }

            if (i > 0) {
                KLinePoint preEntry = points.get(i - 1);
                canvas.drawLine(preEntry.difPt[0], preEntry.difPt[1], entry.difPt[0], entry.difPt[1], mk5Paint);
                canvas.drawLine(preEntry.deaPt[0], preEntry.deaPt[1], entry.deaPt[0], entry.deaPt[1], mk10Paint);
            }

            //draw char B\S\T
            if(entry.opChar != null){
                float dotHeight = 45;
                float textR = 16;
                float distance = 16;

                Paint chPint = otPaint;
                if("B".equals(entry.opChar)){
                    chPint = buyPaint;
                }else if("S" == entry.opChar){
                    chPint = sellPaint;
                }
                float textLength = whitePaint.measureText(entry.opChar);
                if("B".equals(entry.opChar)){
                    if(entry.lowPt[1]+dotHeight+textR*2 > entry.maxPt[1]){ //下方放不下
                        float starty = entry.highPt[1];
                        float[] startPt =new float[]{entry.highPt[0], starty-distance};
                        float[] endPt =new float[]{entry.highPt[0], starty-distance-dotHeight};
                        canvas.drawCircle(startPt[0],startPt[1],5f, chPint);
                        canvas.drawLine(startPt[0],startPt[1], endPt[0],endPt[1], chPint);
                        canvas.drawCircle(endPt[0], endPt[1]-textR, textR,chPint);
                        canvas.drawText(
                                entry.opChar,
                                endPt[0] - (textLength/2.0f),
                                endPt[1]-textR/2,
                                whitePaint);
                    }else{
                        float starty = entry.lowPt[1];
                        float[] startPt =new float[]{entry.lowPt[0], starty+distance};
                        float[] endPt =new float[]{entry.highPt[0], starty+distance+dotHeight};
                        canvas.drawCircle(startPt[0],startPt[1],5f, chPint);
                        canvas.drawLine(startPt[0],startPt[1], endPt[0], endPt[1], chPint);
                        canvas.drawCircle(endPt[0], endPt[1]+textR, textR,chPint);
                        canvas.drawText(
                                entry.opChar,
                                endPt[0] - (textLength/2.0f),
                                endPt[1]+textR+textR/2,
                                whitePaint);
                    }
                }else{
                    if(entry.highPt[1]-dotHeight-textR*2 > entry.minPt[1]){ //上边能放下时
                        float starty = entry.highPt[1];
                        float[] startPt =new float[]{entry.highPt[0], starty-distance};
                        float[] endPt =new float[]{entry.highPt[0], starty-distance-dotHeight};
                        canvas.drawCircle(startPt[0],startPt[1],5f, chPint);
                        canvas.drawLine(startPt[0],startPt[1], endPt[0], endPt[1], chPint);
                        canvas.drawCircle(endPt[0], endPt[1]-textR, textR,chPint);
                        canvas.drawText(
                                entry.opChar,
                                endPt[0] - (textLength/2.0f),
                                endPt[1]-textR/2,
                                whitePaint);
                    }else{
                        float starty = entry.lowPt[1];
                        float[] startPt =new float[]{entry.lowPt[0], starty+distance};
                        float[] endPt =new float[]{entry.lowPt[0], starty+distance+dotHeight};
                        canvas.drawCircle(startPt[0],startPt[1],5f, chPint);
                        canvas.drawLine(startPt[0],startPt[1], endPt[0], endPt[1], chPint);
                        canvas.drawCircle(endPt[0], endPt[1]+textR, textR,chPint);
                        canvas.drawText(
                                entry.opChar,
                                endPt[0] - (textLength/2.0f),
                                endPt[1]+textR+textR/2,
                                whitePaint);
                    }
                }

            }
        }
        canvas.restore();
    }

    /**
     * Draw x and y labels.
     */
    protected void renderLabels(Canvas canvas) {
        float textX = canvas.getWidth()-10;
        // DRAW Y LABELS
        mLabelPaint.setTextAlign(Paint.Align.RIGHT);
        mLabelPaint.setTextSize(30);
        mLabelPaint.getFontMetrics(fontMetrics);
        float moveY = 28;
        for(PriceLinePoint pt : pricePts){
            canvas.drawLine(pt.pstartPt[0],pt.pstartPt[1],pt.pendPt[0],pt.pendPt[1], mGridPaint);
            if(pricePts.indexOf(pt) == 4){
                moveY = -10;
            }
            canvas.drawText(pt.price,textX, pt.pendPt[1]+moveY, mLabelPaint);
        }
        canvas.drawText(
                "MACD",
                textX,
                barRect.height() * 3 / 5 + candleRect.bottom,
                mLabelPaint);

        canvas.drawText(
                "VOL",
                textX,
                macdRect.height() * 3 / 5 + barRect.top,
                mLabelPaint);

        canvas.save();
        canvas.restore();
    }

    /**
     * 计算坐标
     */
    protected void calcChartPoint(){
        if(endx <= 0){
            int visibleCount = (int) ((contentRect.width()-KlineStyle.rightWidth) / (KlineStyle.kWidth+KlineStyle.mBarSpace));
            startx = data.getNodes().size() - visibleCount;
            if (startx < 0) {
                startx = 0;
            }
            endx = data.getNodes().size();
        }
        data.calcMinMax(startx, endx);
        List<KLine> temList = data.getNodes().subList(startx, endx);

        float viewHeight = contentRect.height();
        float chartHeight = viewHeight * KlineStyle.chartRate;
        float volmeHeight = viewHeight * KlineStyle.volRate;
        float macdHeight = viewHeight * KlineStyle.macdRate;

        float half = KlineStyle.kWidth / 2;
        float ySpace = KlineStyle.chartSpace;
        float macdY = chartHeight+KlineStyle.macdSpace;
        float volumeY = chartHeight+macdHeight+KlineStyle.volSpace;

        float startx = 0;

        float priceDelta = data.mYMax - data.mYMin;
        float punit = (chartHeight-KlineStyle.chartSpace*2) / priceDelta;
        float vunit = (volmeHeight-KlineStyle.volSpace*2) / data.mMaxYVolume;
        float macdDelta = data.mYMaxMacd - data.mYMinMacd;
        float munit = (macdHeight-KlineStyle.macdSpace*2) / macdDelta;

        points.clear();
        for (int i = 0; i < temList.size(); i++) {
            KLine node = temList.get(i);
            startx = KlineStyle.mBarSpace * (i + 1) + KlineStyle.kWidth * i;

            KLinePoint pt = new KLinePoint();
            points.add(pt);
            pt.openPt = new float[]{ startx,(data.mYMax - node.open) * punit+ySpace};
            pt.closePt = new float[]{startx, (data.mYMax - node.close)*punit + ySpace};
            pt.highPt = new float[]{startx+half, (data.mYMax - node.high)*punit + ySpace};
            pt.lowPt = new float[]{startx+half, (data.mYMax - node.low)*punit + ySpace};
            pt.state = node.getState();
            pt.isOpen = node.isOpen;
            pt.volumePt = new float[]{startx, (data.mMaxYVolume - node.volume)*vunit + volumeY};
            pt.volBPt = new float[]{startx, chartHeight+macdHeight+volmeHeight};
            pt.difPt = new float[]{startx+half, (data.mYMaxMacd - node.dif)*munit + macdY};
            pt.deaPt = new float[]{startx+half, (data.mYMaxMacd - node.dea)*munit + macdY};
            pt.macdPt = new float[]{startx+half, (data.mYMaxMacd - node.macd)*munit + macdY};
            pt.macdBPt = new float[]{startx+half, data.mYMaxMacd*munit + macdY};
            pt.macdState = node.macd >= 0 ? 1 : -1;
            pt.maxPt = new float[]{startx+half, priceDelta*punit + ySpace};
            pt.minPt = new float[]{startx+half, ySpace};
            pt.opChar = node.ch;

            //均线
            if(node.avg5 != -1){
                pt.line5Pt = new float[]{startx+half, (data.mYMax - node.avg5)* punit+ySpace};
            }
            if(node.avg10 != -1){
                pt.line10Pt = new float[]{startx+half, (data.mYMax - node.avg10)* punit+ySpace};
            }
            if(node.avg20 != -1){
                pt.line20Pt = new float[]{startx+half, (data.mYMax - node.avg20)* punit+ySpace};
            }

            //价格线


        }
    }
    /**
     * 计算价格线
     */
    protected void calcPriceLinePoint(){
        pricePts.clear();;
        float viewHeight = contentRect.height();
        float chartHeight = viewHeight * KlineStyle.chartRate;
        float priceDelta = data.mYMax - data.mYMin;
        float punit = chartHeight / priceDelta;

        float pstartx = 5;
        float pendx = contentRect.width()-5;
        Integer count = 0;
        while(count < 5){
            float y = chartHeight * count / 4;
            String price = CommonUtil.formatNumer(data.mYMax - (y/punit));
            pricePts.add(new PriceLinePoint(new float[]{pstartx, y}, new float[]{pendx, y}, price));
            count++;
        }
    }
}

