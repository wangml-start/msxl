package com.cgmn.msxl.comp.k.time;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TimeShareChart extends View {
    private RectF contentRect;
    private TimeSharePaint chartPaint;
    private TimeShareGroup mData;

    public TimeShareChart(Context context) {
        this(context, null);
    }

    public TimeShareChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeShareChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        contentRect = new RectF();
        chartPaint = new TimeSharePaint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        contentRect.set(0, 0, w , h);
        chartPaint.setContentRect(contentRect);
        if(mData != null){
            invalidateView();
        }
    }


    public void setData(TimeShareGroup data) {
        mData = data;
        chartPaint.setData(mData);
    }

    public void invalidateView() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mData != null){
            chartPaint.render(canvas);
        }
    }
}
