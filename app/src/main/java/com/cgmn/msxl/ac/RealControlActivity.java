package com.cgmn.msxl.ac;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.k.KlineChart;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.service.RealTradeManage;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.FixStringBuffer;
import com.cgmn.msxl.utils.MessageUtil;
import com.google.gson.Gson;

public class RealControlActivity extends AppCompatActivity {
    private static final String TAG = RealControlActivity.class.getSimpleName();
    private Context mContxt;
    private KlineChart chart;
    //消息处理
    private Handler mHandler;

    private LinearLayout chartParent;

    private Gson gson;

    private RealTradeManage realtradeManage;
    TextView lb_open_price;
    TextView lb_close_price;
    TextView lb_open_rate;
    TextView lb_close_rate;
    TextView lb_left_day;
    TextView lb_left_s;
    Button bt_next;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.klinecontrol_layout);
        bindView();
        initMessageHandle();
        loadKLineSet();
    }

    private void loadKLineSet(){
        CustmerToast.makeText(mContxt, R.string.get_stock_datas).show();
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        "/stock/getKlineSet", null);
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
                                    message.obj = data.getKLineSet();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    } else {
                                        //存数据
                                        AppSqlHelper sqlHeper = new AppSqlHelper(mContxt);
                                        ContentValues values = new ContentValues();
                                        values.put("content", gson.toJson(data.getKLineSet()));
                                        values.put("data_type", "K_LINE_SET");
                                        sqlHeper.upsert("temp_data_save", values, "data_type");
                                    }
                                } catch (Exception e) {
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                }
                                mHandler.sendMessage(message);
                            }
                        });
                Log.e(TAG,"NAME="+Thread.currentThread().getName());
            }
        });
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    realtradeManage.setKlineset((KlineSet) msg.obj) ;
                    startChartInit();
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    AppSqlHelper sqlHeper = new AppSqlHelper(mContxt);
                    String json = sqlHeper.getKlinJsonStr();
                    if(!CommonUtil.isEmpty(json)){
                        KlineSet set = gson.fromJson(json, KlineSet.class);
                        realtradeManage.setKlineset(set);
                        startChartInit();
                    }else{
                        Exception exception = (Exception) msg.obj;
                        FixStringBuffer mes = new FixStringBuffer();
                        GlobalExceptionHandler.getInstance(mContxt).handlerException(exception);
                        mes.append(getString(R.string.ge_stock_info_failed));
                        //TODO: 异常处理
                        CustmerToast.makeText(mContxt, mes.toString()).show();
                    }
                }
                return false;
            }
        });
    }

    private void startChartInit(){
        if(realtradeManage.getKlineset() == null) {
            return;
        }
        realtradeManage.showNextOpen();
        chart.setData(realtradeManage.getGroup());
        chart.notifyDataSetChanged(true);
        chartParent.addView(chart);

        updateTopBar();
    }

    private void updateTopBar(){
        StockDetail current = realtradeManage.getCurrentK();
        lb_open_price.setText("开盘价： " + current.getStart());
        lb_open_rate.setText("涨跌:  " + current.getOpenrate());
        lb_close_price.setText("收盘价： 00.00");
        lb_close_rate.setText("涨跌:  00.00%");
        lb_left_day.setText("剩余: " + realtradeManage.getLeftDay() + " 天");
        lb_close_price.setTextColor(getResources().getColor(R.color.kline_ave_5));
        lb_close_rate.setTextColor(getResources().getColor(R.color.kline_ave_5));
    }

    private void bindView(){
        mContxt = this;
        gson = new Gson();
        chart = new KlineChart(this);
        realtradeManage = new RealTradeManage();

        lb_open_price = findViewById(R.id.lb_open_price);
        lb_close_price = findViewById(R.id.lb_close_price);
        lb_open_rate = findViewById(R.id.lb_open_rate);
        lb_close_rate = findViewById(R.id.lb_close_rate);
        lb_left_day = findViewById(R.id.lb_left_day);
        lb_left_s = findViewById(R.id.lb_left_s);
        bt_next = findViewById(R.id.bt_next);

        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(RealTradeManage.OPEN.equals(realtradeManage.getkStatus())){
                    realtradeManage.showNextClose();
                    chart.setData(realtradeManage.getGroup());
                    chart.notifyDataSetChanged(true);
                    StockDetail current = realtradeManage.getCurrentK();
                    lb_close_price.setText("收盘价：" + current.getEnd());
                    lb_close_rate.setText("涨跌: " + current.getUpRate());
                    if(current.getEnd() > current.getStart()){
                        lb_close_price.setTextColor(getResources().getColor(R.color.kline_up));
                        lb_close_rate.setTextColor(getResources().getColor(R.color.kline_up));
                    }else{
                        lb_close_price.setTextColor(getResources().getColor(R.color.kline_down));
                        lb_close_rate.setTextColor(getResources().getColor(R.color.kline_down));
                    }
                }else{
                    if(realtradeManage.showNextOpen()){
                        chart.setData(realtradeManage.getGroup());
                        chart.notifyDataSetChanged(true);
                        updateTopBar();
                    }else{
                        //TODO: end
                    }
                }

            }
        });

        chartParent = findViewById(R.id.chart_parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) chartParent.getLayoutParams();
        linearParams.height = ((Double)(screenHeight * 0.6)).intValue();// 控件的高强制设成20
        chartParent.setLayoutParams(linearParams);
    }
}
