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
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.k.KLineContent;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.comp.pop.TradingPop;
import com.cgmn.msxl.comp.view.MyMarqueeView;
import com.cgmn.msxl.comp.view.StockHolderView;
import com.cgmn.msxl.data.SettingItem;
import com.cgmn.msxl.data.SettledAccount;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.data.Trade;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.AutoNextListener;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.MarqueeManager;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.RealTradeManage;
import com.cgmn.msxl.service.TokenInterceptor;
import com.cgmn.msxl.service.TradeAutoRunManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.GsonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.cgmn.msxl.utils.PlayVoiceUtils;
import com.cgmn.msxl.utils.ShowDialog;
import com.helin.loadinglayout.LoadingLayout;
import com.squareup.okhttp.Request;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KzzXLActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = RealControlActivity.class.getSimpleName();
    private Context mContxt;
    private KLineContent chart;
    private StockHolderView stockView;
    //设置的模式
    List<SettingItem> modeList;

    private int trainType = StockHolder.KZZ_STRATEGY;
    private int userModelId = 1;
    //消息处理
    private Handler mHandler;

    private LoadingLayout chartParent, holderParent;
    private LinearLayout bottomBar;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcastManager;

    private RealTradeManage realtradeManage;
    TextView lb_open_price;
    TextView lb_close_price;
    TextView lb_left_day;
    TextView kzz_g_zf;
    Button bt_next, bt_buy,bt_sell, bt_change, bt_exit;
    MyMarqueeView marqueeview;
    MarqueeManager marqueeManager;
    TradeAutoRunManager autoRunManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kzz_prictice_layout);
        bindView();
        initMessageHandle();
        loadAccCash();
        loadKLineSet();
        registerTokenListener();
    }

    private String getUrl(){
        String action = "/stock/get_kzz_market";
        Map<String, String> params = new HashMap<>();
        params.put("token", GlobalDataHelper.getToken(mContxt));
        return CommonUtil.buildGetUrl(
                ConstantHelper.serverUrl,
                action, params);
    }

    private void loadKLineSet(){
        chartParent.showLoading();
        if(autoRunManager != null){
            autoRunManager.pause();
        }
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                String url = getUrl();
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
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
                                    message.obj = data.getKLineSet();
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
                Log.e(TAG,"NAME="+Thread.currentThread().getName());
            }
        });
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    chartParent.showContent();
                    realtradeManage.resetManager();
                    realtradeManage.setKlineset((KlineSet) msg.obj) ;
                    startChartInit();
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
                }else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContxt).handlerException((Exception) msg.obj);
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
        chart.invalidateView();
        updateTopBar();
        initStockHolder();
        if(autoRunManager != null){
            autoRunManager.resetManager();
        }
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

    private void initStockHolder(){
        Double totalAmount = stockView.getStockHolder().getTotAmt();
        stockView.setStockHolder(new StockHolder());
        stockView.getStockHolder().setModelRecordId(userModelId);
        stockView.getStockHolder().setTrainType(trainType);
        stockView.initAccount(totalAmount);
        stockView.invalidateView();
        stockView.getStockHolder().setModeList(modeList);
    }

    private void updateTopBar(){
        StockDetail current = realtradeManage.getCurrentK();
        StockDetail last = realtradeManage.getLastK();
        lb_open_price.setText("开盘价： " + CommonUtil.formatNumer(current.getStart()));
        lb_left_day.setText("剩余: " + realtradeManage.getLeftDay() + " 天");
        lb_close_price.setText("涨幅：0%");
        lb_close_price.setTextColor(getResources().getColor(R.color.text_topbar));
        lb_open_price.setTextColor(getResources().getColor(R.color.text_topbar));
        if(current != null && last != null){
            float zf = (current.getStart()-last.getEnd())/last.getEnd();
            lb_close_price.setText("涨幅：" + CommonUtil.formatPercent(zf));
            if (zf > 0) {
                lb_close_price.setTextColor(getResources().getColor(R.color.kline_up));
            } else {
                lb_close_price.setTextColor(getResources().getColor(R.color.kline_down));
            }
        }
        kzz_g_zf.setText("0%");
        String kzz_pre_close = realtradeManage.getKlineset().getKzzPreClose();
        if(current != null && !CommonUtil.isEmpty(kzz_pre_close)){
            float pre_close = Float.valueOf(kzz_pre_close);
            float g_zf = (current.getStart() - pre_close) / pre_close;
            kzz_g_zf.setText(CommonUtil.formatPercent(g_zf));
            if (current.getEnd() > pre_close) {
                kzz_g_zf.setTextColor(getResources().getColor(R.color.kline_up));
            } else {
                kzz_g_zf.setTextColor(getResources().getColor(R.color.kline_down));
            }
        }
    }

    private void bindView(){
        mContxt = this;
        realtradeManage = new RealTradeManage();

        lb_open_price = findViewById(R.id.lb_open_price);
        lb_close_price = findViewById(R.id.lb_close_price);
        lb_left_day = findViewById(R.id.lb_left_day);
        kzz_g_zf = findViewById(R.id.kzz_g_zf);
        bt_next = findViewById(R.id.bt_next);
        bt_buy = findViewById(R.id.bt_buy);
        bt_sell = findViewById(R.id.bt_sell);
        bt_change = findViewById(R.id.bt_change);
        bt_exit = findViewById(R.id.bt_exit);
        bottomBar = findViewById(R.id.pop_div);

        bt_next.setOnClickListener(this);
        bt_buy.setOnClickListener(this);
        bt_sell.setOnClickListener(this);
        bt_change.setOnClickListener(this);
        bt_exit.setOnClickListener(this);



        chartParent = findViewById(R.id.chart_parent);
        holderParent = findViewById(R.id.holder_parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        chartParent.getLayoutParams().height=((Double)(screenHeight * 0.5)).intValue();
        chartParent.setStateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadKLineSet();
            }
        });
        holderParent.setStateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAccCash();
            }
        });
        setKlineBaseDatas(screenHeight);

        LinearLayout.LayoutParams bottomParams =(LinearLayout.LayoutParams) bottomBar.getLayoutParams();
        bottomParams.height = ((Double)(screenHeight * 0.055)).intValue();
        if(bottomParams.height > 80){
            bottomParams.height = 80;
        }
        bottomBar.setLayoutParams(bottomParams);

        chart = new KLineContent(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        stockView = new StockHolderView(this);
        chartParent.addView(chart);
        holderParent.addView(stockView);

        marqueeview = findViewById(R.id.marqueeview);
        marqueeManager = new MarqueeManager(mContxt, marqueeview);
        marqueeManager.startMarquee();

        final AppSqlHelper dbHelper = new AppSqlHelper(mContxt);
        Map<String, String> map =  dbHelper.getSystenSettings();

        if (map.get("AUTO_NEXT_STEP") == null || "0".equals(map.get("AUTO_NEXT_STEP"))) {
            if (map.get("TREND_TIME") == null || Integer.valueOf(map.get("TREND_TIME")) > 0) {
                //记时3秒自动下一步
                autoRunManager = new TradeAutoRunManager();
                autoRunManager.setListener(new AutoNextListener() {
                    @Override
                    public void onTicket(Integer sec) {
                        bt_next.setText(String.format("%s%ss", getResources().getString(R.string.next), sec));
                    }

                    @Override
                    public void onComplete() {
                        bt_next.setText(String.format("%s", getResources().getString(R.string.next)));
                        onNextClick();
                        PlayVoiceUtils.getInstance().PlayMusic(mContxt);
                    }
                });
                autoRunManager.startManager();
                if (map.get("TREND_TIME") == null) {
                    autoRunManager.setTotal(2);
                } else {
                    autoRunManager.setTotal(Integer.valueOf(map.get("TREND_TIME")));
                }
            }

        }
    }

    private void onNextClick(){
        if(RealTradeManage.OPEN.equals(realtradeManage.getkStatus())){
            realtradeManage.showNextClose();
            chart.setData(realtradeManage.getGroup());
            chart.invalidateView();
            StockDetail current = realtradeManage.getCurrentK();
            StockDetail last = realtradeManage.getLastK();
            float zf = (current.getEnd()-current.getStart())/current.getStart();
            lb_close_price.setText("涨幅：" + CommonUtil.formatPercent(zf));
            if(current.getEnd() > last.getEnd()){
                lb_close_price.setTextColor(getResources().getColor(R.color.kline_up));
            }else{
                lb_close_price.setTextColor(getResources().getColor(R.color.kline_down));
            }
            String kzz_pre_close = realtradeManage.getKlineset().getKzzPreClose();
            if(!CommonUtil.isEmpty(kzz_pre_close)){
                float pre_close = Float.valueOf(kzz_pre_close);
                float g_zf = (current.getEnd()-pre_close)/pre_close;
                kzz_g_zf.setText(CommonUtil.formatPercent(g_zf));
                if(current.getEnd() > pre_close){
                    kzz_g_zf.setTextColor(getResources().getColor(R.color.kline_up));
                }else{
                    kzz_g_zf.setTextColor(getResources().getColor(R.color.kline_down));
                }
            }
            stockView.getStockHolder().nextPrice(current.getEnd(), false);
            if(autoRunManager != null){
                autoRunManager.resetManager();
            }
        }else{
            //在还未到一下天前检测
            if(realtradeManage.showNextOpen()){
                StockDetail current = realtradeManage.getCurrentK();
                chart.setData(realtradeManage.getGroup());
                chart.invalidateView();
                updateTopBar();
                if(realtradeManage.openWithUp()){
                    lb_open_price.setTextColor(getResources().getColor(R.color.kline_up));

                }else if(realtradeManage.openWithDown()){
                    lb_open_price.setTextColor(getResources().getColor(R.color.kline_down));

                }
                stockView.getStockHolder().nextPrice(current.getStart(), true);
                //更新持仓天数
                stockView.getStockHolder().whenNextDay();
                if(autoRunManager != null){
                    autoRunManager.resetManager();
                }
            }else{
                settleThisTrading();
                bt_buy.setEnabled(false);
                bt_sell.setEnabled(false);
                bt_change.setEnabled(true);
                String tips = String.format("该段行情取自: %s(%s)\n日期：%s 至 %s",
                        realtradeManage.getKlineset().getStockName(),
                        realtradeManage.getCurrentK().getStackCode(),
                        realtradeManage.getKlineset().getStartDate(),
                        realtradeManage.getKlineset().getEndDate()
                );
                new ShowDialog().showTips(mContxt, tips);
            }

        }
        stockView.invalidateView();
    }

    private void settleThisTrading(){
        if(realtradeManage.getkStatus() == null){
            return;
        }
        Float price = CommonUtil.castFloatFromString(realtradeManage.getCurenPrice());
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
        int flag = realtradeManage.canTradingStatus();
        if(flag == 10 && action.equals("BUY")){
            CustmerToast.makeText(mContxt, getString(R.string.up_stop_reject)).show();
            return;
        }
        if(-10 == flag && action.equals("SELL")){
            CustmerToast.makeText(mContxt, getString(R.string.down_stop_reject)).show();
            return;
        }
        if(autoRunManager != null){
            autoRunManager.pause();
        }
        TradingPop popWin = new TradingPop(this,
                stockView.getStockHolder(), realtradeManage, action);
//        设置Popupwindow显示位置（从底部弹出）
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
                if(autoRunManager != null){
                    autoRunManager.resumeManager();
                }
            }
        });
    }

    private void onChageStock(){
        settleThisTrading();
        loadKLineSet();
        bt_buy.setEnabled(true);
        bt_sell.setEnabled(true);
        bt_next.setEnabled(true);
        //初始资金问题
    }

    private void onExit(){
        finish();
    }

    @Override
    public void onClick(View v) {
        v.setEnabled(false);
        if(v.getId() == R.id.bt_next){
            if(autoRunManager != null){
                autoRunManager.pause();
            }
            onNextClick();
            PlayVoiceUtils.getInstance().PlayMusic(mContxt);
        }else if(v.getId() == R.id.bt_buy){
            if(stockView.getStockHolder().getAvaiAmt() < 100){
                new ShowDialog().showTips(mContxt, "当前账户资金不足！");
            }
            if(realtradeManage.getCurrentK() == null){
                return;
            }
            showPopFormBottom(v, "BUY");
        }else if(v.getId() == R.id.bt_sell){
            if(realtradeManage.getCurrentK() == null){
                return;
            }
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


    public void showReloadView(final LoadingLayout loadingLayout){
        loadingLayout.post(new Runnable() {
            @Override
            public void run() {
                loadingLayout.showState("加载失败，点击重试！");
            }
        });
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();//注释掉这行,back键不退出activity
        Log.i(TAG, "onBackPressed");
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


    private void setKlineBaseDatas(float height){
        KlineStyle.initSize();
        float baseHeight = 480f;
        KlineStyle.pxScaleRate = height/baseHeight;
        KlineStyle.resetSize();

    }

    @Override
    public void finish() {
        settleThisTrading();
        marqueeManager.tiemrCancel();
        if(autoRunManager != null){
            autoRunManager.tiemrCancel();
        }
        super.finish();
    }
}
