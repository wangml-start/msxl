package com.cgmn.msxl.comp.k;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;


public class KLineContent extends RelativeLayout{
    private KlineChart chart;
    private ScaleGestureDetector mScaleGestureDetector = null;
    private GestureDetector mGestureDetector;
    protected KlineGroup data;
    private float x1 = 0;
    private float x2 = 0;
    //记录滑动值
    private static final String TAG = KLineContent.class.getSimpleName();
    public KLineContent(Context context) {
        this(context, null);
    }

    public KLineContent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLineContent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        chart = new KlineChart(context);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        addView(chart);

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                whenSliding(distanceX);
                return true;
            }

        }, null, true);

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                whenScale(detector.getScaleFactor());
//                Log.d(TAG, "ScaleFactor:" + detector.getScaleFactor());
                return true;
            }
        });
    }


    public void whenSliding(float distanceX){
        int count = (int) (Math.abs(distanceX)/KlineStyle.kWidth);
        if(distanceX < 0){ //右划
            if(count > 0){
                while(count>0 && chart.getKlinePaint().getStartx() > 0){
                    chart.getKlinePaint().setStartx(chart.getKlinePaint().getStartx() - 1);
                    chart.getKlinePaint().setEndx(chart.getKlinePaint().getEndx() - 1);
                    count--;
                }
                chart.invalidateView();
            }
        }else if(distanceX > 0){
            if(count > 0){
                while(count>0 && chart.getKlinePaint().getEndx() < data.getNodes().size()){
                    chart.getKlinePaint().setStartx(chart.getKlinePaint().getStartx() + 1);
                    chart.getKlinePaint().setEndx(chart.getKlinePaint().getEndx() + 1);
                    count--;
                }
                chart.invalidateView();
            }
        }
    }

    public void whenScale(float rate){
        float oldWidth = KlineStyle.kWidth;
        float newWidth = KlineStyle.kWidth*rate;
        if(newWidth > KlineStyle.kWidthMax){
            KlineStyle.kWidth = KlineStyle.kWidthMax;
        }else if(newWidth < KlineStyle.kWidthmin){
            KlineStyle.kWidth = KlineStyle.kWidthmin;
        }else{
            KlineStyle.kWidth = KlineStyle.kWidth*rate;
        }
        float ohterRate = KlineStyle.kWidth/oldWidth;
        KlineStyle.mBarSpace = KlineStyle.mBarSpace*ohterRate;
        KlineStyle.kLineBold = KlineStyle.kLineBold*ohterRate;

        int count = chart.getKlinePaint().getEndx() - chart.getKlinePaint().getStartx();
        int visibleCount = (int) ((chart.getWidth()-KlineStyle.rightWidth) / (KlineStyle.kWidth+KlineStyle.mBarSpace));
        int abs = Math.abs(count-visibleCount);
        if(count > visibleCount){
            while(abs > 0){//放大
                chart.getKlinePaint().setStartx(chart.getKlinePaint().getStartx() + 1);
                abs--;
                if(abs > 0){
                    chart.getKlinePaint().setEndx(chart.getKlinePaint().getEndx() - 1);
                    abs--;
                }
            }
        }else{//缩小
            while(abs > 0){
                if(chart.getKlinePaint().getStartx() > 0){
                    chart.getKlinePaint().setStartx(chart.getKlinePaint().getStartx() - 1);
                    abs--;
                }
                if(abs > 0 && chart.getKlinePaint().getEndx() < data.getNodes().size()){
                    chart.getKlinePaint().setEndx(chart.getKlinePaint().getEndx() + 1);
                    abs--;
                }
                if(chart.getKlinePaint().getStartx() == 0 && chart.getKlinePaint().getEndx() == data.getNodes().size()){
                    break;
                }
            }
        }
        chart.invalidateView();
    }


    public void invalidateView() {
        invalidate();
        chart.invalidateView();
    }

    public void setData(KlineGroup data) {
        this.data = data;
        chart.setData(data);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        return true;
    }

}
