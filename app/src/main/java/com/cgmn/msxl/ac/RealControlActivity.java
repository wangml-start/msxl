package com.cgmn.msxl.ac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
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
import com.cgmn.msxl.comp.k.KLineContent;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.comp.view.MyMarqueeView;
import com.cgmn.msxl.comp.view.StockHolderView;
import com.cgmn.msxl.comp.pop.TradingPop;
import com.cgmn.msxl.comp.k.KlineChart;
import com.cgmn.msxl.comp.swb.State;
import com.cgmn.msxl.data.*;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.AutoNextListener;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.service.*;
import com.cgmn.msxl.utils.*;
import com.squareup.okhttp.Request;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class RealControlActivity extends AppCompatActivity
        implements View.OnClickListener{
    private static final String TAG = RealControlActivity.class.getSimpleName();
    private Context mContxt;
    private KLineContent chart;
    private StockHolderView stockView;
    //设置的模式
    List<SettingItem> modeList;

    private int trainType;
    private int userModelId;
    //消息处理
    private Handler mHandler;

    private LinearLayout chartParent, holderParent;
    private LinearLayout bottomBar;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcastManager;

    private RealTradeManage realtradeManage;
    TextView lb_open_price;
    TextView lb_close_price;
    TextView lb_open_rate;
    TextView lb_close_rate;
    TextView lb_left_day;
    TextView lb_left_s,lb_last_rate;
    Button bt_next, bt_buy,bt_sell, bt_change, bt_exit;
    MyMarqueeView marqueeview;
    MarqueeManager marqueeManager;
    TradeAutoRunManager autoRunManager;
    MediaPlayer music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.klinecontrol_layout);
        bindView();
        initMessageHandle();
        loadAccCash();
        loadKLineSet();
        loadUserMode();
        registerTokenListener();
    }

    private String getUrl(){
        String action = "/stock/getKlineSet";
        if(trainType == StockHolder.LEADING_STRATEGY){
            action = "/stock/getHigherKlineSet";
        }
        Map<String, String> params = new HashMap<>();
        params.put("token", GlobalDataHelper.getToken(mContxt));
        return CommonUtil.buildGetUrl(
                ConstantHelper.serverUrl,
                action, params);
    }

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

    private void loadKLineSet(){
//        CustmerToast.makeText(mContxt, R.string.get_stock_datas).show();
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                String url = getUrl();
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
                    realtradeManage.resetManager();
                    realtradeManage.setKlineset((KlineSet) msg.obj) ;
                    startChartInit();
                } else if(msg.what == MessageUtil.GET_CASH_ACC_SUCCESS){
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
                }
                else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
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

        autoRunManager.resetManager();
    }

    private void loadAccCash(){
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
        Float totalAmount = stockView.getStockHolder().getTotAmt();
        stockView.setStockHolder(new StockHolder());
        stockView.getStockHolder().setModelRecordId(userModelId);
        stockView.getStockHolder().setTrainType(trainType);
        stockView.initAccount(totalAmount);
        stockView.invalidateView();
        stockView.getStockHolder().setModeList(modeList);
    }

    private void showSelectModes(){
        if(modeList.size() > 0){
            List<String> texts = new ArrayList<>();
            for(SettingItem m : modeList){
                texts.add(m.getModeText());
            }
            new ShowDialog().showTips(mContxt, StringUtils.join(texts, "\n") ,"设置的模式");
        }
    }

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



        chartParent = findViewById(R.id.chart_parent);
        holderParent = findViewById(R.id.holder_parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        LinearLayout.LayoutParams kparams =(LinearLayout.LayoutParams) chartParent.getLayoutParams();
        kparams.height = ((Double)(screenHeight * 0.45)).intValue();
        chartParent.setLayoutParams(kparams);
        setKlineBaseDatas(screenHeight);

        LinearLayout.LayoutParams bottomParams =(LinearLayout.LayoutParams) bottomBar.getLayoutParams();
        bottomParams.height = ((Double)(screenHeight * 0.05)).intValue();
        bottomBar.setLayoutParams(bottomParams);

        chart = new KLineContent(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        stockView = new StockHolderView(this);
        chartParent.addView(chart);
        holderParent.addView(stockView);

        marqueeview = findViewById(R.id.marqueeview);
        marqueeManager = new MarqueeManager(mContxt, marqueeview);
        marqueeManager.startMarquee();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("datas");
        trainType = bundle.getInt("train_type");
        userModelId =  bundle.getInt("user_model_id");

        //记时3秒自动下一步
        autoRunManager = new TradeAutoRunManager();
        autoRunManager.setListener(new AutoNextListener() {
            @Override
            public void onTicket(Integer sec) {
                bt_next.setText(String.format("%s%ss", getResources().getString(R.string.next), sec));
            }

            @Override
            public void onComplete() {
                bt_next.setText(String.format("%s",  getResources().getString(R.string.next)));
                onNextClick();
            }
        });
        autoRunManager.startManager();
    }

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
            autoRunManager.resetManager();
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
                autoRunManager.resetManager();
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
        autoRunManager.pause();
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
                autoRunManager.resumeManager();
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
        settleThisTrading();
        finish();
    }

    @Override
    public void onClick(View v) {
        PlayMusic();
        if(v.getId() == R.id.bt_next){
            onNextClick();
        }else if(v.getId() == R.id.bt_buy){
            if(stockView.getStockHolder().getTotAmt() < 100){
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


    private void PlayMusic() {
        music = MediaPlayer.create(this, R.raw.btn_wav);
        music.start();
    }

    private void setKlineBaseDatas(float height){
        KlineStyle.initSize();
        float baseHeight = 480f;
        KlineStyle.pxScaleRate = height/baseHeight;
        KlineStyle.resetSize();

    }

    @Override
    public void finish() {
        marqueeManager.tiemrCancel();
        autoRunManager.tiemrCancel();
        super.finish();
    }
}
