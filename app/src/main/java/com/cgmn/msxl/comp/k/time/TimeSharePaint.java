package com.cgmn.msxl.comp.k.time;

import android.graphics.*;
import com.cgmn.msxl.comp.k.*;
import com.cgmn.msxl.server_interface.TimeShare;
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

    private float textSize = KlineStyle.kTextSize*0.7f;

    protected float priceDelta;

    protected Boolean showDetail = true;

    protected float margin = 4f * KlineStyle.pxScaleRate, padding = 2.5f * KlineStyle.pxScaleRate;
    float chartHeight,chartWeight,candleWidth,volWidth;

    public void setShowDetail(Boolean showDetail) {
        this.showDetail = showDetail;
    }

    public TimeSharePaint() {
        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStrokeWidth(KlineStyle.gridLine);
        mGridPaint.setColor(Color.GRAY);

        avgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        avgPaint.setStyle(Paint.Style.FILL);
        avgPaint.setStrokeWidth(KlineStyle.timeShareBold);
        avgPaint.setColor(Color.parseColor("#FFC800"));

        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setStyle(Paint.Style.FILL);
        timePaint.setStrokeWidth(KlineStyle.timeShareBold);
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
        this.contentRect = contentRect;
        chartHeight = contentRect.height()-2*padding;
        chartWeight = contentRect.width()-2*margin;
        candleWidth = chartWeight;
        volWidth = 0;
        if(showDetail){
            candleWidth = chartWeight * 0.74f;
            volWidth = chartWeight-candleWidth;
        }
    }

    public void setData(TimeShareGroup data) {
        this.data = data;
    }

    /**
     * draw everything.
     */
    public void render(Canvas canvas) {
        canvas.clipRect(contentRect.left, contentRect.top, contentRect.right, contentRect.bottom);
        // CALC
        calcChartPoint();
        //表格线
        calcGridLinePoint();
        //价格文本
        calcTextPricePoint();

        drawGridLines(canvas);
        // DRAW LABELS
        renderLabels(canvas);

        drawTradeDetails(canvas);

        // set the entry draw area.
        canvas.save();
        Integer endIndex = timePoints.size();
        for (int i = 0; i < endIndex; i++) {
            ChartPoint timePt = timePoints.get(i);
            ChartPoint solidPt = solidPts.get(i);
            if (i > 0) {
                ChartPoint ltimePt = timePoints.get(i-1);
                ChartPoint lsolidPt = solidPts.get(i-1);

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
        if(showDetail){
            canvas.drawLine(contentRect.right-margin,contentRect.top+padding,contentRect.right-margin,contentRect.bottom-padding, mGridPaint);

            canvas.drawLine(margin+candleWidth,padding,contentRect.right-margin,padding, mGridPaint);
            canvas.drawLine(margin+candleWidth,contentRect.bottom-padding,contentRect.right-margin,contentRect.bottom-padding, mGridPaint);
        }
    }

    protected void drawTradeDetails(Canvas canvas){
        if(showDetail){
            float marginX = 2f * KlineStyle.pxScaleRate;
            float itemHeight = reCalcHeadSpace();

            mLabelPaint.setTextAlign(Paint.Align.LEFT);
            buyPaint.setTextAlign(Paint.Align.RIGHT);
            sellPaint.setTextAlign(Paint.Align.RIGHT);
            float timeWidth = mUpPaint.measureText("15:00");
            Integer showCount = (int)(chartHeight/itemHeight);
            Integer index = 0;

            Integer length = data.showList.size();

            float distanceX = marginX;
            float baseX = margin + candleWidth;
            Paint defPaint,pricePaint;
            while(index < showCount){
                if(length == 0 || index>=length){
                    break;
                }
                TimeShare item = data.showList.get(length-1-index);

                float y = itemHeight*(showCount-index) + (chartHeight-itemHeight*showCount);
                defPaint = mLabelPaint;
                String time = item.getShowTime();
                canvas.drawText(time, baseX+distanceX, y, defPaint);
                String suffix = "";
                if("B".equals(item.getCharType())){
                    defPaint = mUpPaint;
                    suffix = "↑";
                }else if("S".equals(item.getCharType())){
                    defPaint = mDownPaint;
                    suffix = "↓";
                }
                if("09:25".equals(time)){
                    defPaint = mLabelPaint;
                    suffix = "";
                }

                pricePaint = mLabelPaint;
                if(item.getPrice() > data.lastClosePrice){
                    pricePaint = mUpPaint;
                }else if(item.getPrice() < data.lastClosePrice){
                    pricePaint = mDownPaint;
                }
                String price = CommonUtil.formatNumer(item.getPrice());
                float priceWidth = mUpPaint.measureText(price);
                canvas.drawText(price, baseX+distanceX+timeWidth+distanceX, y, pricePaint);
                canvas.drawText(suffix, baseX+distanceX+timeWidth+distanceX+priceWidth, y, defPaint);

                String volText = CommonUtil.formatVolume(item.getVolume());
                float volWidth = mUpPaint.measureText(volText);
                canvas.drawText(volText, contentRect.right-margin-volWidth-distanceX, y, defPaint);

                index++;
            }
            canvas.save();
            canvas.restore();
        }
    }
    /**
     * 计算坐标
     */
    protected void calcChartPoint(){
        Integer count = 4 * 60;
        priceDelta = data.calExtremeNum();
        float delta = priceDelta*2;
        float punit = chartHeight / delta;
        float distanceX = candleWidth / count;
        float mYMax = priceDelta+data.lastClosePrice;

        timePoints.clear();
        solidPts.clear();
        Integer endIndex = data.solidPrices.size();
        //分时线
        for (int i = 0; i < endIndex; i++) {
            CMinute node = data.timePrices.get(i);
            CMinute snode = data.solidPrices.get(i);

            float startx = distanceX * i + margin;
            ChartPoint timeItem = new ChartPoint(startx, (mYMax - node.price) * punit + padding);
            timePoints.add(timeItem);
            solidPts.add(new ChartPoint(startx, (mYMax - snode.price) * punit + padding));
            if(node.opChar != null){
                timeItem.setOpInfo(startx, (mYMax - node.opPrice) * punit + padding, node.opChar);
            }
        }
    }
    /**
     * 计算表格线
     */
    protected void calcGridLinePoint(){
        xLines.clear();
        float pstartx = margin;
        float pendx = margin+candleWidth;
        Integer count = 0;

        while(count < 5){
            float y = chartHeight * count / 4;
            xLines.add(new PriceLinePoint(new float[]{pstartx, y+padding}, new float[]{pendx, y+padding}, null));
            count++;
        }

        yLines.clear();
        Integer countY = 0;
        while(countY < 5){
            float x = candleWidth * countY / 4;
            yLines.add(new PriceLinePoint(new float[]{x+margin, contentRect.top+padding}, new float[]{x+margin, contentRect.bottom-padding}, null));
            countY++;
        }
    }

    /**
     * 计算价格坐标位置
     */
    protected void calcTextPricePoint(){
        textPrices.clear();
        float moveY = 8 * KlineStyle.pxScaleRate;
        float moveBY = 3 * KlineStyle.pxScaleRate;
        String downRate = "-"+CommonUtil.formatPercent(priceDelta/data.lastClosePrice);
        float textWidth = mUpPaint.measureText(downRate);
        float startX = margin+2f*KlineStyle.pxScaleRate;
        textPrices.add(new PriceLinePoint(
                new float[]{startX, contentRect.top+moveY+padding},null,
                CommonUtil.formatNumer(data.lastClosePrice+priceDelta)));
        textPrices.add(new PriceLinePoint(
                new float[]{margin+candleWidth-textWidth, contentRect.top+moveY+padding},null,
                CommonUtil.formatPercent(priceDelta/data.lastClosePrice)));
        textPrices.add(new PriceLinePoint(
                new float[]{startX, contentRect.height()/2+moveBY},null,
                CommonUtil.formatNumer(data.lastClosePrice)));
        textPrices.add(new PriceLinePoint(
                new float[]{startX, contentRect.bottom-moveBY-padding},null,
                CommonUtil.formatNumer(data.lastClosePrice-priceDelta)));
        textPrices.add(new PriceLinePoint(
                new float[]{margin+candleWidth-textWidth, contentRect.bottom-moveBY-padding},null,
                downRate));

    }


    public float reCalcHeadSpace(){
        float textHight = fontMetrics.bottom - fontMetrics.top;

        return textHight*1f;
    }
}

