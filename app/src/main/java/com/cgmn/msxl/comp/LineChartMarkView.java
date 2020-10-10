package com.cgmn.msxl.comp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.utils.CommonUtil;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class LineChartMarkView extends MarkerView {

    private TextView tvDate;
    private TextView tvValue;
    private IAxisValueFormatter xAxisValueFormatter;

    public LineChartMarkView(Context context, IAxisValueFormatter xAxisValueFormatter) {
        super(context, R.layout.layout_markview);
        this.xAxisValueFormatter = xAxisValueFormatter;

        tvDate = findViewById(R.id.tv_date);
        tvValue = findViewById(R.id.tv_value);
    }

    @SuppressLint({"SetTextI18n"})
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        //展示自定义X轴值 后的X轴内容
        tvDate.setText(xAxisValueFormatter.getFormattedValue(e.getX(), null));
        tvValue.setText(CommonUtil.formatPercent(e.getY()));
        if(e.getY() > 0){
            tvValue.setTextColor(getResources().getColor(R.color.kline_up));
        }else if(e.getY() < 0){
            tvValue.setTextColor(getResources().getColor(R.color.kline_down));
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}