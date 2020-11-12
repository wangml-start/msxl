package com.cgmn.msxl.ac;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.comp.adpter.VipAdpter;
import com.cgmn.msxl.comp.pop.PayPop;
import com.cgmn.msxl.data.VipItem;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
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

public class VIPActivity extends BaseOtherActivity {
    private static final String TAG = VIPActivity.class.getSimpleName();
    public static String LEVEL_1 = "LEVEL_1";
    public static String LEVEL_2 = "LEVEL_2";

    private LinearLayout line_level1, line_level2;
    private GridView grid_view;
    private NetImageView icon_head;
    private TextView txt_u_name, txt_des, btn_pay;

    private String permissionKey = LEVEL_2;

    private Handler mHandler;
    private List<VipItem> mData = null;
    private VipAdpter myAdapter = null;
    private VipDataSetting setting;
    private VipItem selectedVip = null;
    private PayPop mPayPopWindow;
    protected ProgressDialog dialog;

    @Override
    protected int getContentView() {
        return R.layout.vip_page_layout;
    }

    @Override
    protected String setTitle(){
        return getString(R.string.vip_way);
    }

    private void setSelLevel(){
        if(permissionKey.equals(LEVEL_2)){
            line_level2.setBackgroundResource(R.drawable.bt_back_pressed);
            line_level1.setBackgroundResource(R.drawable.bt_back_nomal);
        }else if(permissionKey.equals(LEVEL_1)){
            line_level1.setBackgroundResource(R.drawable.bt_back_pressed);
            line_level2.setBackgroundResource(R.drawable.bt_back_nomal);
        }

        if(getParms() == null){
            btn_pay.setEnabled(false);
        }else{
            btn_pay.setEnabled(true);
        }
    }

    @Override
    protected void init(){
        bindView();
        initAdpter();

        setSelLevel();
    }

    private void bindView(){
        line_level1 = findViewById(R.id.line_level1);
        line_level2 = findViewById(R.id.line_level2);
        btn_pay = findViewById(R.id.btn_pay);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.line_level1){
                    permissionKey = LEVEL_1;
                    myAdapter.setRate(1);
                    myAdapter.notifyDataSetChanged();
                    setSelLevel();
                }else if(v.getId() == R.id.line_level2){
                    permissionKey = LEVEL_2;
                    myAdapter.setRate(setting.getRate());
                    myAdapter.notifyDataSetChanged();
                    setSelLevel();
                }else if(v.getId() == R.id.btn_pay){
                    showPayPop();
                }
            }
        };
        line_level1.setOnClickListener(listener);
        line_level2.setOnClickListener(listener);
        btn_pay.setOnClickListener(listener);


        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    setting = (VipDataSetting) msg.obj;
                    afterLoad();
                } else if(MessageUtil.VIP_CHARGE_RESPONSE == msg.what){
                    dialog.cancel();
                    VipDataSetting res = (VipDataSetting) msg.obj;
                    setting.setExpireDate(res.getExpireDate());
                    setting.setLevel(res.getLevel());
                    setPermission();
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    dialog.cancel();
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });

        grid_view = findViewById(R.id.grid_view);
        txt_u_name = findViewById(R.id.txt_u_name);
        txt_des = findViewById(R.id.txt_des);
        icon_head = findViewById(R.id.icon_head);
        byte[] cut = GlobalDataHelper.getUserCut(mContext);
        if(cut != null && cut.length > 0){
            icon_head.setImageContent(cut);
        }else{
            icon_head.setImageName(GlobalDataHelper.getUserAcc(mContext));
            icon_head.setImageURL(GlobalDataHelper.getUserPortraitUrl(mContext));
        }
        txt_u_name.setText(GlobalDataHelper.getUserName(mContext));

        dialog = new ProgressDialog(mContext);
        dialog.setMessage("正在提交...");
    }

    private void setPermission(){
        if(setting != null && !CommonUtil.isEmpty(setting.getExpireDate())){
            String des = String.format("当前权限: %s   到期日: %s",
                    setting.getLevel(), CommonUtil.formartTimeString(setting.getExpireDate(), "yyyy-MM-dd HH:mm:ss"));
            txt_des.setText(des);
        }
    }

    private void afterLoad(){
        setPermission();
        if (!CommonUtil.isEmpty(setting.getList())) {
            mData = new ArrayList<>();
            for (Map<String, Object> item : setting.getList()) {
                VipItem viewItem = new VipItem();
                viewItem.setTimeType((String) item.get("time_type"));
                viewItem.setAmt(((Double) item.get("amt")).intValue());
                viewItem.setNum(((Double) item.get("num")).intValue());
                mData.add(viewItem);
            }
        }
        myAdapter = new VipAdpter(mContext, mData);
        myAdapter.setRate(setting.getRate());
        grid_view.setAdapter(myAdapter);
        grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedVip = mData.get(position);
                for(int i=0;i<parent.getCount();i++){
                    View v = parent.getChildAt(i);
                    if (position == i) {//当前选中的Item改变背景颜色
                        view.setBackgroundResource(R.drawable.bt_back_pressed);
                    } else {
                        v.setBackgroundResource(R.drawable.bt_back_nomal);
                    }
                }
                setSelLevel();
            }});
    }

    private void initAdpter(){
        //加载配置数据
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/vip_query/vip_setting";
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        action, params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<VipDataSetting>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(VipDataSetting data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.REQUEST_SUCCESS;
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
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
            }
        });
    }

    @Override
    protected boolean showRight(){
        return false;
    }
    @Override
    protected boolean showComplate(){
        return false;
    }

    private Map<String, String> getParms(){
        if(setting == null){
            return null;
        }
        Map<String, String> p = new HashMap<>();
        float rate = 1;
        if(permissionKey.equals(LEVEL_2)){
            p.put("per_name", "Level2");
            p.put("per_key", LEVEL_2);
            rate = setting.getRate();
        }else if(permissionKey.equals(LEVEL_1)){
            p.put("per_name", "Level1");
            p.put("per_key", LEVEL_1);
        }else {
            return null;
        }

        if(selectedVip == null){
            return null;
        }
        p.put("time_type", selectedVip.getTimeType());
        p.put("num", selectedVip.getNum()+"");
        Float menoy = selectedVip.getAmt() * rate;
        p.put("amt", menoy.intValue()+"");

        return p;
    }


    private void showPayPop(){
        mPayPopWindow = new PayPop(VIPActivity.this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPayPopWindow.dismiss();
                onZFBCilck();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPayPopWindow.dismiss();
                onWXCilck();
            }
        });
        mPayPopWindow.setAmt(getParms().get("amt"));

        View rootView = LayoutInflater.from(mContext)
                .inflate(R.layout.vip_page_layout, null);
        mPayPopWindow.showAtLocation(rootView,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }

    private void onZFBCilck(){
        sendToServer();
    }

    private void onWXCilck(){
        sendToServer();
    }

    private void sendToServer(){
        dialog.show();
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/vip_purchase/vip_charge";
                Map<String, String> params = getParms();
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        action, params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<VipDataSetting>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(VipDataSetting data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.VIP_CHARGE_RESPONSE;
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
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
            }
        });
    }

    @Override
    public void finish() {
        dialog.dismiss();
        super.finish();
    }
}