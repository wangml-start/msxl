package com.cgmn.msxl.ac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.comp.k.time.TimeShareChart;
import com.cgmn.msxl.comp.k.time.TimeShareGroup;
import com.cgmn.msxl.comp.pop.TradingPop;
import com.cgmn.msxl.comp.view.MyMarqueeView;
import com.cgmn.msxl.comp.view.StockHolderView;
import com.cgmn.msxl.data.SettledAccount;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.data.Trade;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.server_interface.TimeShare;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.MarqueeManager;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.RealTradeManage;
import com.cgmn.msxl.service.TokenInterceptor;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.GsonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.cgmn.msxl.utils.ShowDialog;
import com.helin.loadinglayout.LoadingLayout;
import com.squareup.okhttp.Request;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class KzzTimeShareActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = KzzTimeShareActivity.class.getSimpleName();
    private Handler mHandler;
    public Context mContxt;

    private LoadingLayout chartParent, holderParent;
    private TimeShareChart chart;
    Button bt_buy, bt_sell, bt_change, bt_exit;
    TextView lb_current_price, lb_current_rate,lb_current_time;

    private LinearLayout bottomBar;
    private TimeShareGroup timeShareGroup;
    private int trainType = StockHolder.KZZ_STRATEGY;

    Timer mTimer;
    TimerTask mTimerTask;
    Integer currentSpeed=1;

    MyMarqueeView marqueeview;
    MarqueeManager marqueeManager;
    StockHolderView stockView;
    RealTradeManage realtradeManage;

    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcastManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kzz_time_share_layout);
        bindView();
        playSpeed(currentSpeed);
        initMessageHandle();
        loadAccCash();
        loadTimeShareData();
        registerTokenListener();
    }

    private void bindView() {
        mContxt = this;
        bt_buy = findViewById(R.id.bt_buy);
        bt_sell = findViewById(R.id.bt_sell);
        bt_change = findViewById(R.id.bt_change);
        bt_exit = findViewById(R.id.bt_exit);
        bottomBar = findViewById(R.id.pop_div);

        bt_buy.setOnClickListener(this);
        bt_sell.setOnClickListener(this);
        bt_change.setOnClickListener(this);
        bt_exit.setOnClickListener(this);

        lb_current_price = findViewById(R.id.lb_current_price);
        lb_current_rate = findViewById(R.id.lb_current_rate);
        lb_current_time = findViewById(R.id.lb_current_time);


        chartParent = findViewById(R.id.chart_parent);
        holderParent = findViewById(R.id.holder_parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        chartParent.getLayoutParams().height = ((Double) (screenHeight * 0.45)).intValue();
        chartParent.setStateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTimeShareData();
            }
        });
        holderParent.setStateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAccCash();
            }
        });
        LinearLayout.LayoutParams bottomParams = (LinearLayout.LayoutParams) bottomBar.getLayoutParams();
        bottomParams.height = ((Double) (screenHeight * 0.055)).intValue();
        if(bottomParams.height > 80){
            bottomParams.height = 80;
        }
        setKlineBaseDatas(screenHeight); //千万不可删除

        bottomBar.setLayoutParams(bottomParams);
        chart = new TimeShareChart(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        chart.setShowDetails(false);
        chartParent.addView(chart);

        stockView = new StockHolderView(this);
        holderParent.addView(stockView);
        marqueeview = findViewById(R.id.marqueeview);
        marqueeManager = new MarqueeManager(mContxt, marqueeview);
        marqueeManager.startMarquee();

        realtradeManage = new RealTradeManage(); //只是传参数需要

    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    chartParent.showContent();
                    startChartInit((List<TimeShare>) msg.obj);
                    initStockHolder();
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContxt).handlerException((Exception) msg.obj);
                } else if(msg.what == MessageUtil.GET_CASH_ACC_SUCCESS){
                    holderParent.showContent();
                    SettledAccount acc = (SettledAccount) msg.obj;
                    stockView.initAccount(Double.valueOf(acc.getCashAmt()));
                    stockView.invalidateView();
                    if(acc.getVipPermission() == 0){
                        jumpToChargetPage("vip");
                    }else if(acc.getCashAmt().intValue() < 100){
                        jumpToChargetPage("charge");
                    }
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
            StockDetail stockDetail = new StockDetail();
            stockDetail.setStackCode(timeShareGroup.current.getStockCode());
            stockDetail.setStart(timeShareGroup.current.getPrice());
            realtradeManage.initkStatus();
            realtradeManage.setCurrentK(stockDetail);
        }else{
            showReloadView(chartParent);
        }
    }

    private void initStockHolder(){
        Double totalAmount = stockView.getStockHolder().getTotAmt();
        stockView.setStockHolder(new StockHolder());
        stockView.getStockHolder().setModelRecordId(1);
        stockView.getStockHolder().setTrainType(trainType);
        stockView.initAccount(totalAmount);
        stockView.invalidateView();
    }

    public void playSpeed(Integer speed){
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if(timeShareGroup != null && timeShareGroup.current != null){
                    timeShareGroup.onNextMinu();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onNextMunite();
                        }
                    });
                }
            }
        };
        mTimer = new Timer();
        Integer delay = 200;
