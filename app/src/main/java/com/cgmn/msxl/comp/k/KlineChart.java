package com.cgmn.msxl.comp.k;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.*;
import com.cgmn.msxl.R;
import com.cgmn.msxl.db.AppSqlHelper;

import java.util.Map;

public class KlineChart extends View {

    private RectF contentRect;
    private KlinePaint klinePaint;
    private KlineGroup mData;

    public KlinePaint getKlinePaint() {
        return klinePaint;
    }

    public KlineChart(Context context) {
        this(context, null);
    }

    public KlineChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KlineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        contentRect = new RectF();
        klinePaint = new KlinePaint();

        klinePaint.setColors(
                getResources().getColor(R.color.kline_up),
                getResources().getColor(R.color.kline_down),
                getResources().getColor(R.color.kline_ave_5),
                getResources().getColor(R.color.kline_ave_10),
                getResources().getColor(R.color.kline_ave_20)
        );

        final AppSqlHelper dbHelper = new AppSqlHelper(context);
        Map<String, String> map =  dbHelper.getSystenSettings();
        klinePaint.setFirstIndex(map.get("FIRST_INDEX"));
        klinePaint.setSecondIndex(map.get("SECOND_INDEX"));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        contentRect.set(0, 0, w , h);
        klinePaint.setContentRect(contentRect);
        if(mData != null){
            invalidateView();
        }
    }

    /**
     * Sets a new data object for the chart. The data object contains all values
     * and information needed for displaying.
     */
    public void setData(KlineGroup data) {
        mData = data;
        klinePaint.setData(mData);
        mData.calcMinMax(0, mData.getNodes().size());
    }

    public void invalidateView() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mData != null){
            klinePaint.render(canvas);
        }
    }
}
