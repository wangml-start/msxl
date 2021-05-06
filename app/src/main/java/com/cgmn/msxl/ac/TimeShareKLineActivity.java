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
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.comp.k.single.SKLineContent;
import com.cgmn.msxl.comp.k.time.TimeShareChart;
import com.cgmn.msxl.comp.k.time.TimeShareGroup;
import com.cgmn.msxl.comp.pop.TradingPop;
import com.cgmn.msxl.comp.swb.State;
import com.cgmn.msxl.comp.view.MyMarqueeView;
import com.cgmn.msxl.comp.view.StockHolderView;
import com.cgmn.msxl.data.SettingItem;
import com.cgmn.msxl.data.SettledAccount;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.data.Trade;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.server_interface.TimeShare;
import com.cgmn.msxl.service.*;
import com.cgmn.msxl.utils.*;
import com.helin.loadinglayout.LoadingLayout;
import com.squareup.okhttp.Request;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class TimeShareKLineActivity extends AppCompatActivity
        implements View.OnClickListener{
    private static final String TAG = TimeShareKLineActivity.class.getSimpleName();
    private Context mContxt;
    private SKLineContent chart;
    private TimeShareChart timeChart;
    private StockHolderView stockView;
    //设置的模式
    List<SettingItem> modeList;

    private int trainType=StockHolder.LEADING_STRATEGY;
    //消息处理
    private Handler mHandler;

    private LoadingLayout chartParent, timeParent, holderParent;
    private LinearLayout bottomBar;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcastManager;

    private RealTradeManage realtradeManage;
    TextView lb_open_price;
    TextView lb_close_price;
    TextView lb_open_rate;
    TextView lb_close_rate;
    TextView lb_left_day;
    TextView lb_last_rate;
    Button bt_next, bt_buy,bt_sell, bt_change, bt_exit;
    Button bt_1, bt_3, bt_10, bt_30, bt_60, bt_120;
    TextView lb_current_price, lb_current_rate,lb_current_speed,lb_current_time;


    Timer mTimer;
    TimerTask mTimerTask;
    Integer currentSpeed=0;
    private TimeShareGroup timeShareGroup;

    MyMarqueeView marqueeview;
    MarqueeManager marqueeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tineshare_kline_layout);
        bindView();
        initMessageHandle();
        loadAccCash();
        loadKLineSet();
        loadUserMode();
        registerTokenListener();
    }

    private String getUrl(){
        String action = "/stock/get_time_share_klines";
        Map<String, String> params = new HashMap<>();
        params.put("token", GlobalDataHelper.getToken(mContxt));
        return CommonUtil.buildGetUrl(
                ConstantHelper.serverUrl,
                action, params);
    }

    /**
     * 加载用户模式
     */
    private void loadUserMode(){
        //获取用户模式
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                List<SettingItem> modelList = ModeManager.getInstance().getList();
                List<SettingItem> selects = new ArrayList<>();
                AppSqlHelper sqlHeper = new AppSqlHelper(mContxt);
                Map<String, Object> map = GlobalDataHelper.getUser(mContxt);
                Map<String, String> hash = sqlHeper.getUserModelSettings((String) map.get("id"));
                if(!CommonUtil.isEmpty(hash)){
                    for(SettingItem item : modelList){
                        String type = item.getModedType()+"";
                        if(hash.containsKey(type)){
                            item.setState(Integer.valueOf(hash.get(type)));
                            if(item.getState() == State.OPEN){
                                selects.add(item);
                            }
                        }
                    }
                }
                modeList = selects;
                stockView.getStockHolder().setModeList(selects);
                Message message = Message.obtain();
                message.what = MessageUtil.LPAD_USER_MODES_SUCCESS;
                mHandler.sendMessage(message);
            }
        });
    }


    /**
     * 加载K线集合
     */
    private void loadKLineSet(){
        chartParent.showLoading();
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                String url = getUrl();
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

    /**
     * 加载分时数据
     */
    private void loadTimeShareData() {
        if(realtradeManage.getCurrentK() == null){
            return;
        }
        timeParent.showLoading();
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("stock_code", realtradeManage.getCurrentK().getStackCode());
                params.put("trade_date", CommonUtil.formartTimeString(realtradeManage.getCurrentK().getQuoteDate(), null));
                params.put("token", GlobalDataHelper.getToken(mContxt));
                Log.i("A", String.format("%s-%s",params.get("stock_code"),params.get("trade_date")));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/stock/normal_time_share_datas", params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                showReloadView(timeParent);
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.TIME_SHARE_DATA_SUCCESS;
                                try {
                                    message.obj = data.getTimeShareList();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        showReloadView(timeParent);
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

    /**
     * 监听消息队列
     */
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
                    stockView.initAccount(acc.getCashAmt());
                    stockView.invalidateView();
                    if(acc.getVipPermission() == 0){
                        jumpToChargetPage("vip");
                    }else if(acc.getCashAmt().intValue() < 100){
                        jumpToChargetPage("charge");
                    }
                }else if(msg.what == MessageUtil.LPAD_USER_MODES_SUCCESS){
                    //show current mode
                    showSelectModes();
                }else if(msg.what == MessageUtil.TIME_SHARE_DATA_SUCCESS){
                    timeParent.showContent();
                    startTimeChart((List<TimeShare>) msg.obj);
                }
                else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContxt).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    /**
     * 初始化k线图表
     */
    private void startChartInit(){
        if(realtradeManage.getKlineset() == null) {
            return;
        }
        realtradeManage.showNextOpen();
        loadTimeShareData();
        chart.setData(realtradeManage.getGroup());
        chart.invalidateView();
        updateTopBar();
        initStockHolder();
    }

    /**
     * 加载账户资金
     */
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
                            public void onError(Request request, Exception e) {
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

    /**
     * 初始化持仓view
     */
    private void initStockHolder(){
        Float totalAmount = stockView.getStockHolder().getTotAmt();
        stockView.setStockHolder(new StockHolder());
        stockView.getStockHolder().setTrainType(trainType);
        stockView.initAccount(totalAmount);
        stockView.invalidateView();
        stockView.getStockHolder().setModeList(modeList);
    }

    /**
     * 弹出模式设置提示框
     */
    private void showSelectModes(){
        if(modeList.size() > 0){
            List<String> texts = new ArrayList<>();
            for(SettingItem m : modeList){
                texts.add(m.getModeText());
            }
            new ShowDialog().showTips(mContxt, StringUtils.join(texts, "\n") ,"设置的模式");
        }
    }

    /**
     * 更新顶部文本数据
     */
    private void updateTopBar(){
        StockDetail current = realtradeManage.getCurrentK();
        StockDetail last = realtradeManage.getLastK();
        lb_open_price.setText("开盘价： " + CommonUtil.formatNumer(current.getStart()));
        lb_open_rate.setText("涨跌:  " + current.getOpenrate());
        lb_close_price.setText("收盘价： 00.00");
        lb_close_rate.setText("涨跌:  00.00%");
        lb_left_day.setText("剩余: " + realtradeManage.getLeftDay() + " 天");
        lb_last_rate.setText("昨换手: "+last.getExchageRate());

        lb_close_price.setTextColor(getResources().getColor(R.color.text_topbar));
        lb_close_rate.setTextColor(getResources().getColor(R.color.text_topbar));
        lb_open_price.setTextColor(getResources().getColor(R.color.text_topbar));
        lb_open_rate.setTextColor(getResources().getColor(R.color.text_topbar));
    }

    /**
     * 关联界面控件
     */
    private void bindView(){
        mContxt = this;
        realtradeManage = new RealTradeManage();

        lb_open_price = findViewById(R.id.lb_open_price);
        lb_close_price = findViewById(R.id.lb_close_price);
        lb_open_rate = findViewById(R.id.lb_open_rate);
        lb_close_rate = findViewById(R.id.lb_close_rate);
        lb_left_day = findViewById(R.id.lb_left_day);
        lb_last_rate = findViewById(R.id.lb_last_rate);
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
        lb_current_time = findViewById(R.id.lb_current_time);

        chartParent = findViewById(R.id.chart_parent);
        holderParent = findViewById(R.id.holder_parent);
        timeParent = findViewById(R.id.time_parent);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        chartParent.getLayoutParams().height=((Double)(screenHeight * 0.2)).intValue();
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

        chart = new SKLineContent(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        stockView = new StockHolderView(this);

        timeChart = new TimeShareChart(this);
        timeChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        timeChart.setShowDetails(true);

        timeParent.getLayoutParams().height = ((Double) (screenHeight * 0.25)).intValue();
        timeParent.setStateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTimeShareData();
            }
        });

        timeParent.addView(timeChart);

        chartParent.addView(chart);
        holderParent.addView(stockView);


        marqueeview = findViewById(R.id.marqueeview);
        marqueeManager = new MarqueeManager(mContxt, marqueeview);
        marqueeManager.startMarquee();
    }

    /**
     * 点下一步按钮
     */
    private void onNextClick(){
        if(RealTradeManage.OPEN.equals(realtradeManage.getkStatus())){
            realtradeManage.showNextClose();
            chart.setData(realtradeManage.getGroup());
            chart.invalidateView();
            StockDetail current = realtradeManage.getCurrentK();
            StockDetail last = realtradeManage.getLastK();
            lb_close_price.setText("收盘价：" + CommonUtil.formatNumer(current.getEnd()));
            lb_close_rate.setText("涨跌: " + current.getUpRate());
            if(current.getEnd() > last.getEnd()){
                lb_close_price.setTextColor(getResources().getColor(R.color.kline_up));
                lb_close_rate.setTextColor(getResources().getColor(R.color.kline_up));
            }else{
                lb_close_price.setTextColor(getResources().getColor(R.color.kline_down));
                lb_close_rate.setTextColor(getResources().getColor(R.color.kline_down));
            }
            stockView.getStockHolder().nextPrice(current.getEnd(), false);
        }else{
            //在还未到一下天前检测
            //交易模式检测
            List<Integer> holdChecks = ModeManager.getInstance().getHoldCheck();
            Map<String, Object> values = new HashMap<>();
            values.put("nodes", realtradeManage.getGroup().getNodes());
            values.put("kStatus", realtradeManage.getkStatus());
            values.put("holdDay", stockView.getStockHolder().getHoldDays());
            values.put("holdStock", stockView.getStockHolder().getAvaiLabelShare() > 0);
            values.put("lossRate", stockView.getStockHolder().getLossRate());
            List<String> messges = new ArrayList<>();
            for(SettingItem sItem : stockView.getStockHolder().getModeList()){
                if(holdChecks.contains(sItem.getModedType())){
                    boolean flag = ModeManager.getInstance().assertionOverMode(sItem.getModedType(), values);
                    if(flag && !stockView.getStockHolder().exists(sItem.getModedType())){
                        stockView.getStockHolder().addOverType(sItem.getModedType());
                        messges.add(sItem.getModeText());
                    }
                }
            }
            if(messges.size() > 0){
                CustmerToast.makeText(mContxt,
                        "违背模式\n " + StringUtils.join(messges, "\n"), Toast.LENGTH_LONG).show();
            }
            if(realtradeManage.showNextOpen()){
                StockDetail current = realtradeManage.getCurrentK();
                chart.setData(realtradeManage.getGroup());
                chart.invalidateView();
                updateTopBar();
                if(realtradeManage.openWithUp()){
                    lb_open_price.setTextColor(getResources().getColor(R.color.kline_up));
                    lb_open_rate.setTextColor(getResources().getColor(R.color.kline_up));
                }else if(realtradeManage.openWithDown()){
                    lb_open_price.setTextColor(getResources().getColor(R.color.kline_down));
                    lb_open_rate.setTextColor(getResources().getColor(R.color.kline_down));
                }
                stockView.getStockHolder().nextPrice(current.getStart(), true);
                //更新持仓天数
                stockView.getStockHolder().whenNextDay();
                loadTimeShareData();
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

    /**
     * 交易结算
     */
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

    /**
     * 弹出买卖窗体
     * @param view
     * @param action
     */
    public void showPopFormBottom(View view, String action) {
        int flag;
        if(timeShareGroup != null && timeShareGroup.current != null){
            flag = realtradeManage.canTradingStatus(timeShareGroup.currentRate());
        }else{
            flag = realtradeManage.canTradingStatus();
        }
        if(flag == 10 && action.equals("BUY")){
            CustmerToast.makeText(mContxt, getString(R.string.up_stop_reject)).show();
            return;
        }
        if(-10 == flag && action.equals("SELL")){
            CustmerToast.makeText(mContxt, getString(R.string.down_stop_reject)).show();
            return;
        }
        TradingPop popWin = new TradingPop(this,
                stockView.getStockHolder(), realtradeManage, action);
        popWin.setTimeShare(timeShareGroup);
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
            }
        });
    }

    /**
     * 换股
     */
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
            onNextClick();
        }else if(v.getId() == R.id.bt_buy){
            if(stockView.getStockHolder().getTotAmt() < 100){
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
        }
        v.setEnabled(true);
    }

    /**
     * 跳转到充值界面
     * @param type
     */
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

    /**
     * 显示重新加载
     * @param loadingLayout
     */
    public void showReloadView(final LoadingLayout loadingLayout){
        loadingLayout.post(new Runnable() {
            @Override
            public void run() {
                loadingLayout.showState("加载失败，点击重试！");
            }
        });
    }

    /**
     * 系统退回建
     */
    @Override
    public void onBackPressed() {
        // super.onBackPressed();//注释掉这行,back键不退出activity
        Log.i(TAG, "onBackPressed");
    }

    /**
     * 监听退出
     */
    private void registerTokenListener() {
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

    /**
     * 控制不同分辨率的显示问题
     * @param height
     */
    private void setKlineBaseDatas(float height){
        KlineStyle.initSize();
        float baseHeight = 480f;
        KlineStyle.pxScaleRate = height/baseHeight;
        KlineStyle.resetSize();

    }

    /**
     * 分时开始
     * @param list
     */
    private void startTimeChart(List<TimeShare> list) {
        timeShareGroup = new TimeShareGroup();
        if(timeShareGroup.init(list)){
            timeChart.setData(timeShareGroup);
            timeChart.invalidateView();
            currentSpeed = 0;
            bt_next.setEnabled(false);
            playSpeed(1);
        }else{
            timeParent.showEmpty();
            bt_next.setEnabled(true);
        }
    }

    private void stopTimeChart() {
        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 播放分时
     * @param speed
     */
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

    /**
     * 分时播放执行
     */
    public void onNextMunite(){
        timeChart.invalidateView();
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

            stockView.getStockHolder().nextPrice(timeShareGroup.current.getPrice(), false);
            stockView.invalidateView();
        }
        if(timeShareGroup.timer.getOver()){
            if(RealTradeManage.OPEN.equals(realtradeManage.getkStatus())){
                stopTimeChart();
                onNextClick();
                bt_next.setEnabled(true);
            }
        }
    }

    @Override
    public void finish() {
        stopTimeChart();
        settleThisTrading();
        marqueeManager.tiemrCancel();
        super.finish();
    }
}
