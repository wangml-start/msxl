package com.cgmn.msxl.ac;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.view.LineChartMarkView;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.data.TradeStatistic;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.*;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticActivity extends BaseOtherActivity {
    private static final String TAG = StatisticActivity.class.getSimpleName();
    private LineChart mLineChart;

    private Context mContext;
    protected BottomSheetDialog dialog;
    protected View commentView = null;

    //消息处理
    private Handler mHandler;
    private XAxis xAxis;                //X轴
    private YAxis leftYAxis;            //左侧Y轴
    private YAxis rightYaxis;           //右侧Y轴
    private Legend legend;              //图例

    private Integer trainType;
    private Integer userModelId;

    private TextView  tx_st_pl, tx_st_baseAmt,lb_st_plrate,lb_st_baseAmt,
            tx_st_ex,  tx_st_plrate, tx_st_leftAmt;
    private String title;

    private TradeStatistic statistic;

    @Override
    protected void init(){
        initMessageHandle();
        bindView();
        loadDatas();
    };

    @Override
    protected int getContentView() {
        return R.layout.statistics_layout;
    }

    @Override
    protected String setTitle(){
        return title;
    }

    @Override
    protected boolean showRight(){
        return false;
    };

    @Override
    protected void onCompletedClick(){
        showShareDialog();
    }

    private void bindView() {
        mContext = this;
        mLineChart = findViewById(R.id.lineChart);
        initChart(mLineChart);
        tx_st_pl = findViewById(R.id.tx_st_pl);
        tx_st_ex = findViewById(R.id.tx_st_ex);
        tx_st_plrate = findViewById(R.id.tx_st_plrate);
        tx_st_baseAmt = findViewById(R.id.tx_st_baseAmt);
        tx_st_leftAmt = findViewById(R.id.tx_st_leftAmt);
        lb_st_plrate = findViewById(R.id.lb_st_plrate);
        lb_st_baseAmt = findViewById(R.id.lb_st_baseAmt);
        txt_complete.setText("分享");

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("datas");
        if (bundle != null) {
            trainType = bundle.getInt("train_type");
            userModelId = bundle.getInt("user_model_id");
            title = bundle.getString("title");
        }

        if(trainType != StockHolder.EARNING_CURVE_SUMMARY){
            tx_st_baseAmt.setVisibility(View.GONE);
            tx_st_plrate.setVisibility(View.GONE);
            lb_st_plrate.setVisibility(View.GONE);
            lb_st_baseAmt.setVisibility(View.GONE);
        }
    }

    private void loadDatas() {
        CustmerToast.makeText(mContext, R.string.get_stock_datas).show();
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                if (trainType != null) {
                    params.put("trainType", trainType + "");
                }
                if (userModelId != null) {
                    params.put("userModelId", userModelId + "");
                }
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        "/stock/get_statistics", params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.REQUEST_SUCCESS;
                                try {
                                    message.obj = data.getStatistic();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    }
                                } catch (Exception e) {
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                }
                                mHandler.sendMessage(message);
                            }
                        });
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
            }
        });
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    statistic = (TradeStatistic) msg.obj;
                    showStatisticChart(statistic);
                } else if(msg.what == MessageUtil.PUBLISHED_COMMENT){
                    new ShowDialog().showTips(mContext, "分享成功");
                }else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    /**
     * 初始化图表
     */
    private void initChart(LineChart lineChart) {
        /***图表设置***/
        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        //是否可以拖动
        lineChart.setDragEnabled(true);
        //是否有触摸事件
        lineChart.setTouchEnabled(true);
        //缩放
        lineChart.setScaleEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        //设置XY轴动画效果
        lineChart.animateY(2500);
        lineChart.animateX(1500);
        lineChart.setBackgroundColor(Color.WHITE);
        //是否显示边界
        lineChart.setDrawBorders(false);
        Description description = new Description();
//        description.setText("需要展示的内容");
        description.setEnabled(false);
        lineChart.setDescription(description);

        /***XY轴的设置***/
        xAxis = lineChart.getXAxis();
        leftYAxis = lineChart.getAxisLeft();
        rightYaxis = lineChart.getAxisRight();
        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        //保证Y轴从0开始，不然会上移一点
//        leftYAxis.setAxisMinimum(0f);

        rightYaxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setDrawAxisLine(false); //不显示左侧Y轴
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        rightYaxis.setEnabled(false);
        xAxis.setLabelCount(8, false);

        leftYAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getYformatValue(value);
            }
        });
        leftYAxis.setLabelCount(6);

        /***折线图例 标签 设置***/
        legend = lineChart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);

    }

    public String getYformatValue(float value){
        if(trainType == StockHolder.EARNING_CURVE_SUMMARY){
            return CommonUtil.formatPercent(value);
        }else{
            return CommonUtil.formatAmt(value);
        }
    }

    /**
     * 设置线条填充背景颜色
     *
     * @param drawable
     */
    public void setChartFillDrawable(Drawable drawable) {
        if (mLineChart.getData() != null && mLineChart.getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet) mLineChart.getData().getDataSetByIndex(0);
            //避免在 initLineDataSet()方法中 设置了 lineDataSet.setDrawFilled(false); 而无法实现效果
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            mLineChart.invalidate();
        }
    }


    /**
     * 曲线初始化设置 一个LineDataSet 代表一条曲线
     *
     * @param lineDataSet 线条
     * @param color       线条颜色
     * @param mode
     */
    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircles(false);
        //设置曲线值的圆点是实心还是空心
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        //设置折线图填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        lineDataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry,
                                            int dataSetIndex, ViewPortHandler viewPortHandler) {
                return getYformatValue(value);
            }
        });
        lineDataSet.setDrawValues(false);
        if (mode == null) {
            //设置曲线展示为圆滑曲线（如果不设置则默认折线）
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }
    }


    /**
     * 展示曲线
     *
     * @param dataList 数据集合
     * @param name     曲线名称
     * @param color    曲线颜色
     */
    public void showLineChart(List<Float> dataList, String name, int color) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            /**
             * 在此可查看 Entry构造方法，可发现 可传入数值 Entry(float x, float y)
             * 也可传入Drawable， Entry(float x, float y, Drawable icon) 可在XY轴交点 设置Drawable图像展示
             */
            Entry entry = new Entry(i, dataList.get(i));
            entries.add(entry);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        LineData lineData = new LineData(lineDataSet);
        mLineChart.setData(lineData);
    }

    public void setMarkerView() {
        LineChartMarkView mv = new LineChartMarkView(
                this,
                xAxis.getValueFormatter(),
                leftYAxis.getValueFormatter());
        mv.setChartView(mLineChart);
        mLineChart.setMarker(mv);
        mLineChart.invalidate();
    }

    @Override
    public void finish() {
        super.finish();
    }

    protected void invalidCommentView() {
        /**
         * 解决bsd显示不全的情况
         */
        View parent = (View) commentView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        commentView.measure(0, 0);
        behavior.setPeekHeight(commentView.getMeasuredHeight());
    }

    private void showStatisticChart(TradeStatistic statistic) {
        List<Float> list = statistic.getList();
        if (CommonUtil.isEmpty(list)) {
            return;
        }
        list.add(0, 0f);
        showLineChart(list, "X轴代表训练次数", getResources().getColor(R.color.kline_up));
        Drawable drawable = getResources().getDrawable(R.drawable.fade_blue);
        setChartFillDrawable(drawable);
        setMarkerView();

        tx_st_baseAmt.setText(CommonUtil.formatNumer(statistic.getBaseAmt()));
        tx_st_leftAmt.setText(CommonUtil.formatNumer(statistic.getCashAmt()));
        tx_st_pl.setText(CommonUtil.formatNumer(statistic.getPl()));
        tx_st_plrate.setText(CommonUtil.formatPercent(statistic.getPl() / statistic.getBaseAmt()));
        if (statistic.getPl() > 0) {
            tx_st_pl.setTextColor(getResources().getColor(R.color.kline_up));
            tx_st_plrate.setTextColor(getResources().getColor(R.color.kline_up));
        } else if (statistic.getPl() < 0) {
            tx_st_pl.setTextColor(getResources().getColor(R.color.kline_down));
            tx_st_plrate.setTextColor(getResources().getColor(R.color.kline_down));
        }
        tx_st_ex.setText(CommonUtil.formatNumer(statistic.getFee()));
    }

    private String getComment(){
        FixStringBuffer text = new FixStringBuffer();
        String trianTypeStr = "趋势波段";
        if(trainType == StockHolder.LEADING_STRATEGY){
            trianTypeStr = "连板战法";
        }
        text.append("我在%s中通过%s次训练，获得总收益为%s ^_^",
                trianTypeStr, statistic.getList().size(),
                CommonUtil.formatPercent(statistic.getPl() / statistic.getBaseAmt()));

        return text.toString();
    }

    private void showShareDialog() {
        dialog = new BottomSheetDialog(mContext);
        if (commentView == null) {
            commentView = LayoutInflater.from(mContext).inflate(R.layout.share_dialog_layout, null);
        }
        if (commentView.getParent() != null) {
            ((ViewGroup) commentView.getParent()).removeView(commentView);
        }
        final EditText commentText = (EditText) commentView.findViewById(R.id.txt_comment);
        final Button bt_comment = (Button) commentView.findViewById(R.id.txt_publish);
        final NetImageView im_chart = commentView.findViewById(R.id.im_chart);
        dialog.setContentView(commentView);
        commentText.setText(getComment());
        final byte[] content = ImageUtil.getCompressBytes(mLineChart.getChartBitmap());
        im_chart.setImageContent(content);

        invalidCommentView();
        View.OnClickListener listener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.txt_publish) {
                    if (!TextUtils.isEmpty(commentText.getText())) {
                        Map<String, String> params = new HashMap<>();
                        params.put("comment", commentText.getText().toString().trim());
                        if(content != null && content.length > 0){
                            params.put("picture",  org.apache.shiro.codec.Base64.encodeToString(content));
                        }
                        dialog.dismiss();
                        uploadShare(params);
                    } else {
                        CustmerToast.makeText(mContext, "内容不能为空").show();
                    }
                }
            }
        };
        bt_comment.setOnClickListener(listener);
        dialog.show();
    }

    public void uploadShare(Map<String, String> p){
        OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                new OkHttpClientManager.Param("token", GlobalDataHelper.getToken(mContext)),
                new OkHttpClientManager.Param("picture", p.get("picture")),
                new OkHttpClientManager.Param("comment", p.get("comment"))
        };
        String url = String.format("%s%s",
                PropertyService.getInstance().getKey("serverUrl"), "/chat/publish_comment");
        OkHttpClientManager.postAsyn(url,
                new OkHttpClientManager.ResultCallback<BaseData>() {
                    @Override
                    public void onError(com.squareup.okhttp.Request request, Exception e) {
                        Message message = Message.obtain();
                        message.what = MessageUtil.EXCUTE_EXCEPTION;
                        message.obj = e;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onResponse(BaseData data) {
                        Message message = Message.obtain();
                        message.what = MessageUtil.PUBLISHED_COMMENT;
                        try {
                            message.obj = data.getRecordId();
                            Integer status = data.getStatus();
                            if (status == null || status == -1) {
                                throw new Exception(data.getError());
                            }
                        } catch (Exception e) {
                            message.what = MessageUtil.EXCUTE_EXCEPTION;
                            message.obj = e;
                        }
                        mHandler.sendMessage(message);
                    }
                }, params);
    }
}
