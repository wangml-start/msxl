package com.cgmn.msxl.ac;

import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.StockHolderView;
import com.cgmn.msxl.comp.pop.TradingPop;
import com.cgmn.msxl.comp.k.KlineChart;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.service.RealTradeManage;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

public class RealControlActivity extends AppCompatActivity
        implements View.OnClickListener{
    private static final String TAG = RealControlActivity.class.getSimpleName();
    private Context mContxt;
    private KlineChart chart;
    private StockHolderView stockView;
    //消息处理
    private Handler mHandler;

    private LinearLayout chartParent, holderParent;


    private RealTradeManage realtradeManage;
    TextView lb_open_price;
    TextView lb_close_price;
    TextView lb_open_rate;
    TextView lb_close_rate;
    TextView lb_left_day;
    TextView lb_left_s;
    Button bt_next, bt_buy,bt_sell, bt_change, bt_exit;



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
                    Exception exception = (Exception) msg.obj;
                    StringBuffer mes = new StringBuffer();
                    GlobalExceptionHandler.getInstance(mContxt).handlerException(exception);
                    mes.append(getString(R.string.ge_stock_info_failed));
                    //TODO: 异常处理
                    CustmerToast.makeText(mContxt, mes.toString()).show();
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
        stockView.setStockHolder(new StockHolder());
        stockView.initAccount(100000f);
        stockView.invalidateView();
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
        realtradeManage = new RealTradeManage();

        lb_open_price = findViewById(R.id.lb_open_price);
        lb_close_price = findViewById(R.id.lb_close_price);
        lb_open_rate = findViewById(R.id.lb_open_rate);
        lb_close_rate = findViewById(R.id.lb_close_rate);
        lb_left_day = findViewById(R.id.lb_left_day);
        lb_left_s = findViewById(R.id.lb_left_s);
        bt_next = findViewById(R.id.bt_next);
        bt_buy = findViewById(R.id.bt_buy);
        bt_sell = findViewById(R.id.bt_sell);
        bt_change = findViewById(R.id.bt_change);
        bt_exit = findViewById(R.id.bt_exit);

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
        kparams.height = ((Double)(screenHeight * 0.5)).intValue();
        chartParent.setLayoutParams(kparams);

        chart = new KlineChart(this);
        stockView = new StockHolderView(this);
        chartParent.addView(chart);
        holderParent.addView(stockView);
    }

    private void onNextClick(){
        if(RealTradeManage.OPEN.equals(realtradeManage.getkStatus())){
            realtradeManage.showNextClose();
            chart.setData(realtradeManage.getGroup());
            chart.invalidateView();
            StockDetail current = realtradeManage.getCurrentK();
            StockDetail last = realtradeManage.getLastK();
            lb_close_price.setText("收盘价：" + current.getEnd());
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
            }else{
                settleThisTrading();
                bt_buy.setEnabled(false);
                bt_sell.setEnabled(false);
                bt_next.setEnabled(false);
                bt_change.setEnabled(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContxt);
                builder.setTitle(R.string.tips);
                builder.setMessage(String.format("该段行情取自: %s(%s)\n日期：%s 至 %s",
                        realtradeManage.getKlineset().getStockName(),
                        realtradeManage.getCurrentK().getStackCode(),
                        realtradeManage.getKlineset().getStartDate(),
                        realtradeManage.getKlineset().getEndDate()
                ));
                builder.setPositiveButton("Ok", null);
                builder.show();
            }
        }
    }

    private void settleThisTrading(){
        Float price = CommonUtil.castFloatFromString(realtradeManage.getCurenPrice());
        stockView.getStockHolder().settleTrading(price);
        if(stockView.getStockHolder().getSettlementStatus() == 1 ||
                stockView.getStockHolder().getNodes().size() == 0){
            return;
        }
        //TODO send trade to server
    }

    public void showPopFormBottom(View view, String action) {
        int flag = realtradeManage.canTradingStatus();
        if(flag == 10){
            CustmerToast.makeText(mContxt, getString(R.string.up_stop_reject)).show();
            return;
        }
        if(-10 == flag){
            CustmerToast.makeText(mContxt, getString(R.string.down_stop_reject)).show();
            return;
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
            }
        });
    }

    private void onChageStock(){
        settleThisTrading();
        realtradeManage.resetManager();
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
        if(v.getId() == R.id.bt_next){
            onNextClick();
            stockView.invalidateView();
        }else if(v.getId() == R.id.bt_buy){
            showPopFormBottom(v, "BUY");
        }else if(v.getId() == R.id.bt_sell){
            showPopFormBottom(v, "SELL");
        }else if(v.getId() == R.id.bt_change){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContxt);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.sure_to_do_this);
            builder.setPositiveButton(R.string.queding, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onChageStock();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        }else if(v.getId() == R.id.bt_exit){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContxt);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.sure_to_do_this);
            builder.setPositiveButton(R.string.queding, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onExit();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();

        }
    }
}
