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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NormalTimeShareActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = NormalTimeShareActivity.class.getSimpleName();
    private Handler mHandler;
    public Context mContxt;

    private LoadingLayout chartParent;
    private TimeShareChart chart;
    Button bt_buy, bt_sell, bt_change, bt_exit;
    TextView lb_open_price, lb_open_rate;
    TextView lb_buy_cost, lb_rate;

    private LinearLayout bottomBar, pop_div_text;

    SimpleStockHolder holder;
    private TimeShareGroup timeShareGroup;

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

        lb_open_price = findViewById(R.id.lb_open_price);
        lb_open_rate = findViewById(R.id.lb_open_rate);
        lb_buy_cost = findViewById(R.id.lb_buy_cost);
        lb_rate = findViewById(R.id.lb_rate);

        bt_buy.setOnClickListener(this);
        bt_sell.setOnClickListener(this);
        bt_change.setOnClickListener(this);
        bt_exit.setOnClickListener(this);


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
        bottomParams.height = ((Double) (screenHeight * 0.04)).intValue();
        bottomBar.setLayoutParams(bottomParams);
        pop_div_text.setLayoutParams(bottomParams);

        chart = new TimeShareChart(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
            chart.invalidateView();
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


    public void buySellAction(String action) {
//        if(realtradeManage.getCurrentK() == null){
//            return;
//        }
//        int flag = realtradeManage.canTradingStatus();
//        if (flag == 10 && action.equals("BUY")) {
//            CustmerToast.makeText(mContxt, getString(R.string.up_stop_reject)).show();
//            return;
//        }
//        if (-10 == flag && action.equals("SELL")) {
//            CustmerToast.makeText(mContxt, getString(R.string.down_stop_reject)).show();
//            return;
//        }
//        Float price = CommonUtil.castFloatFromString(realtradeManage.getCurenPrice());
//        if (action.equals("BUY")) {
//            holder.onBuy(price, realtradeManage.getCurrentK().getStackCode());
//            updateReceiveRate();
//            realtradeManage.getCurrentK().setOpChar("B");
//        } else {
//            if (holder.canSell()) {
//                settleThisTrading();
//                updateReceiveRate();
//                realtradeManage.getCurrentK().setOpChar("S");
//            }
//        }
    }

    private void onChageStock() {
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
        }else if(v.getId() == R.id.bt_buy){
            timeShareGroup.onNextStep();
            chart.invalidateView();
        }
    }



    @Override
    public void finish() {
        super.finish();
    }
}