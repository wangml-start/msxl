package com.cgmn.msxl.ac;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.adpter.MarketAdapter;
import com.cgmn.msxl.comp.k.KLineContent;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.comp.pop.PayPop;
import com.cgmn.msxl.comp.pop.UnlockPop;
import com.cgmn.msxl.data.SelectionItem;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.PaymentListener;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.MarketData;
import com.cgmn.msxl.server_interface.TrendStock;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.StockDisplayManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;
import com.cgmn.msxl.utils.ShowDialog;
import com.helin.loadinglayout.LoadingLayout;

import java.util.*;

public class MarketTrendActivity extends BaseOtherActivity {
    private static final String TAG = MarketTrendActivity.class.getSimpleName();

    private Handler mHandler;
    private TextView txt_day_list, txt_date, txt_vol,txt_des,txt_unlock_des;
    private LoadingLayout chartParent;
    private RelativeLayout add_optional_st;
    private ListView list_content;
    private MarketAdapter adapter;
    private KLineContent chart;
    List<TrendStock> adpterDatas = new ArrayList<>();
    private OptionsPickerView dayOptions, volOptions, pvTime;
    private ArrayList<SelectionItem> dayList = new ArrayList<>(), volList = new ArrayList<>();
    private ArrayList<SelectionItem> dateList = new ArrayList<>();
//    private TimePickerView pvTime;
    private MarketData marketData;
    private String selectedDay, selectedVol = "0", selectedDate;
    private Integer selectedListIndex;
    private String selectedCode;
    private StockDisplayManager stockManager;

    private UnlockPop mPhotoPopupWindow;
    private PayPop mPayPopWindow;

    @Override
    protected int getContentView() {
        return R.layout.market_trend_layout;
    }

