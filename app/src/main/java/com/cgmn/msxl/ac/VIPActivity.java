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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.comp.adpter.VipAdpter;
import com.cgmn.msxl.comp.pop.PayPop;
import com.cgmn.msxl.data.VipItem;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.PaymentListener;
import com.cgmn.msxl.server_interface.VipDataSetting;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

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
            line_level1.setBackgroundResource(R.drawable.bt_back_blank);
            if(setting != null && "Level1".equals(setting.getLevel())){
                btn_pay.setText(R.string.vip_up_grade);
            }
        }else if(permissionKey.equals(LEVEL_1)){
            line_level1.setBackgroundResource(R.drawable.bt_back_pressed);
            line_level2.setBackgroundResource(R.drawable.bt_back_blank);
            btn_pay.setText(R.string.vip_pay);
        }

        if(getParms() == null){
            btn_pay.setEnabled(false);
        }else{
            btn_pay.setEnabled(true);
        }
    }

    @Override
    protected void init(){
        initMessageHandler();
        bindView();
        initAdpter();

        setSelLevel();
    }




    private void initMessageHandler(){
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    setting = (VipDataSetting) msg.obj;
                    afterLoad();
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    dialog.cancel();
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                } else if(msg.what == MessageUtil.VIP_UP_GRADE_CHARGE_RESPONSE){
                    VipDataSetting vipItem = (VipDataSetting) msg.obj;
                    showPayPop(vipItem.getUpGradeAmt());
                }
                return false;
            }
        });

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
                    if(setting != null && "Level1".equals(setting.getLevel())
                            && permissionKey.equals(LEVEL_2)){//升级
                        calcuVipUpGrade();
                        CustmerToast.makeText(mContext, "正请求数据。。。").show();
                    }else{
                        showPayPop(0);
                    }
                }
            }
        };
        line_level1.setOnClickListener(listener);
        line_level2.setOnClickListener(listener);
        btn_pay.setOnClickListener(listener);

        grid_view = findViewById(R.id.grid_view);
        txt_u_name = findViewById(R.id.txt_u_name);
        txt_des = findViewById(R.id.txt_des);
        icon_head = findViewById(R.id.icon_head);
        byte[] cut = GlobalDataHelper.getUserCut(mContext);
        if(cut != null && cut.length > 0){
            icon_head.setImageContent(cut);
        }else{
            Glide.with(mContext).load(R.drawable.user_logo)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(icon_head);
        }
        txt_u_name.setText(GlobalDataHelper.getUserName(mContext));

        dialog = new ProgressDialog(mContext);
        dialog.setMessage("正在提交...");
    }

    private void setPermission(){
        if(setting != null && !CommonUtil.isEmpty(setting.getExpireDate())){
            String des = String.format("当前权限: %s   到期日: %s",
                    setting.getLevel(), setting.getExpireDate());
            txt_des.setText(des);
        }
    }

    private void afterLoad(){
        setPermission();
        setSelLevel();
        if (!CommonUtil.isEmpty(setting.getList())) {
            mData = setting.getList();
        }
        if (myAdapter == null) {
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
                            v.setBackgroundResource(R.drawable.bt_back_blank);
                        }
                    }
                    setSelLevel();
                }});
        }
        myAdapter.notifyDataSetChanged();
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

    private void calcuVipUpGrade(){
        //加载配置数据
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/vip_query/vip_up_grade";
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
                                message.what = MessageUtil.VIP_UP_GRADE_CHARGE_RESPONSE;
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
        p.put("channel","android");
        p.put("subject","会员");
        p.put("body","投资悟道开通开通会员");
        p.put("charge_type",PayPop.CHARGE_TYPE_VIP+"");
        if(setting != null && "Level1".equals(setting.getLevel()) && permissionKey.equals(LEVEL_2)){
            p.put("body","投资悟道会员升级");
            p.put("charge_type",PayPop.CHARGE_TYPE_VIP_UP_GRADE+"");
        }
        return p;
    }


    private void showPayPop(Integer subAmt){
        mPayPopWindow = new PayPop(VIPActivity.this, new PaymentListener() {
            @Override
            public void afterPayment(Boolean success) {
//                initAdpter();
            }
        });
        if(subAmt == null){
            subAmt = 0;
        }
        Map<String, String> p = getParms();
        Integer orderAmt = Double.valueOf(p.get("amt")).intValue();
        mPayPopWindow.setAmt((orderAmt-subAmt)+"");
        p.put("amt", (orderAmt-subAmt)+"");
        mPayPopWindow.setParams(p);

        View rootView = LayoutInflater.from(mContext)
                .inflate(R.layout.vip_page_layout, null);
        mPayPopWindow.showAtLocation(rootView,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }


    @Override
    public void finish() {
        dialog.dismiss();
        super.finish();
    }
}