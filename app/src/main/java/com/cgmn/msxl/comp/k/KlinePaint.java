package com.cgmn.msxl.comp.k;

import android.graphics.*;
import android.view.MotionEvent;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class KlinePaint {
    protected final Paint mDownPaint, mUpPaint;
    protected final Paint mGridPaint, mLabelPaint;
    protected final Paint mk5Paint, mk10Paint, mk20Paint;
    private Paint.FontMetrics fontMetrics = new Paint.FontMetrics();
    private float defaultTextSize;

    protected KlineGroup data;

    protected RectF candleRect = new RectF();
    protected RectF barRect = new RectF();
    protected RectF macdRect = new RectF();

    private final float candleBarRatio = 0.7f;
    private final float macdBarRatio = 0.85f;

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
    protected float mBarSpace = 0.1f;

    /**
     * the max visible entry count.
     */
    protected int visibleCount = 50;

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
        mDownPaint.setStrokeWidth(1);

        mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUpPaint.setStyle(Paint.Style.STROKE);
        mUpPaint.setStrokeWidth(1);

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
        mk5Paint.setStrokeWidth(1f);
        mk10Paint.setStrokeWidth(1f);
        mk20Paint.setStrokeWidth(1f);
    }

    public void setContentRect(RectF contentRect) {
        float barTop = contentRect.bottom -
                (1 - candleBarRatio) * (contentRect.height() - contentRect.top);
        float macdTop = contentRect.bottom -
                (1 - macdBarRatio) * (contentRect.height() - contentRect.top);
        float candleBottom = barTop - contentRect.top;
        float barBottom = macdTop - contentRect.top;

        this.candleRect.set(contentRect.left, contentRect.top, contentRect.right, candleBottom);
        this.barRect.set(contentRect.left, barTop, contentRect.right, barBottom);
        this.macdRect.set(contentRect.left, macdTop, contentRect.right, contentRect.bottom);

        defaultTextSize = contentRect.top * 3 / 4;
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

        // set the entry draw area.
        canvas.save();
        canvas.clipRect(candleRect.left, candleRect.top, candleRect.right, macdRect.bottom);
        List<KLine> temList = data.getNodes().subList(visibleXMin, visibleXMax);
        for (int i = 0; i < temList.size(); i++) {
            KLine entry = temList.get(i);
            if(entry.isOpen){
                float openKline[] = new float[4];
                openKline[0] = i + mBarSpace;
                openKline[2] = i + 1 - mBarSpace;
                openKline[1] = entry.open;
                openKline[3] = entry.open;
                mapPoints(openKline);
                canvas.drawLine(openKline[0], openKline[1], openKline[2], openKline[3], mLabelPaint);

                continue;
            }

            // draw step 0: set color
            Boolean isUp = entry.open < entry.close;

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
            if (isUp) {
                canvas.drawLines(shadowBuffer, mUpPaint);
            } else {
                canvas.drawLines(shadowBuffer, mDownPaint);
            }

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
            if (isUp) {
                canvas.drawRect(bodyBuffer[0], bodyBuffer[1], bodyBuffer[2], bodyBuffer[3], mUpPaint);
            } else {
                canvas.drawRect(bodyBuffer[0], bodyBuffer[1], bodyBuffer[2], bodyBuffer[3], mDownPaint);
            }


            // draw step 3: draw bar
            barBuffer[0] = 0;
            barBuffer[1] = entry.volume;
            mMatrixBar.mapPoints(barBuffer);
            if (isUp) {
                canvas.drawRect(bodyBuffer[0], barRect.bottom - barBuffer[1], bodyBuffer[2], barRect.bottom - 1, mUpPaint);
            } else {
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
                canvas.drawLine(shadowBuffer[0], macdBuffer[1], shadowBuffer[0], macdBuffer[3], mUpPaint);
            } else {
                canvas.drawLine(shadowBuffer[0], macdBuffer[1], shadowBuffer[0], macdBuffer[3], mDownPaint);
            }

            if (i > 0) {
                KLine preEntry = temList.get(i - 1);
                difBuffer[0] = i - 1;
                difBuffer[2] = i;
                difBuffer[1] = preEntry.dif;
                difBuffer[3] = entry.dif;
                mMatrixMacd.mapPoints(difBuffer);
                mMatrixOffset.mapPoints(difBuffer);
                canvas.drawLine(difBuffer[0], difBuffer[1], difBuffer[2], difBuffer[3], mk5Paint);

                deaBuffer[0] = i - 1;
                deaBuffer[2] = i;
                deaBuffer[1] = preEntry.dea;
                deaBuffer[3] = entry.dea;
                mMatrixMacd.mapPoints(deaBuffer);
                mMatrixOffset.mapPoints(deaBuffer);
                canvas.drawLine(deaBuffer[0], deaBuffer[1], deaBuffer[2], deaBuffer[3], mk10Paint);
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
        // DRAW Y LABELS
        mLabelPaint.setTextAlign(Paint.Align.RIGHT);

        //draw max y value
        calcTemp[1] = candleRect.top;
        revertMapPoints(calcTemp);
        String value = decimalFormatter.format(calcTemp[1]);
        mLabelPaint.setTextSize(10);
        mLabelPaint.setTextSize(candleRect.left * 9 / mLabelPaint.measureText(value));
        mLabelPaint.getFontMetrics(fontMetrics);
        canvas.drawText(
                value,
                candleRect.left * 9 / 10,
                candleRect.top - fontMetrics.top - fontMetrics.bottom,
                mLabelPaint);
        canvas.drawLine(candleRect.left, candleRect.top - fontMetrics.top - fontMetrics.bottom, candleRect.right, candleRect.top - fontMetrics.top - fontMetrics.bottom, mGridPaint);

        // draw min y value
        calcTemp[1] = candleRect.bottom;
        revertMapPoints(calcTemp);
        value = decimalFormatter.format(calcTemp[1]);
        mLabelPaint.setTextSize(10);
        mLabelPaint.setTextSize(candleRect.left * 10 / mLabelPaint.measureText(value));
        mLabelPaint.getFontMetrics(fontMetrics);
        canvas.drawText(
                value,
                candleRect.left * 9 / 10,
                candleRect.bottom - fontMetrics.bottom,
                mLabelPaint);
        canvas.drawLine(candleRect.left, candleRect.bottom - fontMetrics.bottom, candleRect.right, candleRect.bottom - fontMetrics.bottom, mGridPaint);

        calcTemp[1] = candleRect.height() / 3 + candleRect.top;
        revertMapPoints(calcTemp);
        value = decimalFormatter.format(calcTemp[1]);
        mLabelPaint.setTextSize(10);
        mLabelPaint.setTextSize(candleRect.left * 10 / mLabelPaint.measureText(value));
        mLabelPaint.getFontMetrics(fontMetrics);
        canvas.drawText(
                value,
                candleRect.left * 9 / 10,
                candleRect.height() / 3 + candleRect.top + fontMetrics.bottom,
                mLabelPaint);
        canvas.drawLine(candleRect.left, candleRect.height() / 3 + candleRect.top + fontMetrics.bottom, candleRect.right, candleRect.height() / 3 + candleRect.top + fontMetrics.bottom, mGridPaint);

        calcTemp[1] = candleRect.height() * 2 / 3 + candleRect.top;
        revertMapPoints(calcTemp);
        value = decimalFormatter.format(calcTemp[1]);
        mLabelPaint.setTextSize(10);
        mLabelPaint.setTextSize(candleRect.left * 10 / mLabelPaint.measureText(value));
        mLabelPaint.getFontMetrics(fontMetrics);
        canvas.drawText(
                value,
                candleRect.left * 9 / 10,
                candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom,
                mLabelPaint);
        canvas.drawLine(candleRect.left, candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom, candleRect.right, candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom, mGridPaint);
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
        deltaY += yMin * 0.03;
        float scaleX = candleRect.width() / visibleCount;
        float scaleY = candleRect.height() / deltaY;

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
        mMatrixBar.postScale(1, barRect.height() / maxY);
    }

    public void prepareMatrixMacd() {
        float deltaY = Math.abs(data.mYMaxMacd) + Math.abs(data.mYMinMacd);
        deltaY = deltaY * 20 / 10;
        float absMin = Math.abs(data.mYMaxMacd);
        if (Math.abs(data.mYMinMacd) < absMin) {
          absMin = Math.abs(data.mYMinMacd);
        }
        float scaleY = macdRect.height() / deltaY;
        float scaleX = macdRect.width() / visibleCount;
        mMatrixMacd.reset();
        float height = candleRect.height() + barRect.height() + macdRect.height();
        mMatrixMacd.postTranslate(0, -absMin);
        mMatrixMacd.postScale(scaleX, -scaleY);
        mMatrixMacd.postTranslate(0, height);

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

    private boolean isOnBorder = true;

}