    @Override
    protected String setTitle() {
        return getString(R.string.trend_break_up);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void init() {
        bindView();
        initMessageHandler();
        loadInfo();

    }

    private void initMessageHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean handleMessage(Message msg) {
                if (MessageUtil.REQUEST_SUCCESS == msg.what) {
                    chartParent.showContent();
                    marketData = (MarketData) msg.obj;
                    adpterDatas.addAll(marketData.getTrendList());
                    initOptions();
                    if(!CommonUtil.isEmpty(marketData.getTrendList())){
                        txt_title.setText(marketData.getTrendList().get(0).getStackName());
                        selectedListIndex=0;
                        selectedCode = marketData.getTrendList().get(0).getStackCode();
                    }
                    adapter = new MarketAdapter(mContext, adpterDatas);
                    list_content.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    if(!CommonUtil.isEmpty(marketData.getStocks())){
                        stockManager.resetManager();
                        stockManager.setStocks(marketData.getStocks());
                        startChartInit();
                    }
                } else if (msg.what == MessageUtil.REQUEST_BREAK_UP_SUCCESS) {
                    adpterDatas.clear();
                    MarketData mkData = (MarketData) msg.obj;
                    if(!CommonUtil.isEmpty(mkData.getTrendList())){
                        adpterDatas.addAll(mkData.getTrendList());
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
                } else if(msg.what == MessageUtil.UNLOCK_BY_ADD_FEE){
                    BaseData data = (BaseData) msg.obj;
                    if(data.getStatus() == 5000){ //成功
                        loadBreakUpList();
                        setPermissionText(true);
                    }else if(data.getStatus() == 2000){
                        new ShowDialog().showTips(mContext, data.getError());
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                } else if(msg.what == MessageUtil.OPTINAL_STOCK_ADD){
                    CustmerToast.makeText(mContext, "已成功加入自选").show();
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initOptions() {
        if (marketData != null) {
            for (String str : marketData.getPoints()) {
                if (selectedDay == null) {
                    selectedDay = str;
                }
                dayList.add(new SelectionItem(str, str + "天", "day"));
            }
            if(marketData.getTradeDate() != null){
                for (String str : marketData.getTradeDate()) {
                    if (selectedDate == null) {
                        selectedDate = str;
                    }
                    dateList.add(new SelectionItem(str, str, "date"));
                }
            }

            if(marketData.getUnlocked() == 0){
                setPermissionText(false);
            }
        }
        volList.add(new SelectionItem("0", "价格突破", "vol"));
        volList.add(new SelectionItem("1", "放量突破", "vol"));
        dayOptions = creatOptionPicker(dayList);
        volOptions = creatOptionPicker(volList);
        pvTime = creatOptionPicker(dateList);
//        initTimePicker();
        txt_date.setText(selectedDate);
        txt_day_list.setText(dayList.get(0).getText());
        txt_vol.setText(volList.get(0).getText());
        selectedVol = volList.get(0).getValue();
    }

    private void setPermissionText(Boolean flag){
        if(!flag){
            txt_unlock_des.setVisibility(View.VISIBLE);
            txt_complete.setText("解锁");
            txt_complete.setEnabled(true);
        }else{
            txt_unlock_des.setVisibility(View.GONE);
            txt_complete.setText("");
            txt_complete.setEnabled(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private OptionsPickerView creatOptionPicker(final ArrayList<SelectionItem> list) {
        OptionsPickerView ops = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                SelectionItem item = list.get(options1);
                if (item.getType().equals("day")) {
                    txt_day_list.setText(item.getPickerViewText());
                    selectedDay = item.getValue();
                } else if(item.getType().equals("vol")){
                    selectedVol = item.getValue();
                    txt_vol.setText(item.getPickerViewText());
                } else if(item.getType().equals("date")){
                    selectedDate = item.getValue();
                    txt_date.setText(item.getPickerViewText());
                }
                loadBreakUpList();
            }
        })
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确认")//确认按钮文字
                .setContentTextSize(20)//设置滚轮文字大小
                .setDividerColor(getColor(R.color.colorPrimary))//设置分割线的颜色
                .setSelectOptions(0)//默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(getColor(R.color.div_white))
                .setCancelColor(getColor(R.color.colorPrimary))
                .setSubmitColor(getColor(R.color.colorPrimary))
                .setTextColorCenter(getColor(R.color.colorPrimary))
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideColor(getColor(R.color.div_white_bg)) //设置外部遮罩颜色
                .build();

        ops.setPicker(list);
        return ops;
    }

    private void initTimePicker() {//Dialog 模式下，在底部弹出
//        pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
//            @Override
//            public void onTimeSelect(Date date, View v) {
//                String time = CommonUtil.formartTimeString(date, "yyyyMMdd");
//                selectedDate = time;
//                txt_date.setText(time);
//                loadBreakUpList();
//            }
//        })
//                .setType(new boolean[]{true, true, true, false, false, false})
//                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
//                .setCancelText("取消")//取消按钮文字
//                .setSubmitText("确认")//确认按钮文字
//                .setContentTextSize(20)//设置滚轮文字大小
//                .setDividerColor(getColor(R.color.colorPrimary))//设置分割线的颜色
//                .setOutSideColor(getColor(R.color.div_white_bg)) //设置外部遮罩颜色
//                .setTextColorCenter(getColor(R.color.colorPrimary))
//                .setItemVisibleCount(6) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
//                .setLineSpacingMultiplier(2.0f)
//                .isAlphaGradient(true)
//                .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
//                .isCenterLabel(false)
//                .build();
//
//        Dialog mDialog = pvTime.getDialog();
//        if (mDialog != null) {
//
//            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    Gravity.BOTTOM);
//
//            params.leftMargin = 0;
//            params.rightMargin = 0;
//            pvTime.getDialogContainerLayout().setLayoutParams(params);
//
//            Window dialogWindow = mDialog.getWindow();
//            if (dialogWindow != null) {
//                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
//                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
//                dialogWindow.setDimAmount(0.3f);
//            }
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void bindView() {
        stockManager = new StockDisplayManager();

        list_content = findViewById(R.id.list_content);
        txt_des = findViewById(R.id.txt_des);
        txt_vol = findViewById(R.id.txt_vol);
        txt_day_list = findViewById(R.id.txt_day_list);
        txt_date = findViewById(R.id.txt_date);
        txt_unlock_des = findViewById(R.id.txt_unlock_des);
        add_optional_st = findViewById(R.id.add_optional_st);
        chartParent = findViewById(R.id.chart_parent);
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

        txt_complete.setEnabled(false);
        View.OnClickListener lis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.txt_vol && volOptions != null) {
                    volOptions.show(v);
                } else if (v.getId() == R.id.txt_day_list && dayOptions != null) {
                    dayOptions.show();
                } else if (v.getId() == R.id.txt_date && pvTime != null) {
                    pvTime.show();
                } else if(v.getId() == R.id.add_optional_st){
                    addOptionalStock();
                }
            }
        };
        txt_vol.setOnClickListener(lis);
        txt_day_list.setOnClickListener(lis);
        txt_date.setOnClickListener(lis);
        add_optional_st.setOnClickListener(lis);

        Drawable right = getResources().getDrawable(R.drawable.down);
        right.setBounds(0, 0, (int) (6*KlineStyle.pxScaleRate),
                (int) (6*KlineStyle.pxScaleRate));//必须设置图片的大小否则没有作用
        Drawable wrappedDrawable = DrawableCompat.wrap(right);
        DrawableCompat.setTint(wrappedDrawable, getColor(R.color.colorPrimary));

        //设置图片left这里如果是右边就放到第二个参数里面依次对应
        txt_day_list.setCompoundDrawables(null, null, wrappedDrawable, null);
        txt_vol.setCompoundDrawables(null, null, wrappedDrawable, null);
        txt_date.setCompoundDrawables(null, null, wrappedDrawable, null);

        chart = new KLineContent(this);
        chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        chartParent.addView(chart);
        chartParent.showLoading();
        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrendStock object = adpterDatas.get(position);
                if(!object.getStackCode().equals(selectedCode)){
                    selectedCode = object.getStackCode();
                    loadStockDetails(object.getStackCode());
                    txt_des.setText(object.getBreakUpDays());
                    txt_title.setText(object.getStackName());
                    selectedListIndex=position;
                }
            }
        });
    }
    private void startChartInit(){
        stockManager.repacklineNode(selectedDate);
        if(stockManager.getGroup() == null) {
            return;
        }
        chart.setData(stockManager.getGroup());
        chart.invalidateView();
    }


