package com.cgmn.msxl.ac;

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
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.comp.k.time.TimeShareChart;
import com.cgmn.msxl.comp.k.time.TimeShareGroup;
import com.cgmn.msxl.data.SimpleStockHolder;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.TimeShare;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;
import com.cgmn.msxl.utils.ShowDialog;
import com.helin.loadinglayout.LoadingLayout;
import com.squareup.okhttp.Request;

import java.util.*;


public class NormalTimeShareActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = NormalTimeShareActivity.class.getSimpleName();
    private Handler mHandler;
    public Context mContxt;

    private LoadingLayout chartParent;
    private TimeShareChart chart;
    Button bt_buy, bt_sell, bt_change, bt_exit;
    Button bt_1, bt_3, bt_10, bt_30, bt_60, bt_120;
    TextView lb_current_price, lb_current_rate,lb_current_speed,lb_current_time;
    TextView lb_buy_cost, lb_rate;

    private LinearLayout bottomBar, pop_div_text, speed_div;

    SimpleStockHolder holder;
    private TimeShareGroup timeShareGroup;

    Timer mTimer;
    TimerTask mTimerTask;
    Integer currentSpeed=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_time_share_layout);
        bindView();
        initMessageHandle();
        loadTimeShareData();
    }

    private void bindView() {
        mContxt = this;
        holder = new SimpleStockHolder();
        bt_buy = findViewById(R.id.bt_buy);
        bt_sell = findViewById(R.id.bt_sell);
        bt_change = findViewById(R.id.bt_change);
        bt_exit = findViewById(R.id.bt_exit);
        bottomBar = findViewById(R.id.pop_div);
        pop_div_text = findViewById(R.id.pop_div_text);
        speed_div = findViewById(R.id.speed_div);

        bt_buy.setOnClickListener(this);
        bt_sell.setOnClickListener(this);
        bt_change.setOnClickListener(this);
        bt_exit.setOnClickListener(this);

        bt_1 = findViewById(R.id.bt_1);
        bt_3 = findViewById(R.id.bt_3);
        bt_10 = findViewById(R.id.bt_10);
        bt_30 = findViewById(R.id.bt_30);
        bt_60 = findViewById(R.id.bt_60);
        bt_120 = findViewById(R.id.bt_120);

        bt_1.setOnClickListener(this);
        bt_3.setOnClickListener(this);
        bt_10.setOnClickListener(this);
        bt_30.setOnClickListener(this);
        bt_60.setOnClickListener(this);
        bt_120.setOnClickListener(this);

        lb_current_price = findViewById(R.id.lb_current_price);
        lb_current_rate = findViewById(R.id.lb_current_rate);
        lb_current_speed = findViewById(R.id.lb_current_speed);
        lb_buy_cost = findViewById(R.id.lb_buy_cost);
        lb_rate = findViewById(R.id.lb_rate);
        lb_current_time = findViewById(R.id.lb_current_time);


        chartParent = findViewById(R.id.chart_parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        chartParent.getLayoutParams().height = ((Double) (screenHeight * 0.35)).intValue();
        chartParent.setStateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTimeShareData();
            }
        });
        setKlineBaseDatas(screenHeight);

        LinearLayout.LayoutParams bottomParams = (LinearLayout.LayoutParams) bottomBar.getLayoutParams();
        bottomParams.height = ((Double) (screenHeight * 0.055)).intValue();
        if(bottomParams.height > 80){
            bottomParams.height = 80;
        }
        bottomBar.setLayoutParams(bottomParams);
        pop_div_text.setLayoutParams(bottomParams);

        LinearLayout.LayoutParams bottomParams2 = (LinearLayout.LayoutParams) speed_div.getLayoutParams();
        bottomParams2.height = ((Double) (screenHeight * 0.055)).intValue();
        if(bottomParams2.height > 80){
            bottomParams2.height = 80;
        }
        speed_div.setLayoutParams(bottomParams2);

        chart = new TimeShareChart(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        chart.setShowDetails(false);
        chartParent.addView(chart);
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    chartParent.showContent();
                    startChartInit((List<TimeShare>) msg.obj);
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContxt).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    private void startChartInit(List<TimeShare> list) {
        timeShareGroup = new TimeShareGroup();
        if(timeShareGroup.init(list)){
            chart.setData(timeShareGroup);
            onNextMunite();
            currentSpeed = 0;
            playSpeed(1);
        }else{
            showReloadView(chartParent);
        }
    }

    public void showReloadView(final LoadingLayout loadingLayout) {
        loadingLayout.post(new Runnable() {
            @Override
            public void run() {
                loadingLayout.showState("加载失败，点击重试！");
            }
        });
    }

    private void loadTimeShareData() {
        chartParent.showLoading();
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContxt));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/stock/normal_time_share_datas", params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                showReloadView(chartParent);
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
                                    message.obj = data.getTimeShareList();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        showReloadView(chartParent);
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


    private void setKlineBaseDatas(float height) {
        KlineStyle.initSize();
        float baseHeight = 480f;
        KlineStyle.pxScaleRate = height / baseHeight;
        KlineStyle.resetSize();

    }


    private void updateReceiveRate() {
        lb_buy_cost.setText("成本： " + CommonUtil.formatNumer(holder.getCostPrice()));
        lb_rate.setText("收益率: " + holder.getRateStr());
        if (holder.getRate() > 0) {
            lb_rate.setTextColor(getResources().getColor(R.color.kline_up));
        } else if (holder.getRate() < 0) {
            lb_rate.setTextColor(getResources().getColor(R.color.kline_down));
        } else {
            lb_rate.setTextColor(getResources().getColor(R.color.text_topbar));
        }

        if (holder.needSettle()) {
            Log.i(TAG, "Common In");
            bt_buy.setEnabled(false);
            if(holder.canSell()){
                bt_sell.setEnabled(true);
            }else{
                bt_sell.setEnabled(false);
            }
        } else {
            bt_sell.setEnabled(false);
            bt_buy.setEnabled(true);
        }

        if (!holder.needSettle()) {
            bt_change.setEnabled(true);
        } else {
            bt_change.setEnabled(false);
        }
    }

    private void settleThisTrading() {
        if (!holder.needSettle()) {
            return;
        }
        // send trade to server
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                final String token = GlobalDataHelper.getToken(mContxt);
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", token),
                        new OkHttpClientManager.Param("rate", holder.getRate().toString()),
                        new OkHttpClientManager.Param("train_type", "2"),
                        new OkHttpClientManager.Param("code", holder.getCode())};
                String url = String.format("%s%s",
                        ConstantHelper.serverUrl, "/stock/normal_upload_trading");
                OkHttpClientManager.postAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                Log.d(TAG, "UPLOAD TRADING FAILED!");
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                if (data.getStatus() == 0) {
                                    holder.resetHolder();
                                    Log.d(TAG, "UPLOAD TRADING SUCCESS!");
                                } else {
                                    Message message = Message.obtain();
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = new RuntimeException(data.getError());
                                    mHandler.sendMessage(message);
                                }
                            }
                        },
                        params);
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
            }
        });

    }

    public void buySellAction(String action) {
        if(timeShareGroup.current == null){
            return;
        }
        Float price = timeShareGroup.current.getPrice();
        String time = timeShareGroup.timer.getTimeMinute();
        if (action.equals("BUY")) {
            holder.onBuy(price, timeShareGroup.current.getStockCode());
            updateReceiveRate();
            timeShareGroup.setOpInfo("B", price, time);
        } else {
            if (holder.canSell()) {
                settleThisTrading();
                updateReceiveRate();
                timeShareGroup.setOpInfo("S", price, time);
            }
        }
    }

    private void onChageStock() {
        mTimer.cancel();
        mTimerTask.cancel();
        mTimer=null;
        mTimerTask=null;
        loadTimeShareData();
        bt_buy.setEnabled(true);
        bt_sell.setEnabled(false);
        //初始资金问题
    }

    private void onExit() {
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_exit) {
            new ShowDialog().show(mContxt, getString(R.string.sure_to_do_this), new ShowDialog.OnBottomClickListener() {
                @Override
                public void positive() {
                    //确定操作
                    onExit();
                }

                @Override
                public void negative() {
                    //取消操作
                }
            });
        }else if(v.getId() == R.id.bt_1){
            playSpeed(1);
        }else if(v.getId() == R.id.bt_3){
            playSpeed(3);
        }else if(v.getId() == R.id.bt_10){
            playSpeed(10);
        }else if(v.getId() == R.id.bt_30){
            playSpeed(30);
        }else if(v.getId() == R.id.bt_60){
            playSpeed(60);
        }else if(v.getId() == R.id.bt_120){
            playSpeed(120);
        }else if(v.getId() == R.id.bt_change){
            onChageStock();
        }else if(v.getId() == R.id.bt_buy){
            buySellAction("BUY");
        }else if(v.getId() == R.id.bt_sell){
            buySellAction("SELL");
        }
    }

    public void onNextMunite(){
        chart.invalidateView();
        if(timeShareGroup.current != null){
            float currentPrice = timeShareGroup.current.getPrice();
            lb_current_price.setText(CommonUtil.formatNumer(currentPrice));
            lb_current_rate.setText(timeShareGroup.currentRate());
            lb_current_time.setText(timeShareGroup.timer.showTimeStr());
            if(currentPrice > timeShareGroup.lastClosePrice){
                lb_current_price.setTextColor(getResources().getColor(R.color.kline_up));
                lb_current_rate.setTextColor(getResources().getColor(R.color.kline_up));
            }else if(currentPrice < timeShareGroup.lastClosePrice){
                lb_current_price.setTextColor(getResources().getColor(R.color.kline_down));
                lb_current_rate.setTextColor(getResources().getColor(R.color.kline_down));
            }else{
                lb_current_price.setTextColor(getResources().getColor(R.color.text_topbar));
                lb_current_rate.setTextColor(getResources().getColor(R.color.text_topbar));
            }
            holder.nextPrice(timeShareGroup.current.getPrice(), true);
            updateReceiveRate();
        }
    }

    public void playSpeed(Integer speed){
        if(currentSpeed != speed){
            lb_current_speed.setText("X"+speed);
            stopTimeChart();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    timeShareGroup.onNextStep();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onNextMunite();
                        }
                    });
                }
            };
            mTimer = new Timer();
            Integer delay = 3000;
            if(currentSpeed > 0){
                delay = 0;
            }
            currentSpeed = speed;
            mTimer.schedule(mTimerTask, delay, 3000/currentSpeed);
        }
    }

    private void stopTimeChart() {
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }


    @Override
    public void finish() {
        stopTimeChart();
        settleThisTrading();
        super.finish();
    }
}