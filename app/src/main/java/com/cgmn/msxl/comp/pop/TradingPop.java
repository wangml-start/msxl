package com.cgmn.msxl.comp.pop;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.k.time.TimeShareGroup;
import com.cgmn.msxl.data.SettingItem;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.*;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
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
    private TimeShareGroup timeShare;

    private View view;

    private TextView btn_close, et_price, count_minus, count_plus;
    private TextView et_count, tx_candle_count, tx_candle_amt;
    private TextView first_pos, second_pos, third_pos, four_pos, five_pos, btn_action;

    private Map<String, String> positionMap;

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

    public void setTimeShare(TimeShareGroup timeShare) {
        this.timeShare = timeShare;
    }

    public String getPrice(){
        String price = "0";
        if(timeShare != null && timeShare.current != null){
            price = CommonUtil.formatNumer(timeShare.current.getPrice());
        }else {
             price = manage.getCurenPrice();
        }
        et_price.setText(price);
        return price;
    }

    public void setPositions(){
        final AppSqlHelper dbHelper = new AppSqlHelper(mContext);
        positionMap =  dbHelper.getSystenSettings();
        if(positionMap == null){
            positionMap = new HashMap<>();
        }
        if(!CommonUtil.isEmpty(positionMap.get("FIRST_POS"))){
            if("1/1".equals(positionMap.get("FIRST_POS"))){
                first_pos.setText("满仓");
            }else{
                first_pos.setText(positionMap.get("FIRST_POS"));
            }
        }else{
            first_pos.setText("满仓");
        }
        if(!CommonUtil.isEmpty(positionMap.get("SECOND_POS"))){
            second_pos.setText(positionMap.get("SECOND_POS"));
        }else{
            second_pos.setText("1/2");
        }
        if(!CommonUtil.isEmpty(positionMap.get("THIRD_POS"))){
            third_pos.setText(positionMap.get("THIRD_POS"));
        }else{
            third_pos.setText("1/4");
        }
        if(!CommonUtil.isEmpty(positionMap.get("FOUR_POS"))){
            four_pos.setText(positionMap.get("FOUR_POS"));
        }else{
            four_pos.setText("1/5");
        }
        if(!CommonUtil.isEmpty(positionMap.get("FIVE_POS"))){
            five_pos.setText(positionMap.get("FIVE_POS"));
        }else{
            five_pos.setText("1/10");
        }
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

        first_pos = (TextView) view.findViewById(R.id.first_pos);
        second_pos = (TextView) view.findViewById(R.id.second_pos);
        third_pos = (TextView) view.findViewById(R.id.third_pos);
        four_pos = (TextView) view.findViewById(R.id.four_pos);
        five_pos = (TextView) view.findViewById(R.id.five_pos);
        btn_action = (TextView) view.findViewById(R.id.btn_action);


        btn_close.setOnClickListener(this);
        count_minus.setOnClickListener(this);
        et_count.setOnClickListener(this);
        count_plus.setOnClickListener(this);
        tx_candle_count.setOnClickListener(this);
        tx_candle_amt.setOnClickListener(this);
        first_pos.setOnClickListener(this);
        second_pos.setOnClickListener(this);
        third_pos.setOnClickListener(this);
        four_pos.setOnClickListener(this);
        five_pos.setOnClickListener(this);
        btn_action.setOnClickListener(this);

        String price = getPrice();
        setPositions();

        String uom = "股";
        if(CommonUtil.isKzz(manage.getCurrentK().getStackCode())){
            uom = "张";
        }
        if (this.action.equals("BUY")) {
            long avaiCount = stoHolder.getAvaiBuyCount(price, manage.getCurrentK().getStackCode());
            tx_candle_count.setText("可买： " + CommonUtil.formatAmt(avaiCount)  + uom);
        } else {
            Long avaiCount = stoHolder.getAvaiLabelShare();
            tx_candle_count.setText("可卖： " + CommonUtil.formatAmt(avaiCount) + uom);
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
        float price = CommonUtil.castFloatFromString(getPrice());
        if (this.action.equals("BUY")) {
            long avaiCount = stoHolder.getAvaiBuyCount(getPrice(), persent, manage.getCurrentK().getStackCode());
            et_count.setText(avaiCount + "");
            setAmountText(CommonUtil.formatAmt(avaiCount*price));
        }else{
            long avaiCount = stoHolder.getAvaiSellCount(persent);
            et_count.setText(avaiCount+"");
            setAmountText(CommonUtil.formatAmt(avaiCount*price));
        }
    }

    private void onAction(){
        float price = CommonUtil.castFloatFromString(getPrice());
        if(CommonUtil.isEmpty(et_count.getText().toString())){
            CustmerToast.makeText(mContext, mContext.getString(R.string.need_vol)).show();
            return;
        }
        int count = Integer.valueOf(et_count.getText().toString());
        if(count <= 0){
            CustmerToast.makeText(mContext, mContext.getString(R.string.need_vol)).show();
            return;
        }
        if (this.action.equals("BUY")) {
            if(CommonUtil.isKzz(manage.getCurrentK().getStackCode())){
                stoHolder.buyKzz(count, price,
                        manage.getCurrentK().getStackCode(),
                        manage.getCurrentK().getStackName());
            }else{
                stoHolder.buyStock(count, price,
                        manage.getCurrentK().getStackCode(),
                        manage.getCurrentK().getStackName());
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
            }

            if(count == stoHolder.getHoldShare()){
                stoHolder.whenNextDay();
            }

            //标记
            manage.getCurrentK().setOpChar("B");
            if(timeShare != null && timeShare.current != null){
                timeShare.setOpInfo("B", price, timeShare.timer.getTimeMinute());
            }
        }else{
            float rate = stoHolder.getPlRateNum();
            if(CommonUtil.isKzz(this.stoHolder.getCode())){
                stoHolder.sellKzz(count, price);
            }else{
                stoHolder.sellStock(count, price);
            }
            if(stoHolder.getHoldShare() == 0){
                //标记
                manage.getCurrentK().setOpChar("S");
            }else{
                //标记
                manage.getCurrentK().setOpChar("T");
            }
            if(timeShare != null && timeShare.current != null){
                timeShare.setOpInfo("S", price, timeShare.timer.getTimeMinute());
            }
            if(stoHolder.getHoldShare() == 0 && (rate >= 0.2 || rate <= -1)){
                //上传出色交易
                if(!CommonUtil.isEmpty(stoHolder.getStackName()))
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
        int uom = 100;
        if(CommonUtil.isKzz(manage.getCurrentK().getStackCode())){
            if(manage.getCurrentK().getStackCode().startsWith("11")){
                uom = 10;
            }
        }
        int changed = count + uom*rate;
        float price = CommonUtil.castFloatFromString(getPrice());
        if (this.action.equals("BUY")) {
            long avaiCount = stoHolder.getAvaiBuyCount(getPrice(), manage.getCurrentK().getStackCode());
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
        }else if(v.getId() == R.id.first_pos){
            float rate = calcRate("FIRST_POS");
            if(rate < 0){
                positionManage(1);
            }else{
                positionManage(rate);
            }
        }else if(v.getId() == R.id.second_pos){
            float rate = calcRate("SECOND_POS");
            if(rate < 0){
                positionManage(0.5f);
            }else{
                positionManage(rate);
            }
        }else if(v.getId() == R.id.third_pos){
            float rate = calcRate("THIRD_POS");
            if(rate < 0){
                positionManage(0.25f);
            }else{
                positionManage(rate);
            }
        }else if(v.getId() == R.id.four_pos){
            float rate = calcRate("FOUR_POS");
            if(rate < 0){
                positionManage(0.2f);
            }else{
                positionManage(rate);
            }
        } else if(v.getId() == R.id.five_pos){
            float rate = calcRate("FIVE_POS");
            if(rate < 0){
                positionManage(0.1f);
            }else{
                positionManage(rate);
            }
        }else if(v.getId() == R.id.btn_action){
            onAction();
        }else if(v.getId() == R.id.count_minus){
            onCountChange(-1);
        }else if(v.getId() == R.id.count_plus){
            onCountChange(1);
        }
    }

    private float calcRate(String name){
        if(CommonUtil.isEmpty(positionMap.get(name))){
            return  -1;
        }
        String[] arr = positionMap.get(name).split(ConstantHelper.positionSplit);
        float rate = Float.valueOf(arr[0]) / Float.valueOf(arr[1]);
        if(rate > 1){
            rate = 1;
        }
        return rate;
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
                        ConstantHelper.serverUrl,
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