    private void loadInfo() {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/stock/market_info";
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
                                message.what = MessageUtil.REQUEST_SUCCESS;
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

    private void loadBreakUpList() {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/stock/break_up_day_list";
                Map<String, String> params = new HashMap<>();
                params.put("day", selectedDay);
                params.put("trade_date", selectedDate);
                params.put("add_vol", selectedVol);
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
                                chartParent.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        chartParent.showState("加载失败，点击重试！");
                                    }
                                });
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

    private void addOptionalStock(){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/stock/stock_optional";
                Map<String, String> params = new HashMap<>();
                params.put("stock_code", selectedCode);
                params.put("stock_name", adpterDatas.get(selectedListIndex).getStackName());
                params.put("operation", "add");
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

    @Override
    protected boolean showRight() {
        return false;
    }

    @Override
    protected void onCompletedClick() {
        mPhotoPopupWindow = new UnlockPop(MarketTrendActivity.this,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPhotoPopupWindow.dismiss();
                        //调出支付界面
                        mPayPopWindow = new PayPop(MarketTrendActivity.this, new PaymentListener() {
                            @Override
                            public void afterPayment(Boolean success) {
                                if(success){
                                    loadBreakUpList();
                                    setPermissionText(true);
                                }
                            }
                        });
                        mPayPopWindow.setAmt(marketData.getMarketPrice());
                        Map<String, String> p = new HashMap<>();
                        p.put("amt", marketData.getMarketPrice());
                        p.put("channel","android");
                        p.put("body","投资悟道解锁完整突破行情");
                        p.put("subject","解锁");
                        p.put("charge_type",PayPop.CHARGE_TYPE_MARKET+"");
                        mPayPopWindow.setParams(p);

                        View rootView = LayoutInflater.from(mContext)
                                .inflate(R.layout.activity_main, null);
                        mPayPopWindow.showAtLocation(rootView,
                                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

                    }
                }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoPopupWindow.dismiss();
                //查询累积消费
                GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
                    @Override
                    public void run() {
                        String action = "/stock/market_unlock";
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
                                        message.what = MessageUtil.UNLOCK_BY_ADD_FEE;
                                        try {
                                            message.obj = data;
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
        },marketData.getMarketPrice(), marketData.getAddAmtLimit());
        View rootView = LayoutInflater.from(mContext)
                .inflate(R.layout.activity_main, null);
        mPhotoPopupWindow.showAtLocation(rootView,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void setKlineBaseDatas(float height){
        KlineStyle.initSize();
        float baseHeight = 480f;
        KlineStyle.pxScaleRate = height/baseHeight;
        KlineStyle.resetSize();
    }

}