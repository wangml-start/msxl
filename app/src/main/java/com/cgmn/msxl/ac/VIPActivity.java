package com.cgmn.msxl.ac;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.VipAdpter;
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

    private LinearLayout line_level1, line_level2;
    private GridView grid_view;

    private String permissionKey = "LEVEL2";

    private Handler mHandler;
    private List<VipItem> mData = null;
    private VipAdpter myAdapter = null;
    private Float rate;
    private VipItem selectedVip = null;

    @Override
    protected int getContentView() {
        return R.layout.vip_page_layout;
    }

    @Override
    protected String setTitle(){
        return getString(R.string.vip_way);
    }

    private void setSelLevel(){
        if(permissionKey.equals("LEVEL2")){
            line_level2.setBackgroundResource(R.drawable.bt_back_pressed);
            line_level1.setBackgroundResource(R.drawable.bt_back_nomal);
        }else if(permissionKey.equals("LEVEL1")){
            line_level1.setBackgroundResource(R.drawable.bt_back_pressed);
            line_level2.setBackgroundResource(R.drawable.bt_back_nomal);
        }
    }

    @Override
    protected void init(){
        bindView();
        setSelLevel();
        initAdpter();
    }

    private void bindView(){
        line_level1 = findViewById(R.id.line_level1);
        line_level2 = findViewById(R.id.line_level2);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.line_level1){
                    permissionKey = "LEVEL1";
                    myAdapter.setRate(1);
                }else if(v.getId() == R.id.line_level2){
                    permissionKey = "LEVEL2";
                    myAdapter.setRate(rate);
                }
                myAdapter.notifyDataSetChanged();
                setSelLevel();
            }
        };
        line_level1.setOnClickListener(listener);
        line_level2.setOnClickListener(listener);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    VipDataSetting datas = (VipDataSetting) msg.obj;
                    if (!CommonUtil.isEmpty(datas.getList())) {
                        mData = new ArrayList<>();
                        for (Map<String, Object> item : datas.getList()) {
                            VipItem viewItem = new VipItem();
                            viewItem.setTimeType((String) item.get("time_type"));
                            viewItem.setAmt(((Double) item.get("amt")).intValue());
                            viewItem.setNum(((Double) item.get("num")).intValue());
                            mData.add(viewItem);
                        }
                        myAdapter = new VipAdpter(mContext, mData);
                        rate = datas.getRate();
                        myAdapter.setRate(datas.getRate());
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
                            }});
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });

        grid_view = findViewById(R.id.grid_view);
    }

    private void initAdpter(){
//加载用户信息
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
    };
    @Override
    protected boolean showComplate(){
        return false;
    };




}