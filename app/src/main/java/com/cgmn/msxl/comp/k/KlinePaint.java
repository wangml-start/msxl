package com.cgmn.msxl.comp.k;

import android.graphics.*;
import android.view.MotionEvent;
import com.cgmn.msxl.utils.CommonUtil;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class KlinePaint {
    protected final Paint mDownPaint, mUpPaint,mimdlePaint;
    protected final Paint mGridPaint, mLabelPaint;
    protected final Paint mk5Paint, mk10Paint, mk20Paint;
    private Paint.FontMetrics fontMetrics = new Paint.FontMetrics();

    protected KlineGroup data;

    protected RectF contentRect = new RectF();
    protected RectF candleRect = new RectF();
    protected RectF barRect = new RectF();
    protected RectF macdRect = new RectF();

    private final float candleBarRatio = 0.56f;
    private final float reactUseRate = 0.95f;

    // contain 4 points to draw 2 lines.
    protected float[] shadowBuffer = new float[8];
    // contain 2 points to draw a rect.
    protected float[] bodyBuffer = new float[4];
    // contain 1 points to get y value.
    protected float[] barBuffer = new float[2];
    // contain 1 points to get y value.
    protected float[] macdBuffer = new float[4];
    protected float[] difBuffer = new float[4];
    protected float[] deaBuffer = new float[4];

    private float[] calcTemp = new float[]{0, 0};

    // contain 4 points to draw average lines.
    protected float[] averageBuffer = new float[4];

    /**
     * the space between the entries, default 0.1f (10%)
     */
    protected float mBarSpace = 0.15f;
    protected float kLineBold = 3f;

    /**
     * the max visible entry count.
     */
    protected int visibleCount = 40;

    private boolean highlightEnable = false;
    private float[] highlightPoint = new float[2];

    /**
     * a y value formatter.
     */
    protected DecimalFormat decimalFormatter = new DecimalFormat("0.00");

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
        mDownPaint.setStrokeWidth(kLineBold);

        mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUpPaint.setStyle(Paint.Style.STROKE);
        mUpPaint.setStrokeWidth(kLineBold);

        mimdlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mimdlePaint.setStyle(Paint.Style.STROKE);
        mimdlePaint.setStrokeWidth(kLineBold);
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
        mk5Paint.setStrokeWidth(kLineBold);
        mk10Paint.setStrokeWidth(kLineBold);
        mk20Paint.setStrokeWidth(kLineBold);
    }

    public void setContentRect(RectF contentRect) {
        float barTop = contentRect.bottom -
                (1 - candleBarRatio) * (contentRect.height());
        float macdTop = barTop + (1 - candleBarRatio) * (contentRect.height()) / 2.0f;

        this.candleRect.set(contentRect.left, contentRect.top, contentRect.right, barTop);
        this.barRect.set(contentRect.left, barTop, contentRect.right, macdTop);
        this.macdRect.set(contentRect.left, macdTop, contentRect.right, contentRect.bottom);
        this.contentRect = contentRect;
    }

    public void setData(KlineGroup data) {
        this.data = data;
        prepareMatrixValue(data.mYMax - data.mYMin, data.mYMin);
        prepareMatrixOffset(candleRect.left, candleRect.top);
    }

    /**
     * draw everything.
     */
    public void render(Canvas canvas) {
        // CALC
        calc();
        // DRAW LABELS
        renderLabels(canvas);
        canvas.drawLine(0, macdRect.top, macdRect.right, macdRect.top, mGridPaint);
        canvas.drawLine(0, macdRect.bottom, macdRect.right, macdRect.bottom, mGridPaint);

        // set the entry draw area.
        canvas.save();
        canvas.clipRect(candleRect.left, candleRect.top, candleRect.right, macdRect.bottom);
        List<KLine> temList = data.getNodes().subList(visibleXMin, visibleXMax);
        for (int i = 0; i < temList.size(); i++) {
            KLine entry = temList.get(i);
            // draw step 0: set color
            Paint tempKpint = mimdlePaint;
            if(entry.open < entry.close){
                tempKpint= mUpPaint;
            }else if(entry.open > entry.close){
                tempKpint = mDownPaint;
            }
            if (entry.isOpen) {
                float openKline[] = new float[4];
                openKline[0] = i + mBarSpace;
                openKline[2] = i + 1 - mBarSpace;
                openKline[1] = entry.open;
                openKline[3] = entry.open;
                mapPoints(openKline);
                canvas.drawLine(openKline[0], openKline[1], openKline[2], openKline[3], tempKpint);

                continue;
            }

            // draw step 1: draw shadow 上下引线
            shadowBuffer[0] = i + 0.5f;
            shadowBuffer[2] = i + 0.5f;
            shadowBuffer[4] = i + 0.5f;
            shadowBuffer[6] = i + 0.5f;
            if (entry.open > entry.close) {
                shadowBuffer[1] = entry.high;
                shadowBuffer[3] = entry.open;
                shadowBuffer[5] = entry.close;
                shadowBuffer[7] = entry.low;
            } else {
                shadowBuffer[1] = entry.high;
                shadowBuffer[3] = entry.close;
                shadowBuffer[5] = entry.open;
                shadowBuffer[7] = entry.low;
            }
            mapPoints(shadowBuffer);
            canvas.drawLines(shadowBuffer, tempKpint);

            // draw step 2: draw body
            bodyBuffer[0] = i + mBarSpace;
            bodyBuffer[2] = i + 1 - mBarSpace;
            if (entry.open > entry.close) {
                bodyBuffer[1] = entry.open;
                bodyBuffer[3] = entry.close;
            } else {
                bodyBuffer[1] = entry.close;
                bodyBuffer[3] = entry.open;
            }

            mapPoints(bodyBuffer);
            if(CommonUtil.floatNumEqual(entry.close, entry.open)){
                canvas.drawLine(bodyBuffer[0], bodyBuffer[1], bodyBuffer[2], bodyBuffer[3], tempKpint);
            }else{
                canvas.drawRect(bodyBuffer[0], bodyBuffer[1], bodyBuffer[2], bodyBuffer[3], tempKpint);
            }

            // draw step 3: draw bar
            barBuffer[0] = 0;
            barBuffer[1] = entry.volume;
            mMatrixBar.mapPoints(barBuffer);
            if(entry.close >= entry.lastClose){
                canvas.drawRect(bodyBuffer[0], barRect.bottom - barBuffer[1], bodyBuffer[2], barRect.bottom - 1,mUpPaint);
            }else{
                canvas.drawRect(bodyBuffer[0], barRect.bottom - barBuffer[1], bodyBuffer[2], barRect.bottom - 1, mDownPaint);
            }

            //draw step 4: MA5 10 20
            if (i > 0) {
                KLine preEntry = temList.get(i - 1);
                if (preEntry.avg5 > 0) {
                    averageBuffer[0] = i - 1;
                    averageBuffer[2] = i;
                    averageBuffer[1] = preEntry.avg5;
                    averageBuffer[3] = entry.avg5;
                    mapPoints(averageBuffer);
                    canvas.drawLine(averageBuffer[0], averageBuffer[1], averageBuffer[2], averageBuffer[3], mk5Paint);
                }
                if (preEntry.avg10 > 0) {
                    averageBuffer[0] = i - 1;
                    averageBuffer[2] = i;
                    averageBuffer[1] = preEntry.avg10;
                    averageBuffer[3] = entry.avg10;
                    mapPoints(averageBuffer);
                    canvas.drawLine(averageBuffer[0], averageBuffer[1], averageBuffer[2], averageBuffer[3], mk10Paint);
                }
                if (preEntry.avg20 > 0) {
                    averageBuffer[0] = i - 1;
                    averageBuffer[2] = i;
                    averageBuffer[1] = preEntry.avg20;
                    averageBuffer[3] = entry.avg20;
                    mapPoints(averageBuffer);
                    canvas.drawLine(averageBuffer[0], averageBuffer[1], averageBuffer[2], averageBuffer[3], mk20Paint);
                }
            }

            //draw step 4: MACD
            macdBuffer[0] = 0;
            macdBuffer[1] = entry.macd;
            macdBuffer[2] = 0;
            macdBuffer[3] = 0;
            mMatrixMacd.mapPoints(macdBuffer);
            mMatrixOffset.mapPoints(macdBuffer);
            if (entry.macd > 0) {
                canvas.drawLine(shadowBuffer[0], macdRect.bottom - macdBuffer[1], shadowBuffer[0], macdRect.bottom - macdBuffer[3], mUpPaint);
            } else {
                canvas.drawLine(shadowBuffer[0], macdRect.bottom - macdBuffer[1], shadowBuffer[0], macdRect.bottom - macdBuffer[3], mDownPaint);
            }

            if (i > 0) {
                KLine preEntry = temList.get(i - 1);
                difBuffer[0] = i - 1;
                difBuffer[2] = i;
                difBuffer[1] = preEntry.dif;
                difBuffer[3] = entry.dif;
                mMatrixMacd.mapPoints(difBuffer);
                mMatrixOffset.mapPoints(difBuffer);
                canvas.drawLine(difBuffer[0], macdRect.bottom - difBuffer[1], difBuffer[2], macdRect.bottom - difBuffer[3], mk5Paint);

                deaBuffer[0] = i - 1;
                deaBuffer[2] = i;
                deaBuffer[1] = preEntry.dea;
                deaBuffer[3] = entry.dea;
                mMatrixMacd.mapPoints(deaBuffer);
                mMatrixOffset.mapPoints(deaBuffer);
                canvas.drawLine(deaBuffer[0], macdRect.bottom - deaBuffer[1], deaBuffer[2], macdRect.bottom - deaBuffer[3], mk10Paint);
            }


            // extra calc: set highlight position
            if (highlightPoint[0] <= bodyBuffer[2] && highlightPoint[0] >= bodyBuffer[0]) {
                highlightPoint[0] = shadowBuffer[0];
                highlightPoint[1] = (bodyBuffer[1] + bodyBuffer[3]) / 2;

                // DRAW HIGHLIGHT
                if (highlightEnable) {
                    renderHighlight(canvas);
                }
            }
        }
        canvas.restore();
    }

    /**
     * Calculate the current range of x and y.
     */
    protected void calc() {
        visibleXMin = data.getNodes().size() - visibleCount;
        if (visibleXMin < 0) {
            visibleXMin = 0;
        }
        visibleXMax = data.getNodes().size();

        // calc step 1: calc min&max y value
        data.calcMinMax(visibleXMin, visibleXMax);
        prepareMatrixValue(data.mYMax - data.mYMin, data.mYMin);
        prepareMatrixBar(data.mMaxYVolume);
        prepareMatrixMacd();
    }

    /**
     * Draw x and y labels.
     */
    protected void renderLabels(Canvas canvas) {
        int marginRight = 16;
        float textX = canvas.getWidth()-marginRight;
        // DRAW Y LABELS
        mLabelPaint.setTextAlign(Paint.Align.RIGHT);

        //draw max y value
        calcTemp[1] = candleRect.top;
        revertMapPoints(calcTemp);
        String value = decimalFormatter.format(calcTemp[1]);
        mLabelPaint.setTextSize(30);
        mLabelPaint.getFontMetrics(fontMetrics);
        canvas.drawLine(5, candleRect.top - fontMetrics.top - fontMetrics.bottom, candleRect.right, candleRect.top - fontMetrics.top - fontMetrics.bottom, mGridPaint);
        canvas.drawText(
                value,
                textX,
                candleRect.top - fontMetrics.top - fontMetrics.bottom,
                mLabelPaint);
        // draw min y value
        calcTemp[1] = candleRect.bottom;
        revertMapPoints(calcTemp);
        value = decimalFormatter.format(calcTemp[1]);
        mLabelPaint.getFontMetrics(fontMetrics);
        canvas.drawLine(5, candleRect.bottom - fontMetrics.bottom, candleRect.right, candleRect.bottom - fontMetrics.bottom, mGridPaint);
        canvas.drawText(
                value,
                textX,
                candleRect.bottom - fontMetrics.bottom,
                mLabelPaint);

        calcTemp[1] = candleRect.height() / 3 + candleRect.top;
        revertMapPoints(calcTemp);
        value = decimalFormatter.format(calcTemp[1]);
        mLabelPaint.getFontMetrics(fontMetrics);
        canvas.drawLine(5, candleRect.height() / 3 + candleRect.top + fontMetrics.bottom, candleRect.right, candleRect.height() / 3 + candleRect.top + fontMetrics.bottom, mGridPaint);
        canvas.drawText(
                value,
                textX,
                candleRect.height() / 3 + candleRect.top + fontMetrics.bottom,
                mLabelPaint);

        calcTemp[1] = candleRect.height() * 2 / 3 + candleRect.top;
        revertMapPoints(calcTemp);
        value = decimalFormatter.format(calcTemp[1]);
        mLabelPaint.getFontMetrics(fontMetrics);
        canvas.drawLine(5, candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom, candleRect.right, candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom, mGridPaint);
        canvas.drawText(
                value,
                textX,
                candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom,
                mLabelPaint);

        mLabelPaint.setTextSize(30);
        canvas.drawText(
                "VOL",
                textX,
                barRect.height() * 3 / 5 + candleRect.bottom,
                mLabelPaint);

        canvas.drawText(
                "MACD",
                textX,
                macdRect.height() * 3 / 5 + barRect.bottom,
                mLabelPaint);

        canvas.save();
        canvas.restore();
    }

    /**
     * Draw highlight.
     */
    protected void renderHighlight(Canvas canvas) {
        canvas.drawLine(candleRect.left, highlightPoint[1], candleRect.right, highlightPoint[1], mGridPaint);
        canvas.drawLine(highlightPoint[0], candleRect.top, highlightPoint[0], barRect.bottom, mGridPaint);
    }

    /**
     * Transform an array of points with all matrices.
     * VERY IMPORTANT: Keep matrix order "value-touch-offset" when transforming.
     */
    protected void mapPoints(float[] pts) {
        mMatrixValue.mapPoints(pts);
        mMatrixOffset.mapPoints(pts);
    }

    protected void revertMapPoints(float[] pixels) {
        Matrix tmp = new Matrix();

        // invert all matrices to convert back to the original value
        mMatrixOffset.invert(tmp);
        tmp.mapPoints(pixels);

        tmp.mapPoints(pixels);

        mMatrixValue.invert(tmp);
        tmp.mapPoints(pixels);
    }

    public void prepareMatrixValue(float deltaY, float yMin) {
        float scaleX = candleRect.width() / visibleCount;
        float scaleY = candleRect.height() * reactUseRate / deltaY;

        mMatrixValue.reset();
        mMatrixValue.postTranslate(0, -yMin);
        // the negative scale factor is used to draw y from down to up
        mMatrixValue.postScale(scaleX, -scaleY);
        mMatrixValue.postTranslate(0, candleRect.height());
    }

    public void prepareMatrixOffset(float offsetX, float offsetY) {
        mMatrixOffset.reset();
        mMatrixOffset.postTranslate(offsetX, offsetY);
    }

    public void prepareMatrixBar(float maxY) {
        // increase the y range for good looking.
        mMatrixBar.reset();
        mMatrixBar.postScale(1, barRect.height() * reactUseRate / maxY);
    }

    public void prepareMatrixMacd() {
        float deltaY = Math.abs(data.mYMaxMacd) + Math.abs(data.mYMinMacd);
        float absMax = Math.abs(data.mYMaxMacd);
        float absMin = Math.abs(data.mYMaxMacd);
        int direction = 1;
        if (Math.abs(data.mYMinMacd) > absMax) {
            absMax = Math.abs(data.mYMinMacd);
            direction = -1;
        }
        if (Math.abs(data.mYMinMacd) < absMin) {
            absMin = Math.abs(data.mYMinMacd);
        }
        float scaleY = macdRect.height() * 0.75f / deltaY;
        float scaleX = macdRect.width() / visibleCount;
        mMatrixMacd.reset();
        if(direction > 0){
            mMatrixMacd.postTranslate(0, absMin);
        }else{
            mMatrixMacd.postTranslate(0, absMax);
        }
        mMatrixMacd.postScale(scaleX, scaleY);
    }

    /**
     * matrix to map the values to the screen pixels
     */
    protected Matrix mMatrixValue = new Matrix();

    /**
     * matrix to map the chart offset
     */
    protected Matrix mMatrixOffset = new Matrix();

    /**
     * matrix to map the volume value
     */
    protected Matrix mMatrixBar = new Matrix();

    /**
     * matrix to map the macd value
     */
    protected Matrix mMatrixMacd = new Matrix();

    protected int visibleXMin, visibleXMax;

}
