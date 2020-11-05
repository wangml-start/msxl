package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.MissModeAdpter;
import com.cgmn.msxl.data.SettingItem;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.ModeManager;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViolateModeActivity extends BaseOtherActivity {
    private static String TAG = ViolateModeActivity.class.getSimpleName();
    private ListView list_content;
    private MissModeAdpter myAdapter = null;
    //消息处理
    private Handler mHandler;
    private List<SettingItem> mData = null;

    @Override
    protected void init() {
        bindView();
        initAdpter();
    }

    @Override
    protected int getContentView() {
        return R.layout.miss_mode_layout;
    }

    @Override
    protected String setTitle() {
        return getString(R.string.miss_mode_st);
    }

    @Override
    protected boolean showRight() {
        return false;
    }

    @Override
    protected boolean showComplate() {
        return false;
    }

    @SuppressLint("WrongViewCast")
    private void bindView() {
        mContext = this;
        list_content = findViewById(R.id.list_content);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    List<Map<String, Object>> misses = (List<Map<String, Object>>) msg.obj;
                    if (!CommonUtil.isEmpty(misses)) {
                        mData = new ArrayList<>();
                        for (Map<String, Object> item : misses) {
                            Integer type = ((Double) item.get("miss_type")).intValue();
                            Integer num = ((Double) item.get("num")).intValue();
                            String text = ModeManager.getInstance().transType(type);
                            if (CommonUtil.isEmpty(text)) {
                                continue;
                            }
                            SettingItem viewItem = new SettingItem(text, num + " 次");
                            mData.add(viewItem);
                        }
                        myAdapter = new MissModeAdpter(mContext, mData);
                        list_content.setAdapter(myAdapter);
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    private void initAdpter() {
        //加载用户信息
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/statistic/miss_mode_statics";
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
                                    message.obj = data.getRecords();
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
}
