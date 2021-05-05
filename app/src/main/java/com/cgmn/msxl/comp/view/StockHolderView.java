package com.cgmn.msxl.comp.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.utils.CommonUtil;

public class StockHolderView extends View {
    private RectF contentRect;
    private float contentMinOffset;

    private float headSpace = 120.0f;
    private float topSpace = 2.0f * KlineStyle.pxScaleRate;
    private float vSpace = 0.5f * KlineStyle.pxScaleRate;
    private float textSize = KlineStyle.kTextSize;


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
        mGridPaint.setStrokeWidth(KlineStyle.gridLine);
        mGridPaint.setColor(Color.GRAY);

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(Color.BLACK);
        mLabelPaint.setTextSize(textSize);
        mLabelPaint.getFontMetrics(fontMetrics);

        mDownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDownPaint.setStyle(Paint.Style.FILL);
        mDownPaint.setStrokeWidth(KlineStyle.gridLine);
        mDownPaint.setTextSize(textSize);
        mDownPaint.setColor(getResources().getColor(R.color.kline_down));

        mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUpPaint.setStyle(Paint.Style.FILL);
        mUpPaint.setStrokeWidth(KlineStyle.gridLine);
        mUpPaint.setTextSize(textSize);
        mUpPaint.setColor(getResources().getColor(R.color.kline_up));

        headSpace = reCalcHeadSpace();
    }

    public StockHolder getStockHolder() {
        return stockHolder;
    }

    public void setStockHolder(StockHolder stockHolder) {
        this.stockHolder = stockHolder;
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

        float[] collines = new float[8];
        collines[0] = contentRect.width() * 1/3.0f;
        collines[2] = collines[0];
        collines[1] = topSpace*4;
        collines[3] = headSpace*2+topSpace - topSpace*4;

        collines[4] = contentRect.width()  * 2/3.0f;
        collines[6] = collines[4];
        collines[5] = topSpace*4;
        collines[7] = headSpace*2+topSpace - topSpace*4;
        mMatrixOffset.mapPoints(collines);
        canvas.drawLines(collines, mGridPaint);
    }

    private float[] calcTextPoint(Object value, int yOrder, float ox_rate, float oY){
        String strValue = null;
        if(value instanceof Float){
            strValue = CommonUtil.formatNumer(value);
        }else{
            strValue = value.toString();
        }

        float halfWidth = contentRect.width() / 3;
        float textLength = mLabelPaint.measureText(strValue);

        float textHight = fontMetrics.bottom - fontMetrics.top;
        float x = (halfWidth-textLength)/2;
        float y = 0;
        if(yOrder == 1){
            y = ( headSpace - textHight *2 - vSpace) / 2;
        }else if( yOrder == 2){
            y = ( headSpace - textHight *2 - vSpace) / 2 + textHight + vSpace;
        }
        float baseX = contentRect.width() * ox_rate;
        float[] pts = {x+baseX, y+oY+textHight};
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

        float[] avaiLb = calcTextPoint(stockHolder.getAvaiAmtLb(), 1, 1/3.0f, topSpace);
        canvas.drawText(
                stockHolder.getAvaiAmtLb(),
                avaiLb[0], avaiLb[1],
                mLabelPaint);
        float[] avaiamt = calcTextPoint(stockHolder.getAvaiAmt(), 2, 1/3.0f, topSpace);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getAvaiAmt()),
                avaiamt[0], avaiamt[1],
                mLabelPaint);

        float[] exchgeLb = calcTextPoint(stockHolder.getExchangeLb(), 1, 2/3.0f, topSpace);
        canvas.drawText(
                stockHolder.getExchangeLb(),
                exchgeLb[0], exchgeLb[1],
                mLabelPaint);
        float[] exchgeamt = calcTextPoint(stockHolder.getExchange(), 2, 2/3.0f, topSpace);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getExchange()),
                exchgeamt[0], exchgeamt[1],
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

        float[] realPlLb = calcTextPoint(stockHolder.getPlLb(), 1, 1/3.0f, topSpace+headSpace);
        canvas.drawText(
                stockHolder.getPlLb(),
                realPlLb[0], realPlLb[1],
                mLabelPaint);
        Paint tempPain = mLabelPaint;
        if(stockHolder.getPl() > 0.01){
            tempPain = mUpPaint;
        }
        if(stockHolder.getPl() < 0.0){
            tempPain = mDownPaint;
        }
        float[] realPlamt = calcTextPoint(stockHolder.getPl(), 2, 1/3.0f, topSpace+headSpace);
        canvas.drawText(
                CommonUtil.formatNumer(stockHolder.getPl()),
                realPlamt[0], realPlamt[1],
                tempPain);

        float[] plLb = calcTextPoint(stockHolder.getRateLb(), 1, 2/3.0f, topSpace+headSpace);
        canvas.drawText(
                stockHolder.getRateLb(),
                plLb[0], plLb[1],
                mLabelPaint);
        float[] plamt = calcTextPoint(stockHolder.getRealRate(), 2, 2/3.0f, topSpace+headSpace);
        canvas.drawText(
                stockHolder.getRealRate(),
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
        float[] p4 = calcHoldPts(stockHolder.getHoldPlLb(), 1, 3/4.0f, orginY);
        canvas.drawText(
                stockHolder.getHoldPlLb(),
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
                stockHolder.getHoldShare()+"",
                avaiNum[0], avaiNum[1],
                mLabelPaint);
        float[] holdNum = calcDetailPts(stockHolder.getAvaiLabelShare(), 2, 1/4.0f, orginY);
        canvas.drawText(
                stockHolder.getAvaiLabelShare()+"",
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
        if(totalAmount == null){
            totalAmount = 0.0f;
        }
        stockHolder.setTotAmt(totalAmount);
        stockHolder.setAvaiAmt(totalAmount);
        stockHolder.setInitTotAmt(totalAmount);
    }

    public float reCalcHeadSpace(){
        float textHight = fontMetrics.bottom - fontMetrics.top;

        return textHight*2.5f;
    }

}
