package com.cgmn.msxl.ac;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import androidx.core.graphics.drawable.DrawableCompat;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.MarketAdapter;
import com.cgmn.msxl.comp.k.KlineChart;
import com.cgmn.msxl.data.SelectionItem;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.MarketData;
import com.cgmn.msxl.server_interface.TrendStock;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.service.StockDisplayManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.*;

public class MarketTrendActivity extends BaseOtherActivity {
    private static final String TAG = MarketTrendActivity.class.getSimpleName();

    private Handler mHandler;
    private TextView txt_day_list, txt_date, txt_vol,txt_des;
    private LinearLayout chartParent;
    private ListView list_content;
    private MarketAdapter adapter;
    private KlineChart chart;
    List<TrendStock> adpterDatas = new ArrayList<>();
    private OptionsPickerView dayOptions, volOptions, pvTime;
    private ArrayList<SelectionItem> dayList = new ArrayList<>(), volList = new ArrayList<>();
    private ArrayList<SelectionItem> dateList = new ArrayList<>();
//    private TimePickerView pvTime;
    private MarketData marketData;
    private String selectedDay, selectedVol = "0", selectedDate;
    private String selectedCode;
    private StockDisplayManager stockManager;

    @Override
    protected int getContentView() {
        return R.layout.market_trend_layout;
    }

    @Override
    protected String setTitle() {
        return getString(R.string.trend_break_up);
    }


    @Override
    protected void init() {
        bindView();
        initMessageHandler();
        loadInfo();

    }

    private void initMessageHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (MessageUtil.REQUEST_SUCCESS == msg.what) {
                    marketData = (MarketData) msg.obj;
                    adpterDatas.addAll(marketData.getTrendList());
                    initOptions();
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
                    MarketData mkData = (MarketData) msg.obj;
                    if(!CommonUtil.isEmpty(mkData.getStocks())){
                        stockManager.resetManager();
                        stockManager.setStocks(mkData.getStocks());
                        startChartInit();
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

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

    private void bindView() {
        stockManager = new StockDisplayManager();

        list_content = findViewById(R.id.list_content);
        txt_des = findViewById(R.id.txt_des);
        txt_vol = findViewById(R.id.txt_vol);
        txt_day_list = findViewById(R.id.txt_day_list);
        txt_date = findViewById(R.id.txt_date);
        chartParent = findViewById(R.id.chart_parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        LinearLayout.LayoutParams kparams = (LinearLayout.LayoutParams) chartParent.getLayoutParams();
        kparams.height = ((Double) (screenHeight * 0.5)).intValue();
        chartParent.setLayoutParams(kparams);

        View.OnClickListener lis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.txt_vol && volOptions != null) {
                    volOptions.show(v);
                } else if (v.getId() == R.id.txt_day_list && dayOptions != null) {
                    dayOptions.show();
                } else if (v.getId() == R.id.txt_date && pvTime != null) {
                    pvTime.show();
                }
            }
        };
        txt_vol.setOnClickListener(lis);
        txt_day_list.setOnClickListener(lis);
        txt_date.setOnClickListener(lis);
        txt_complete.setText("解锁");
        Drawable right = getResources().getDrawable(R.drawable.down);
        right.setBounds(0, 0, 25, 25);//必须设置图片的大小否则没有作用
        Drawable wrappedDrawable = DrawableCompat.wrap(right);
        DrawableCompat.setTint(wrappedDrawable, getColor(R.color.colorPrimary));

        //设置图片left这里如果是右边就放到第二个参数里面依次对应
        txt_day_list.setCompoundDrawables(null, null, wrappedDrawable, null);
        txt_vol.setCompoundDrawables(null, null, wrappedDrawable, null);
        txt_date.setCompoundDrawables(null, null, wrappedDrawable, null);

        chart = new KlineChart(this);
        chart.getKlinePaint().setVisibleCount(60);
        chart.getKlinePaint().setkLineBold(1f);
        chartParent.addView(chart);

        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrendStock object = adpterDatas.get(position);
                if(!object.getStackCode().equals(selectedCode)){
                    selectedCode = object.getStackCode();
                    loadStockDetails(object.getStackCode());
                    txt_des.setText(object.getBreakUpDays());
                }
            }
        });
    }
    private void startChartInit(){
        stockManager.repacklineNode();
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
                        PropertyService.getInstance().getKey("serverUrl"),
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
                        PropertyService.getInstance().getKey("serverUrl"),
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
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/stock/stock_details";
                Map<String, String> params = new HashMap<>();
                params.put("code", code);
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
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

    @Override
    protected boolean showRight() {
        return false;
    }

    @Override
    protected boolean showComplate() {
        return true;
    }

}