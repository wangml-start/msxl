package com.cgmn.msxl.ac;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.k.KLineContent;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.data.*;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.AutoNextListener;
import com.cgmn.msxl.server_interface.*;
import com.cgmn.msxl.service.*;
import com.cgmn.msxl.utils.*;
import com.helin.loadinglayout.LoadingLayout;
import com.squareup.okhttp.Request;

import java.util.HashMap;
import java.util.Map;


public class KLineSimulateActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = KLineSimulateActivity.class.getSimpleName();
    private Handler mHandler;
    public Context mContxt;

    private LoadingLayout chartParent;
    private KLineContent chart;
    Button bt_next, bt_buy, bt_sell, bt_change, bt_exit;
    TextView lb_open_price;
    TextView lb_close_price;
    TextView lb_open_rate;
    TextView lb_close_rate;
    TextView lb_left_day, lb_buy_cost, lb_rate;
    TextView lb_left_s, lb_last_rate;
    MediaPlayer music;
    private LinearLayout bottomBar, pop_div_text;
    private RealTradeManage realtradeManage;
    TradeAutoRunManager autoRunManager;

    SimpleStockHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kline_simulate_layout);
        bindView();
        initMessageHandle();
        loadKLineSet();
    }

    private void bindView() {
        mContxt = this;
        realtradeManage = new RealTradeManage();
        holder = new SimpleStockHolder();
        bt_next = findViewById(R.id.bt_next);
        bt_buy = findViewById(R.id.bt_buy);
        bt_sell = findViewById(R.id.bt_sell);
        bt_change = findViewById(R.id.bt_change);
        bt_exit = findViewById(R.id.bt_exit);
        bottomBar = findViewById(R.id.pop_div);
        pop_div_text = findViewById(R.id.pop_div_text);

        lb_open_price = findViewById(R.id.lb_open_price);
        lb_close_price = findViewById(R.id.lb_close_price);
        lb_open_rate = findViewById(R.id.lb_open_rate);
        lb_close_rate = findViewById(R.id.lb_close_rate);
        lb_left_day = findViewById(R.id.lb_left_day);
        lb_last_rate = findViewById(R.id.lb_last_rate);
        lb_buy_cost = findViewById(R.id.lb_buy_cost);
        lb_rate = findViewById(R.id.lb_rate);

        bt_next.setOnClickListener(this);
        bt_buy.setOnClickListener(this);
        bt_sell.setOnClickListener(this);
        bt_change.setOnClickListener(this);
        bt_exit.setOnClickListener(this);


        chartParent = findViewById(R.id.chart_parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        chartParent.getLayoutParams().height = ((Double) (screenHeight * 0.45)).intValue();
        chartParent.setStateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadKLineSet();
            }
        });
        setKlineBaseDatas(screenHeight);

        LinearLayout.LayoutParams bottomParams = (LinearLayout.LayoutParams) bottomBar.getLayoutParams();
        bottomParams.height = ((Double) (screenHeight * 0.04)).intValue();
        bottomBar.setLayoutParams(bottomParams);
        pop_div_text.setLayoutParams(bottomParams);

        chart = new KLineContent(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        chartParent.addView(chart);

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
                PlayMusic();
            }
        });
        autoRunManager.startManager();
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    chartParent.showContent();
                    realtradeManage.resetManager();
                    realtradeManage.setKlineset((KlineSet) msg.obj);
                    startChartInit();
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContxt).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    private void startChartInit() {
        if (realtradeManage.getKlineset() == null) {
            return;
        }
        holder.resetHolder();
        realtradeManage.showNextOpen();
        chart.setData(realtradeManage.getGroup());
        chart.invalidateView();
        autoRunManager.resetManager();
        updateTopBar();
    }

    public void showReloadView(final LoadingLayout loadingLayout) {
        loadingLayout.post(new Runnable() {
            @Override
            public void run() {
                loadingLayout.showState("加载失败，点击重试！");
            }
        });
    }

    private void loadKLineSet() {
        chartParent.showLoading();
        autoRunManager.pause();
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContxt));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/stock/getKlineSet", params);
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

    private void onNextClick() {
        if (RealTradeManage.OPEN.equals(realtradeManage.getkStatus())) {
            realtradeManage.showNextClose();
            chart.setData(realtradeManage.getGroup());
            chart.invalidateView();
            StockDetail current = realtradeManage.getCurrentK();
            StockDetail last = realtradeManage.getLastK();
            lb_close_price.setText("收盘价：" + CommonUtil.formatNumer(current.getEnd()));
            lb_close_rate.setText("涨跌: " + current.getUpRate());
            if (current.getEnd() > last.getEnd()) {
                lb_close_price.setTextColor(getResources().getColor(R.color.kline_up));
                lb_close_rate.setTextColor(getResources().getColor(R.color.kline_up));
            } else {
                lb_close_price.setTextColor(getResources().getColor(R.color.kline_down));
                lb_close_rate.setTextColor(getResources().getColor(R.color.kline_down));
            }
            holder.nextPrice(current.getEnd(), false);
            updateReceiveRate();
            autoRunManager.resetManager();
        } else {
            //在还未到一下天前检测
            if (realtradeManage.showNextOpen()) {
                chart.setData(realtradeManage.getGroup());
                chart.invalidateView();
                updateTopBar();
                StockDetail current = realtradeManage.getCurrentK();
                if (realtradeManage.openWithUp()) {
                    lb_open_price.setTextColor(getResources().getColor(R.color.kline_up));
                    lb_open_rate.setTextColor(getResources().getColor(R.color.kline_up));
                } else if (realtradeManage.openWithDown()) {
                    lb_open_price.setTextColor(getResources().getColor(R.color.kline_down));
                    lb_open_rate.setTextColor(getResources().getColor(R.color.kline_down));
                }
                holder.nextPrice(current.getStart(), true);
                updateReceiveRate();
                autoRunManager.resetManager();
            } else {
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
    }

    private void settleThisTrading() {
        if (realtradeManage.getkStatus() == null) {
            return;
        }

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
                        new OkHttpClientManager.Param("train_type", "1"),
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

    private void updateTopBar() {
        StockDetail current = realtradeManage.getCurrentK();
        StockDetail last = realtradeManage.getLastK();
        lb_open_price.setText("开盘价： " + CommonUtil.formatNumer(current.getStart()));
        lb_open_rate.setText("涨跌:  " + current.getOpenrate());
        lb_close_price.setText("收盘价： 00.00");
        lb_close_rate.setText("涨跌:  00.00%");
        lb_left_day.setText("剩余: " + realtradeManage.getLeftDay() + " 天");
        lb_last_rate.setText("昨换手: " + last.getExchageRate());

        lb_close_price.setTextColor(getResources().getColor(R.color.text_topbar));
        lb_close_rate.setTextColor(getResources().getColor(R.color.text_topbar));
        lb_open_price.setTextColor(getResources().getColor(R.color.text_topbar));
        lb_open_rate.setTextColor(getResources().getColor(R.color.text_topbar));

        lb_buy_cost.setText("成本： " + CommonUtil.formatNumer(holder.getCostPrice()));
        lb_rate.setText("收益率: " + holder.getRateStr());
        if (holder.getRate() > 0) {
            lb_rate.setTextColor(getResources().getColor(R.color.kline_up));
        } else if (holder.getRate() < 0) {
            lb_rate.setTextColor(getResources().getColor(R.color.kline_down));
        } else {
            lb_rate.setTextColor(getResources().getColor(R.color.text_topbar));
        }
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


    public void buySellAction(String action) {
        if(realtradeManage.getCurrentK() == null){
            return;
        }
        int flag = realtradeManage.canTradingStatus();
        if (flag == 10 && action.equals("BUY")) {
            CustmerToast.makeText(mContxt, getString(R.string.up_stop_reject)).show();
            return;
        }
        if (-10 == flag && action.equals("SELL")) {
            CustmerToast.makeText(mContxt, getString(R.string.down_stop_reject)).show();
            return;
        }
        Float price = CommonUtil.castFloatFromString(realtradeManage.getCurenPrice());
        if (action.equals("BUY")) {
            holder.onBuy(price, realtradeManage.getCurrentK().getStackCode());
            updateReceiveRate();
            realtradeManage.getCurrentK().setOpChar("B");
        } else {
            if (holder.canSell()) {
                settleThisTrading();
                updateReceiveRate();
                realtradeManage.getCurrentK().setOpChar("S");
            }
        }
    }

    private void onChageStock() {
        loadKLineSet();
        bt_buy.setEnabled(true);
        bt_sell.setEnabled(false);
        bt_next.setEnabled(true);
        //初始资金问题
    }

    private void onExit() {
        finish();
    }

    @Override
    public void onClick(View v) {
        v.setEnabled(false);
        if (v.getId() == R.id.bt_next) {
            autoRunManager.pause();
            onNextClick();
            PlayMusic();
        } else if (v.getId() == R.id.bt_buy) {
            if (realtradeManage.getCurrentK() == null) {
                return;
            }
            buySellAction("BUY");
        } else if (v.getId() == R.id.bt_sell) {
            if (realtradeManage.getCurrentK() == null) {
                return;
            }
            buySellAction("SELL");
        } else if (v.getId() == R.id.bt_change) {
            onChageStock();
        } else if (v.getId() == R.id.bt_exit) {
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

    private void releaseMediaPlayer() {
        if (music != null) {
            music.release();
            music = null;
        }
    }

    private void PlayMusic() {
//        Log.e("PlayMusic","PlayMusic"+flag);
        // 当用户很快的点击播放不同的音频时，就先释放，否则快速点击音频会有声音重叠
        releaseMediaPlayer();
        music = MediaPlayer.create(this, R.raw.btn_wav);
        music.start();
        // 播放完成可以释放资源
        music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseMediaPlayer();
            }
        });
    }


    @Override
    public void finish() {
        settleThisTrading();
        autoRunManager.tiemrCancel();
        super.finish();
    }
}