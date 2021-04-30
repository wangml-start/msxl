package com.cgmn.msxl.comp.k.time;

import android.graphics.*;
import com.cgmn.msxl.comp.k.*;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class TimeSharePaint {
    protected final Paint buyPaint, sellPaint, avgPaint,timePaint, mDownPaint, mUpPaint;
    protected final Paint mGridPaint,mdotGridPaint, mLabelPaint;
    private Paint.FontMetrics fontMetrics = new Paint.FontMetrics();

    protected TimeShareGroup data;
    protected List<PriceLinePoint> xLines = new ArrayList<>();
    protected List<PriceLinePoint> yLines = new ArrayList<>();
    protected List<PriceLinePoint> textPrices = new ArrayList<>();
    protected List<ChartPoint> timePoints = new ArrayList<>();
    protected List<ChartPoint> solidPts = new ArrayList<>();

    protected RectF contentRect = new RectF();
    protected RectF candleRect = new RectF();
    protected RectF barRect = new RectF();

    private float textSize = KlineStyle.kTextSize*0.85f;

    protected float priceDelta;

    protected Boolean showDetail = true;

    protected float margin = 2.5f * KlineStyle.pxScaleRate, padding = 1.2f * KlineStyle.pxScaleRate;


    public void setShowDetail(Boolean showDetail) {
        this.showDetail = showDetail;
    }

    public TimeSharePaint() {
        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStrokeWidth(KlineStyle.gridLine);
        mGridPaint.setColor(Color.GRAY);

        avgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        avgPaint.setStyle(Paint.Style.FILL);
        avgPaint.setStrokeWidth(KlineStyle.kLineBold);
        avgPaint.setColor(Color.parseColor("#FFC800"));

        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setStyle(Paint.Style.FILL);
        timePaint.setStrokeWidth(KlineStyle.kLineBold);
        timePaint.setColor(Color.parseColor("#577DAF"));

        mdotGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mdotGridPaint.setStrokeWidth(KlineStyle.gridLine);
        mdotGridPaint.setColor(Color.BLACK);
        mdotGridPaint.setPathEffect(new DashPathEffect(new float[]{16, 16}, 0));

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setTextSize(textSize);
        mLabelPaint.setColor(Color.BLACK);

        buyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        buyPaint.setStyle(Paint.Style.FILL);
        buyPaint.setStrokeWidth(KlineStyle.kLineBold);
        sellPaint.setStyle(Paint.Style.FILL);
        sellPaint.setStrokeWidth(KlineStyle.kLineBold);
        buyPaint.setStrokeWidth(KlineStyle.kLineBold);
        sellPaint.setStrokeWidth(KlineStyle.kLineBold);
        buyPaint.setColor(Color.parseColor("#DA0505"));
        sellPaint.setColor(Color.parseColor("#1E90FF"));
        buyPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        sellPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

        mDownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDownPaint.setStyle(Paint.Style.FILL);
        mDownPaint.setTextSize(textSize);
        mDownPaint.setStrokeWidth(KlineStyle.gridLine);
        mDownPaint.setColor(Color.parseColor("#05870A"));

        mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUpPaint.setStyle(Paint.Style.FILL);
        mUpPaint.setTextSize(textSize);
        mUpPaint.setStrokeWidth(KlineStyle.gridLine);
        mUpPaint.setColor(Color.parseColor("#CC0000"));
    }

    public void setContentRect(RectF contentRect) {
        float width = contentRect.width()-margin*2;
        if(showDetail){
            float candleWidth = width * 0.8f;
            float startX = contentRect.left+margin;
            this.candleRect.set(startX, contentRect.top+padding, contentRect.left+candleWidth, contentRect.bottom-padding);
            float barstartX = contentRect.left+margin+candleWidth+padding;
            this.barRect.set(barstartX, contentRect.top+padding, contentRect.right-margin, contentRect.bottom-padding);
        }else{
            this.candleRect.set(contentRect.left+margin, contentRect.top+padding, contentRect.right-margin, contentRect.bottom-padding);
        }
        this.contentRect = contentRect;
    }

    public void setData(TimeShareGroup data) {
        this.data = data;
    }

    /**
     * draw everything.
     */
    public void render(Canvas canvas) {
        canvas.clipRect(contentRect.left+margin, contentRect.top+padding, contentRect.right-margin, contentRect.bottom-padding);
        // CALC
        calcChartPoint();
        //表格线
        calcGridLinePoint();
        //价格文本
        calcTextPricePoint();

        drawGridLines(canvas);
        // DRAW LABELS
        renderLabels(canvas);

        // set the entry draw area.
        canvas.save();
        Integer endIndex = timePoints.size();
        for (int i = 0; i < endIndex; i++) {
            ChartPoint timePt = timePoints.get(i);
            ChartPoint solidPt = timePoints.get(i);
            if (i > 0) {
                ChartPoint ltimePt = timePoints.get(i-1);
                ChartPoint lsolidPt = timePoints.get(i-1);

                canvas.drawLine(ltimePt.x, ltimePt.y, timePt.x, timePt.y, timePaint);
                canvas.drawLine(lsolidPt.x, lsolidPt.y, solidPt.x, solidPt.y, avgPaint);
            }


            //draw char B\S\T
            if(timePt.opChar != null){
                float dotR = 1.5f*KlineStyle.pxScaleRate;

                Paint chPint = null;
                if("B".equals(timePt.opChar)){
                    chPint = buyPaint;
                }else{
                    chPint = sellPaint;
                }
                canvas.drawCircle(timePt.opX,timePt.opY,dotR, chPint);
            }
        }
        canvas.restore();
    }

    /**
     * Draw x and y labels.
     */
    protected void renderLabels(Canvas canvas) {
        // DRAW Y LABELS
        mLabelPaint.setTextAlign(Paint.Align.LEFT);
        mLabelPaint.getFontMetrics(fontMetrics);

        canvas.drawText(textPrices.get(0).price, textPrices.get(0).pstartPt[0], textPrices.get(0).pstartPt[1], mUpPaint);
        canvas.drawText(textPrices.get(1).price, textPrices.get(1).pstartPt[0], textPrices.get(1).pstartPt[1], mUpPaint);

        canvas.drawText(textPrices.get(2).price, textPrices.get(2).pstartPt[0], textPrices.get(2).pstartPt[1], mLabelPaint);

        canvas.drawText(textPrices.get(3).price, textPrices.get(3).pstartPt[0], textPrices.get(3).pstartPt[1], mDownPaint);
        canvas.drawText(textPrices.get(4).price, textPrices.get(4).pstartPt[0], textPrices.get(4).pstartPt[1], mDownPaint);

        canvas.save();
        canvas.restore();
    }

    protected void drawGridLines(Canvas canvas){
        //x Line
        Integer index = 0;
        for(PriceLinePoint line : xLines){
            if(index == 2){
                canvas.drawLine(line.pstartPt[0],line.pstartPt[1],line.pendPt[0],line.pendPt[1], mdotGridPaint);
            }else{
                canvas.drawLine(line.pstartPt[0],line.pstartPt[1],line.pendPt[0],line.pendPt[1], mGridPaint);
            }
            index++;
        }

        //y Line
        for(PriceLinePoint line : yLines){
            canvas.drawLine(line.pstartPt[0],line.pstartPt[1],line.pendPt[0],line.pendPt[1], mGridPaint);
        }
    }

    /**
     * 计算坐标
     */
    protected void calcChartPoint(){
        Integer count = 4 * 60;
        float chartHeight = candleRect.height();
        priceDelta = data.calExtremeNum();
        float delta = priceDelta*2;
        float punit = chartHeight / delta;
        float distanceX = candleRect.width() / count;
        float mYMax = priceDelta+data.lastClosePrice;

        timePoints.clear();
        solidPts.clear();
        Integer endIndex = data.solidPrices.size();
        //分时线
        for (int i = 0; i < endIndex; i++) {
            CMinute node = data.timePrices.get(i);
            CMinute snode = data.solidPrices.get(i);

            float startx = distanceX * i;
            ChartPoint timeItem = new ChartPoint(startx, (mYMax - node.price) * punit);
            timePoints.add(timeItem);
            solidPts.add(new ChartPoint(startx, (mYMax - snode.price) * punit));
            if(node.opChar != null){
                timeItem.setOpInfo(startx, (mYMax - node.opPrice) * punit, node.opChar);
            }
        }
    }
    /**
     * 计算表格线
     */
    protected void calcGridLinePoint(){
        xLines.clear();
        float chartHeight = candleRect.height();
        float pstartx = 0;
        float pendx = candleRect.width();
        Integer count = 0;

        while(count < 5){
            float y = chartHeight * count / 4;
            if(y==0){
                y += 1.5f*KlineStyle.pxScaleRate;;
            }
            xLines.add(new PriceLinePoint(new float[]{pstartx, y}, new float[]{pendx, y}, null));
            count++;
        }

        yLines.clear();
        float viewWidth = candleRect.width();
        Integer countY = 0;
        while(countY < 5){
            float x = viewWidth * countY / 4;
            if(x==0){
                x += 3f*KlineStyle.pxScaleRate;;
            }
            yLines.add(new PriceLinePoint(new float[]{x, candleRect.top}, new float[]{x, candleRect.bottom}, null));
            countY++;
        }
    }

    /**
     * 计算价格坐标位置
     */
    protected void calcTextPricePoint(){
        textPrices.clear();
        float moveY = 8 * KlineStyle.pxScaleRate;
        float moveBY = 5 * KlineStyle.pxScaleRate;
        float textWidth = mUpPaint.measureText("-10.08%");
        float startX = 3 * KlineStyle.pxScaleRate;
        textPrices.add(new PriceLinePoint(
                new float[]{startX, candleRect.top+moveY},null,
                CommonUtil.formatNumer(data.lastClosePrice+priceDelta)));
        textPrices.add(new PriceLinePoint(
                new float[]{candleRect.right-textWidth, candleRect.top+moveY},null,
                CommonUtil.formatPercent(priceDelta/data.lastClosePrice)));
        textPrices.add(new PriceLinePoint(
                new float[]{startX, candleRect.height()/2},null,
                CommonUtil.formatNumer(data.lastClosePrice)));
        textPrices.add(new PriceLinePoint(
                new float[]{startX, candleRect.bottom-moveBY},null,
                CommonUtil.formatNumer(data.lastClosePrice-priceDelta)));
        textPrices.add(new PriceLinePoint(
                new float[]{candleRect.right-textWidth, candleRect.bottom-moveBY},null,
                "-"+CommonUtil.formatPercent(priceDelta/data.lastClosePrice)));

    }


}

