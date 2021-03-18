package com.cgmn.msxl.comp.pop;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.data.SettingItem;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.*;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingPop extends PopupWindow
        implements View.OnClickListener {

    private Context mContext;
    private StockHolder stoHolder;
    private RealTradeManage manage;
    private static String action;

    private View view;

    private TextView btn_close, et_price, count_minus, count_plus;
    private TextView et_count, tx_candle_count, tx_candle_amt;
    private TextView all_buy, half_buy, one_third_buy, one_fourth_buy, btn_action;


    public TradingPop(Context mContext, StockHolder holder,
                      RealTradeManage ma, String buySell) {
        this.manage = ma;
        this.mContext = mContext;
        this.stoHolder = holder;
        this.action = buySell;
        bindview();

        // 设置外部可点击
        this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = view.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }


    private void bindview() {
        if (this.action.equals("BUY")) {
            this.view = LayoutInflater.from(mContext).inflate(R.layout.buy_page_layout, null);
        } else if (this.action.equals("SELL")) {
            this.view = LayoutInflater.from(mContext).inflate(R.layout.sell_page_layout, null);
        }
        btn_close = (TextView) view.findViewById(R.id.txt_close);
        et_price = (TextView) view.findViewById(R.id.et_price);
        count_minus = (TextView) view.findViewById(R.id.count_minus);
        et_count = (TextView) view.findViewById(R.id.et_count);
        count_plus = (TextView) view.findViewById(R.id.count_plus);
        tx_candle_count = (TextView) view.findViewById(R.id.tx_candle_count);
        tx_candle_amt = (TextView) view.findViewById(R.id.tx_candle_amt);
        all_buy = (TextView) view.findViewById(R.id.all_buy);
        half_buy = (TextView) view.findViewById(R.id.half_buy);
        one_third_buy = (TextView) view.findViewById(R.id.one_third_buy);
        one_fourth_buy = (TextView) view.findViewById(R.id.one_fourth_buy);
        btn_action = (TextView) view.findViewById(R.id.btn_action);


        btn_close.setOnClickListener(this);
        count_minus.setOnClickListener(this);
        et_count.setOnClickListener(this);
        count_plus.setOnClickListener(this);
        tx_candle_count.setOnClickListener(this);
        tx_candle_amt.setOnClickListener(this);
        all_buy.setOnClickListener(this);
        half_buy.setOnClickListener(this);
        one_third_buy.setOnClickListener(this);
        one_fourth_buy.setOnClickListener(this);
        btn_action.setOnClickListener(this);

        String price = manage.getCurenPrice();
        et_price.setText(price);
        if (this.action.equals("BUY")) {
            float avaiCount = stoHolder.getAvaiBuyCount(price);
            tx_candle_count.setText("可买： " + avaiCount  + "股");
        } else {
            int avaiCount = (int) stoHolder.getAvaiLabelShare();
            tx_candle_count.setText("可卖： " + avaiCount + "股");
        }

        /* 设置弹出窗口特征 */
        // 设置视图
        this.setContentView(this.view);
        // 设置动画效果
        this.setAnimationStyle(R.style.popwindow_anim_style);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(dw);
    }

    private void setAmountText(String amont){
        tx_candle_amt.setText("金额： "+ amont);
        if(this.action.equals("BUY")){
            tx_candle_amt.setTextColor(mContext.getResources().getColor(R.color.main_red_color));
        }else{
            tx_candle_amt.setTextColor(mContext.getResources().getColor(R.color.main_blue_color));
        }
    }

    private void positionManage(float persent){
        float price = CommonUtil.castFloatFromString(manage.getCurenPrice());
        if (this.action.equals("BUY")) {
            int avaiCount = stoHolder.getAvaiBuyCount(manage.getCurenPrice(), persent);
            et_count.setText(avaiCount + "");
            setAmountText(CommonUtil.formatNumer(avaiCount*price));
        }else{
            int avaiCount = stoHolder.getAvaiSellCount(persent);
            et_count.setText(avaiCount+"");
            setAmountText(CommonUtil.formatNumer(avaiCount*price));
        }
    }

    private void onAction(){
        float price = CommonUtil.castFloatFromString(manage.getCurenPrice());
        if(CommonUtil.isEmpty(et_count.getText().toString())){
            CustmerToast.makeText(mContext, mContext.getString(R.string.need_vol)).show();
            return;
        }
        int count = Integer.valueOf(et_count.getText().toString());
        if(count == 0){
            CustmerToast.makeText(mContext, mContext.getString(R.string.need_vol)).show();
            return;
        }
        if (this.action.equals("BUY")) {
            stoHolder.buyStock(count, price,
                    manage.getCurrentK().getStackCode(),
                    manage.getCurrentK().getStackName());
            if(count == stoHolder.getHoldShare()){
                stoHolder.whenNextDay();
            }
            List<Integer> bcks = ModeManager.getInstance().getBuyCheck();
            Map<String, Object> values = new HashMap<>();
            values.put("nodes", manage.getGroup().getNodes());
            values.put("kStatus", manage.getkStatus());
            values.put("isCreateHold", count == stoHolder.getHoldShare());
            values.put("startRate", stoHolder.getStartRate(count*price));
            values.put("totalRate", stoHolder.getHoldRate());
            List<String> messges = new ArrayList<>();
            for(SettingItem sItem : stoHolder.getModeList()){
                if(bcks.contains(sItem.getModedType())){
                    boolean flag = ModeManager.getInstance().assertionOverMode(sItem.getModedType(), values);
                    if(flag && !stoHolder.exists(sItem.getModedType())){
                        stoHolder.addOverType(sItem.getModedType());
                        messges.add(sItem.getModeText());
                    }
                }
            }
            if(messges.size() > 0){
                CustmerToast.makeText(mContext,
                        "违背模式\n " + StringUtils.join(messges, "\n"), Toast.LENGTH_LONG).show();
            }
        }else{
            float rate = stoHolder.getPlRateNum();
            stoHolder.sellStock(count, price);
            if(stoHolder.getHoldShare() == 0 && rate >= 0.2){
                //上传出色交易
                uploadTrade(rate);
            }
        }
        // 销毁弹出框
        dismiss();
    }

    private void onCountChange( int rate){
        int count=0;
        if(!CommonUtil.isEmpty(et_count.getText().toString())){
            count = Integer.valueOf(et_count.getText().toString());
        }
        int changed = count + 100*rate;
        float price = CommonUtil.castFloatFromString(manage.getCurenPrice());
        if (this.action.equals("BUY")) {
            int avaiCount = stoHolder.getAvaiBuyCount(manage.getCurenPrice());
            if(changed > avaiCount || changed < 0){
                return;
            }
            et_count.setText(changed + "");
            setAmountText(CommonUtil.formatNumer(changed * price));
        } else {
            if(changed > stoHolder.getAvaiLabelShare() || changed < 0){
                return;
            }
            et_count.setText(changed + "");
            setAmountText(CommonUtil.formatNumer(changed * price));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.txt_close) {
            // 销毁弹出框
            dismiss();
        }else if(v.getId() == R.id.all_buy){
            positionManage(1);
        }else if(v.getId() == R.id.half_buy){
            positionManage(0.5f);
        }else if(v.getId() == R.id.one_third_buy){
            positionManage(0.2f);
        }else if(v.getId() == R.id.one_fourth_buy){
            positionManage(0.1f);
        }else if(v.getId() == R.id.btn_action){
            onAction();
        }else if(v.getId() == R.id.count_minus){
            onCountChange(-1);
        }else if(v.getId() == R.id.count_plus){
            onCountChange(1);
        }
    }

    /**
     * 将获利20个点以上的交易上传 用于跑马灯展示
     * @param rate
     */
    private void uploadTrade(final float rate){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("name", GlobalDataHelper.getUserName(mContext));
                params.put("stockName", stoHolder.getStackName());
                params.put("plRate", rate+"");
                params.put("token", GlobalDataHelper.getToken(mContext));
                String action = "/common/outstanding_trade";
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        action, params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                            }
                            @Override
                            public void onResponse(BaseData data) {
                            }
                        });
            }
        });
    }
}
