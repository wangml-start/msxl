package com.cgmn.msxl.ac;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAgrementActivity extends BaseOtherActivity {
    private static final String TAG = UserAgrementActivity.class.getSimpleName();

    private Handler mHandler;
    private TextView txt_content;
    @Override
    protected int getContentView() {
        return R.layout.user_agremnet_layout;
    }

    @Override
    protected String setTitle(){
        return getString(R.string.user_agrement_title);
    }


    @Override
    protected void init(){
        bindView();
        loadInfo();

    }
    private void loadInfo(){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/common/query_user_agrement";
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        action, null);
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
                                    message.obj = data.getInfoList();
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

    private void bindView(){
        txt_content = findViewById(R.id.txt_content);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(MessageUtil.REQUEST_SUCCESS == msg.what){
                    List<String> list = (List<String>) msg.obj;
                    txt_content.setText(list.get(0));
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
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

}