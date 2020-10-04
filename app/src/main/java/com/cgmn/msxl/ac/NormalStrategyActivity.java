package com.cgmn.msxl.ac;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.k.KLine;
import com.cgmn.msxl.comp.k.KlineChart;
import com.cgmn.msxl.comp.k.KlineGroup;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.KlineSet;
import com.cgmn.msxl.server_interface.StockDetail;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.FixStringBuffer;
import com.cgmn.msxl.utils.MessageUtil;
import com.google.gson.Gson;

public class NormalStrategyActivity extends AppCompatActivity {
    private static final String TAG = NormalStrategyActivity.class.getSimpleName();
    private Context mContxt;
    //消息处理
    private Handler mHandler;

    private LinearLayout chartParent;

    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_strategy_layout);
        mContxt = this;
        gson = new Gson();
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
                    paintKLineGroups((KlineSet) msg.obj);
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    AppSqlHelper sqlHeper = new AppSqlHelper(mContxt);
                    String json = sqlHeper.getKlinJsonStr();
                    if(!CommonUtil.isEmpty(json)){
                        KlineSet set = gson.fromJson(json, KlineSet.class);
                        paintKLineGroups(set);
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

    private void paintKLineGroups(KlineSet set){
        Log.d("SET",set.toString());
        Log.d("SET.init.size", set.getInitList().size() +"");
        KlineGroup group = new KlineGroup();
        for (int i = 0; i < set.getInitList().size(); i++) {
            StockDetail detail = set.getInitList().get(i);
            group.addKline(new KLine(
                    detail.getHigh(),
                    detail.getLow(),
                    detail.getStart(),
                    detail.getEnd(),
                    detail.getVol(),
                    ""));
        }

        KlineChart chart = new KlineChart(this);
        chart.setData(group);
        chart.notifyDataSetChanged(true);
        chartParent.addView(chart);
    }

    private void bindView(){
        chartParent = findViewById(R.id.chart_parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) chartParent.getLayoutParams();
        linearParams.height = ((Double)(screenHeight * 0.5)).intValue();// 控件的高强制设成20
        chartParent.setLayoutParams(linearParams);
    }
}
