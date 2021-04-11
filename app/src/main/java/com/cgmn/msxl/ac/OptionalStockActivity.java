package com.cgmn.msxl.ac;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.MarketAdapter;
import com.cgmn.msxl.comp.k.KLineContent;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.MarketData;
import com.cgmn.msxl.server_interface.TrendStock;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.StockDisplayManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;
import com.helin.loadinglayout.LoadingLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionalStockActivity extends AppCompatActivity {
    private static final String TAG = OptionalStockActivity.class.getSimpleName();

    private ListView list_content;
    private Handler mHandler;
    public Context mContext;

    private RelativeLayout img_back,img_add,img_delete;
    private LoadingLayout chartParent;
    private KLineContent chart;
    private TextView txt_title;

    private MarketAdapter adapter;
    List<TrendStock> adpterDatas = new ArrayList<>();

    private Integer selectedListIndex;

    private StockDisplayManager stockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.optinal_st_layout);
        mContext = this;

        initMessageHandler();
        bindView();
        loadOptionalList();
    }


    private void initMessageHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_BREAK_UP_SUCCESS) {
                    adapter = new MarketAdapter(mContext, adpterDatas);
                    list_content.setAdapter(adapter);
                    adpterDatas.clear();
                    MarketData mkData = (MarketData) msg.obj;
                    if(!CommonUtil.isEmpty(mkData.getTrendList())){
                        adpterDatas.addAll(mkData.getTrendList());
                        txt_title.setText(mkData.getTrendList().get(0).getStackName());
                        selectedListIndex=0;
                        loadStockDetails(mkData.getTrendList().get(0).getStackCode());
                    }
                    adapter.notifyDataSetChanged();
                } else if (msg.what == MessageUtil.REQUEST_STOCK_DETAIL_SUCCESS) {
                    chartParent.showContent();
                    MarketData mkData = (MarketData) msg.obj;
                    if(!CommonUtil.isEmpty(mkData.getStocks())){
                        stockManager.resetManager();
                        stockManager.setStocks(mkData.getStocks());
                        startChartInit();
                    }
                }  else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                } else if(msg.what == MessageUtil.OPTINAL_STOCK_ADD){
                    loadOptionalList();
                }
                return false;
            }
        });
    }

    private void startChartInit(){
        stockManager.repacklineNode("");
        if(stockManager.getGroup() == null) {
            return;
        }
        chart.setData(stockManager.getGroup());
        chart.invalidateView();
    }

    private void bindView(){
        stockManager = new StockDisplayManager();
        list_content = findViewById(R.id.list_content);
        chartParent = findViewById(R.id.chart_parent);
        img_back = findViewById(R.id.img_back);
        img_add = findViewById(R.id.img_add);
        img_delete = findViewById(R.id.img_delete);
        txt_title =  findViewById(R.id.txt_title);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        setKlineBaseDatas(screenHeight);
        chartParent.getLayoutParams().height=((Double) (screenHeight * 0.5)).intValue();
        chartParent.setStateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStockDetails(adpterDatas.get(selectedListIndex).getStackCode());
            }
        });

        View.OnClickListener lis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.img_back){
                    finish();
                }else if(v.getId() == R.id.img_delete){
                    if(selectedListIndex != null){
                        optionalStockOperation(adpterDatas.get(selectedListIndex).getStackCode(), "del");
                    }
                }else if(v.getId() == R.id.img_add){
                    showAddPage();
                }
            }
        };
        img_back.setOnClickListener(lis);
        img_add.setOnClickListener(lis);
        img_delete.setOnClickListener(lis);

        chart = new KLineContent(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        chartParent.addView(chart);

        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrendStock object = adpterDatas.get(position);
                if(selectedListIndex != position){
                    loadStockDetails(object.getStackCode());
                    txt_title.setText(object.getStackName());
                    selectedListIndex=position;
                }
            }
        });

    }

    private void loadOptionalList() {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/stock/optional_list";
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        action, params);
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
                                message.what = MessageUtil.REQUEST_BREAK_UP_SUCCESS;
                                try {
                                    message.obj = data.getMarketData();
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
            }
        });
    }

    public void showReloadView(final LoadingLayout loadingLayout){
        loadingLayout.post(new Runnable() {
            @Override
            public void run() {
                loadingLayout.showState("加载失败，点击重试！");
            }
        });
    }

    private void loadStockDetails(final String code) {
        chartParent.showLoading();
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/stock/stock_details";
                Map<String, String> params = new HashMap<>();
                params.put("code", code);
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        action, params);
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
                                message.what = MessageUtil.REQUEST_STOCK_DETAIL_SUCCESS;
                                try {
                                    message.obj = data.getMarketData();
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
            }
        });
    }

    private void optionalStockOperation(final String code, final String operation){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/stock/stock_optional";
                Map<String, String> params = new HashMap<>();
                params.put("stock_code", code);
                params.put("operation", operation);
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        action, params);
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
                                message.what = MessageUtil.OPTINAL_STOCK_ADD;
                                try {
                                    message.obj = data.getMarketData();
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
            }
        });
    }

    private void showAddPage(){
        final EditText inputServer = new EditText(this);
        inputServer.setBackgroundResource(R.drawable.edittext_border);
        inputServer.setHint("请输入代码");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inputServer)
                .setNegativeButton("取消", null);
        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                optionalStockOperation(inputServer.getText().toString(), "add");
            }
        });
        builder.show();
    }

    private void setKlineBaseDatas(float height){
        KlineStyle.initSize();
        float baseHeight = 480f;
        KlineStyle.pxScaleRate = height/baseHeight;
        KlineStyle.resetSize();
    }
}