//        if(currentSpeed > 0){
//            delay = 0;
//        }
        currentSpeed = speed;
        mTimer.schedule(mTimerTask, delay, 1500);
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
                        "/stock/kzz_time_share_datas", params);
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

    private void loadAccCash(){
        holderParent.showLoading();
        //获取资金账户信息
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContxt));
                params.put("train_type", trainType+"");
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/stock/account_info", params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                showReloadView(holderParent);
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = new RuntimeException(getString(R.string.get_cash_acc_fail));
                                mHandler.sendMessage(message);
                            }
                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.GET_CASH_ACC_SUCCESS;
                                try {
                                    message.obj = data.getSettledAccount();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        showReloadView(holderParent);
                                        throw new Exception(data.getError());
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

    private void onChageStock() {
//        stopTimeChart();
        settleThisTrading();
        loadTimeShareData();
        bt_buy.setEnabled(true);
        bt_sell.setEnabled(true);
    }

    private void onExit() {
        finish();
    }

    @Override
    public void onClick(View v) {
        v.setEnabled(false);
        if(v.getId() == R.id.bt_buy){
            if(stockView.getStockHolder().getAvaiAmt() < 100){
                new ShowDialog().showTips(mContxt, "当前账户资金不足！");
            }
            showPopFormBottom(v, "BUY");
        }else if(v.getId() == R.id.bt_sell){
            showPopFormBottom(v, "SELL");
        }else if(v.getId() == R.id.bt_change){
            onChageStock();
        }else if(v.getId() == R.id.bt_exit){
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
        }
        v.setEnabled(true);
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

            stockView.getStockHolder().nextPrice(timeShareGroup.current.getPrice(), true);
            stockView.invalidateView();
        }
    }

    public void jumpToChargetPage(String type){
        if(type.equals("vip")){
            new ShowDialog().show(mContxt, "当前权限不足，前往开通?", new ShowDialog.OnBottomClickListener() {
                @Override
                public void positive() {
                    Intent intent = new Intent(mContxt, VIPActivity.class);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void negative() {
                    //取消操作
                }
            });
        }else if(type.equals("charge")){
            new ShowDialog().show(mContxt, "账户可用资金不足，前往充值?", new ShowDialog.OnBottomClickListener() {
                @Override
                public void positive() {
                    Intent intent = new Intent(mContxt, ChargeActivity.class);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void negative() {
                    //取消操作
                }
            });
        }
    }

    private void registerTokenListener(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                marqueeManager.tiemrCancel();
                finish();
            }
        };
        broadcastManager = LocalBroadcastManager.getInstance(mContxt);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ReceiverMessage.STOP_TIMER_STATK); //监听的事件key
        broadcastManager.registerReceiver(receiver, intentFilter);

        OkHttpClientManager.getInstance().addIntercept(new TokenInterceptor(mContxt));
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

    private void settleThisTrading(){
        Float price = timeShareGroup.current.getPrice();
        stockView.getStockHolder().settleTrading(price);
        if(stockView.getStockHolder().getNodes() == null ||
                stockView.getStockHolder().getNodes().size() == 0){
            return;
        }

        final LinkedList<Trade> nodes = stockView.getStockHolder().getNodes();
        if(CommonUtil.isEmpty(nodes)){
            return;
        }
        // send trade to server
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                final String token = GlobalDataHelper.getToken(mContxt);
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", token),
                        new OkHttpClientManager.Param("content", GsonUtil.toJson(nodes))};
                String url = String.format("%s%s",
                        ConstantHelper.serverUrl, "/stock/upload_trading");
                OkHttpClientManager.postAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                Log.d(TAG, "UPLOAD TRADING FAILED!");
                            }
                            @Override
                            public void onResponse(BaseData data) {
                                if(data.getStatus() == 0){
                                    nodes.clear();
                                    Log.d(TAG, "UPLOAD TRADING SUCCESS!");
                                }else{
                                    Message message = Message.obtain();
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = new RuntimeException(data.getError());
                                    mHandler.sendMessage(message);
                                }
                            }
                        },
                        params);
                Log.e(TAG,"NAME="+Thread.currentThread().getName());
            }
        });

    }

    public void showPopFormBottom(View view, String action) {
        TradingPop popWin = new TradingPop(this,
                stockView.getStockHolder(), realtradeManage, action);
//        设置Popupwindow显示位置（从底部弹出）
        popWin.setTimeShare(timeShareGroup);
        popWin.showAtLocation(findViewById(R.id.pop_div),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        final WindowManager.LayoutParams[] params = {getWindow().getAttributes()};
        //当弹出Popupwindow时，背景变半透明
        params[0].alpha = 0.7f;
        getWindow().setAttributes(params[0]);
        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        popWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params[0] = getWindow().getAttributes();
                params[0].alpha = 1f;
                getWindow().setAttributes(params[0]);
                stockView.invalidateView();

                if (stockView.getStockHolder().getHoldShare() == 0) {
                    bt_change.setEnabled(true);
                } else {
                    bt_change.setEnabled(false);
                }
            }
        });
    }

    private void setKlineBaseDatas(float height){
        KlineStyle.initSize();
        float baseHeight = 480f;
        KlineStyle.pxScaleRate = height/baseHeight;
        KlineStyle.resetSize();

    }

    @Override
    public void finish() {
        stopTimeChart();
        settleThisTrading();
        super.finish();
    }
}