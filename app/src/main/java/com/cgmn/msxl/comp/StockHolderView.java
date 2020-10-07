package com.cgmn.msxl.comp;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.cgmn.msxl.R;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.utils.CommonUtil;

public class StockHolderView extends View {
    private RectF contentRect;
    private float contentMinOffset;

    private float headSpace = 120.0f;
    private float topSpace = 5.0f;
    private float vSpace = 2;

    protected Paint mGridPaint, mLabelPaint;
    protected Paint mDownPaint, mUpPaint;
    private Paint.FontMetrics fontMetrics = new Paint.FontMetrics();

    private StockHolder stockHolder;

    public StockHolderView(Context context) {
        this(context, null);
    }

    public StockHolderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StockHolderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        contentRect = new RectF();
        contentMinOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());

        stockHolder = new StockHolder();

        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setStrokeWidth(1.0f);
        mGridPaint.setColor(Color.GRAY);

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(Color.BLACK);
        mLabelPaint.setTextSize(35);
        mLabelPaint.getFontMetrics(fontMetrics);

        mDownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDownPaint.setStyle(Paint.Style.FILL);
        mDownPaint.setStrokeWidth(1);
        mDownPaint.setTextSize(35);
        mDownPaint.setColor(getResources().getColor(R.color.kline_down));

        mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUpPaint.setStyle(Paint.Style.FILL);
        mUpPaint.setStrokeWidth(1);
        mUpPaint.setTextSize(35);
        mUpPaint.setColor(getResources().getColor(R.color.kline_up));
    }

    public StockHolder getStockHolder() {
        return stockHolder;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        contentRect.set(contentMinOffset, contentMinOffset, w - contentMinOffset, h - contentMinOffset);
        prepareMatrixOffset(contentRect.left, contentRect.top);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        renderView(canvas);
    }


    private void renderView(Canvas canvas) {
        renderGrid(canvas);

        renderAccount(canvas);

        renderHeader(canvas);

        if(stockHolder.getHoldShare() > 0){
            renderDetails(canvas);
        }
    }

    private void renderGrid(Canvas canvas) {
        float[] rowlines = new float[12];
        rowlines[0] = 0;
        rowlines[2] = contentRect.right;
        rowlines[4] = 0;
        rowlines[6] = contentRect.right;
        rowlines[1] = headSpace+topSpace;
        rowlines[3] = headSpace+topSpace;
        rowlines[5] = headSpace*2+topSpace;
        rowlines[7] = headSpace*2+topSpace;
        rowlines[8] = 0;
        rowlines[9] = topSpace;
        rowlines[10] = contentRect.right;
        rowlines[11] = topSpace;
        mMatrixOffset.mapPoints(rowlines);
        canvas.drawLines(rowlines, mGridPaint);

        float[] collines = new float[4];
        collines[0] = contentRect.width() / 2;
        collines[2] = collines[0];
        collines[1] = topSpace*4;
        collines[3] = headSpace*2+topSpace - topSpace*4;
        mMatrixOffset.mapPoints(collines);
        canvas.drawLine(collines[0], collines[1], collines[2], collines[3], mGridPaint);
    }
    private float[] calcTextPoint(Object value, int yOrder, float ox, float oY){
        String strValue = null;
        if(value instanceof Float){
            strValue = CommonUtil.formatNumer(value);
        }else{
            strValue = value.toString();
        }

        float halfWidth = contentRect.width() / 2;
        float textLength = mLabelPaint.measureText(strValue);

        float textHight = fontMetrics.bottom - fontMetrics.top;
        float x = (halfWidth-textLength)/2;
        float y = 0;
        if(yOrder == 1){
            y = ( headSpace - textHight *2 - vSpace) / 2;
        }else if( yOrder == 2){
            y = ( headSpace - textHight *2 - vSpace) / 2 + textHight + vSpace;
        }
        float[] pts = {x+ox, y+oY+textHight};
        mMatrixOffset.mapPoints(pts);
        return pts;
    }
    private void renderAccount(Canvas canvas) {
        float[] totallb = calcTextPoint(stockHolder.getTotAmtLb(), 1, 0, topSpace);
        canvas.drawText(
                stockHolder.getTotAmtLb(),
                totallb[0], totallb[1],
                mLabelPaint);

        float[] totalamt = calcTextPoint(stockHolder.getTotAmt(), 2, 0, topSpace);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getTotAmt()),
                totalamt[0], totalamt[1],
                mLabelPaint);

        float[] avaiLb = calcTextPoint(stockHolder.getAvaiAmtLb(), 1, contentRect.width() / 2, 5);
        canvas.drawText(
                stockHolder.getAvaiAmtLb(),
                avaiLb[0], avaiLb[1],
                mLabelPaint);
        float[] avaiamt = calcTextPoint(stockHolder.getAvaiAmt(), 2, contentRect.width() / 2, 5);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getAvaiAmt()),
                avaiamt[0], avaiamt[1],
                mLabelPaint);

        //第二排
        float[] holdlb = calcTextPoint(stockHolder.getHoldAmtLb(), 1, 0, topSpace+headSpace);
        canvas.drawText(
                stockHolder.getHoldAmtLb(),
                holdlb[0], holdlb[1],
                mLabelPaint);

        float[] holdamt = calcTextPoint(stockHolder.getHoldAmt(), 2, 0, topSpace+headSpace);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getHoldAmt()),
                holdamt[0], holdamt[1],
                mLabelPaint);

        float[] plLb = calcTextPoint(stockHolder.getHoldPlLb(), 1, contentRect.width() / 2, topSpace+headSpace);
        canvas.drawText(
                stockHolder.getHoldPlLb(),
                plLb[0], plLb[1],
                mLabelPaint);

        Paint tempPain = mLabelPaint;
        if(stockHolder.getHoldPl() > 0.01){
            tempPain = mUpPaint;
        }
        if(stockHolder.getHoldPl() < 0.0){
            tempPain = mDownPaint;
        }
        float[] plamt = calcTextPoint(stockHolder.getHoldPl(), 2, contentRect.width() / 2, topSpace+headSpace);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getHoldPl()),
                plamt[0], plamt[1],
                tempPain);


    }


    private float[] calcHoldPts(String value, int yOrder, float ox_rete, float oY){
        float baseWidth = contentRect.width() / 4;
        float textLength = mLabelPaint.measureText(value);

        float textHight = fontMetrics.bottom - fontMetrics.top;
        float x = (baseWidth-textLength)/2;
        float y = 0;
        if(yOrder == 1){
            y = ( headSpace - textHight *2 - vSpace) / 2;
        }else if( yOrder == 2){
            y = ( headSpace - textHight *2 - vSpace) / 2 + textHight + vSpace;
        }
        float baseX = ox_rete * contentRect.width();
        float[] pts = {x+baseX, y+oY+textHight};
        mMatrixOffset.mapPoints(pts);
        return pts;

    }
    private void renderHeader(Canvas canvas){
        float orginY = headSpace*2+topSpace;
        float[] p1 = calcHoldPts(stockHolder.getHead1(), 1, 0, orginY);
        canvas.drawText(
                stockHolder.getHead1(),
                p1[0], p1[1],
                mLabelPaint);

        float[] p2 = calcHoldPts(stockHolder.getHead2(), 1, 1/4.0f, orginY);
        canvas.drawText(
                stockHolder.getHead2(),
                p2[0], p2[1],
                mLabelPaint);
        float[] p3 = calcHoldPts(stockHolder.getHead3(), 1, 2/4.0f, orginY);
        canvas.drawText(
                stockHolder.getHead3(),
                p3[0], p3[1],
                mLabelPaint);
        float[] p4 = calcHoldPts(stockHolder.getHead4(), 1, 3/4.0f, orginY);
        canvas.drawText(
                stockHolder.getHead4(),
                p4[0], p4[1],
                mLabelPaint);

        canvas.drawLine(contentRect.left, p1[1]+topSpace*2, contentRect.right, p1[1]+topSpace*2, mGridPaint);
    }


    private float[] calcDetailPts(Object value, int yOrder, float ox_rete, float oY){
        String strValue = null;
        if(value instanceof Float){
            strValue = CommonUtil.formatNumer(value);
        }else{
            strValue = value.toString();
        }
        float baseWidth = contentRect.width() / 4;
        float textLength = mLabelPaint.measureText(strValue);

        float textHight = fontMetrics.bottom - fontMetrics.top;
        float x = (baseWidth-textLength)/2;
        float y = 0;
        float height = headSpace*1.7f;
        if(yOrder == 1){
            y = ( height - textHight *2 - vSpace) / 2;
        }else if( yOrder == 2){
            y = ( height - textHight *2 - vSpace) / 2 + textHight + vSpace;
        }
        float baseX = ox_rete * contentRect.width();
        float[] pts = {x+baseX, y+oY+textHight};
        mMatrixOffset.mapPoints(pts);
        return pts;

    }
    private void renderDetails(Canvas canvas){
        float orginY = headSpace*2.2f;
        float[] code = calcDetailPts(stockHolder.getCode(), 1, 0, orginY);
        canvas.drawText(
                stockHolder.getCode(),
                code[0], code[1],
                mLabelPaint);
        float[] occupy = calcDetailPts(stockHolder.getHoldAmt(), 2, 0, orginY);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getHoldAmt()),
                occupy[0], occupy[1],
                mLabelPaint);

        float[] avaiNum = calcDetailPts(stockHolder.getHoldShare(), 1, 1/4.0f, orginY);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getHoldShare()),
                avaiNum[0], avaiNum[1],
                mLabelPaint);
        float[] holdNum = calcDetailPts(stockHolder.getAvaiLabelShare(), 2, 1/4.0f, orginY);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getAvaiLabelShare()),
                holdNum[0], holdNum[1],
                mLabelPaint);

        float[] nowP = calcDetailPts(stockHolder.getPrice(), 1, 2/4.0f, orginY);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getPrice()),
                nowP[0], nowP[1],
                mLabelPaint);
        float[] costPrice = calcDetailPts(stockHolder.getCostPrice(), 2, 2/4.0f, orginY);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getCostPrice()),
                costPrice[0], costPrice[1],
                mLabelPaint);

        Paint tempPain = mLabelPaint;
        if(stockHolder.getHoldPl() > 0){
            tempPain = mUpPaint;
        }else if(stockHolder.getHoldPl() < 0){
            tempPain = mDownPaint;
        }
        float[] plAmt = calcDetailPts(stockHolder.getHoldPl(), 1, 3/4.0f, orginY);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getHoldPl()),
                plAmt[0], plAmt[1],
                tempPain);
        float[] plRate = calcDetailPts(stockHolder.getPlRate(), 2, 3/4.0f, orginY);
        canvas.drawText(
                stockHolder.getPlRate(),
                plRate[0], plRate[1],
                tempPain);

        float bottom = orginY+headSpace*1.7f;
        canvas.drawLine(contentRect.left, bottom, contentRect.right, bottom, mGridPaint);

        canvas.drawText(
                stockHolder.getExchangeLb() + CommonUtil.formatNumer(stockHolder.getExchange()),
                contentRect.left + 30, contentRect.top+bottom+topSpace*6,
                mLabelPaint);

    }


    /**
     * matrix to map the chart offset
     */
    protected Matrix mMatrixOffset = new Matrix();

    public void prepareMatrixOffset(float offsetX, float offsetY) {
        mMatrixOffset.reset();
        mMatrixOffset.postTranslate(offsetX, offsetY);
    }

    public void invalidateView() {
        invalidate();
    }

    public void initAccount(Float totalAmount){
        stockHolder.setTotAmt(totalAmount);
        stockHolder.setAvaiAmt(totalAmount);
    }

}
