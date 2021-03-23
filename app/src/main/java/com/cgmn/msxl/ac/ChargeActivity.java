package com.cgmn.msxl.ac;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.ChargeAdpter;
import com.cgmn.msxl.comp.pop.PayPop;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.PaymentListener;
import com.cgmn.msxl.server_interface.VipDataSetting;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChargeActivity extends BaseOtherActivity {
    private static final String TAG = ChargeActivity.class.getSimpleName();

    private TextView txt_des, btn_pay;
    private GridView grid_view;

    private Handler mHandler;
    private ChargeAdpter myAdapter = null;
    private PayPop mPayPopWindow;
    private Integer selectedCharge;

    private List<Integer> mData = new ArrayList<>();

    @Override
    protected int getContentView() {
        return R.layout.charge_page_layout;
    }

    @Override
    protected String setTitle(){
        return getString(R.string.chargev_way);
    }


    @Override
    protected void init(){
        bindView();
        initAdpter();

    }

    private void bindView(){
        btn_pay = findViewById(R.id.btn_pay);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_pay) {
                    showPayPop();
                }
            }
        };
        btn_pay.setOnClickListener(listener);
        btn_pay.setEnabled(false);
        grid_view = findViewById(R.id.grid_view);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(MessageUtil.VIP_CHARGE_RESPONSE == msg.what){
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });

        txt_des = findViewById(R.id.txt_des);
    }

    private void setPermission(){
//        String des = String.format("当前权限: %s   到期日: %s",
//                setting.getLevel(), CommonUtil.formartTimeString(setting.getExpireDate(), "yyyy-MM-dd HH:mm:ss"));
//        txt_des.setText(des);
    }

    private void initAdpter(){
        mData.add(5);mData.add(10);mData.add(20);
        mData.add(30);mData.add(50);mData.add(100);
        myAdapter = new ChargeAdpter(mContext, mData);
        grid_view.setAdapter(myAdapter);
        grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCharge = mData.get(position);
                for(int i=0;i<parent.getCount();i++){
                    View v = parent.getChildAt(i);
                    if (position == i) {//当前选中的Item改变背景颜色
                        view.setBackgroundResource(R.drawable.bt_back_pressed);
                    } else {
                        v.setBackgroundResource(R.drawable.bt_back_blank);
                    }
                }
                btn_pay.setEnabled(true);
            }});
    }

    @Override
    protected boolean showRight(){
        return false;
    }
    @Override
    protected boolean showComplate(){
        return false;
    }



    private void showPayPop(){
        mPayPopWindow = new PayPop(ChargeActivity.this, new PaymentListener() {
            @Override
            public void afterPayment(Boolean success) {
            }
        });
        mPayPopWindow.setAmt(selectedCharge+"");
        Map<String, String> params = new HashMap<>();
        params.put("amt", selectedCharge+"");
        params.put("channel","android");
        params.put("charge_type",PayPop.CHARGE_TYPE_CASH+"");
        params.put("body","投资悟道充值");
        params.put("subject","投资悟道充值");
        params.put("token", GlobalDataHelper.getToken(mContext));
        mPayPopWindow.setParams(params);

        View rootView = LayoutInflater.from(mContext)
                .inflate(R.layout.vip_page_layout, null);
        mPayPopWindow.showAtLocation(rootView,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }


    @Override
    public void finish() {
        super.finish();
    }
}