package com.cgmn.msxl.comp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.data.User;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.Map;


public abstract class EditBaseActivity extends AppCompatActivity
        implements View.OnClickListener {

    private Context mContext;
    protected TextView tx_back;
    protected TextView tx_title;
    protected TextView txt_complete;

    protected String fieldData;
    protected String title;
    protected String fieldContent;
    protected Handler mHandler;
    protected ProgressDialog dialog;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backup_btn) {
            finish();
        } else if (v.getId() == R.id.txt_complete) {
            dialog.show();
            GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        final Map<String, String> p = getParams();
                        String url = CommonUtil.buildGetUrl(
                                PropertyService.getInstance().getKey("serverUrl"),
                                "/user/edit", p);
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
                                            Integer status = data.getStatus();
                                            if (status == null || status == -1) {
                                                throw new Exception(data.getError());
                                            }
                                            updateUser(p);
                                        } catch (Exception e) {
                                            message.what = MessageUtil.EXCUTE_EXCEPTION;
                                            message.obj = e;
                                        }
                                        mHandler.sendMessage(message);
                                    }
                                });
                    }catch (Exception e){
                        Message message = Message.obtain();
                        message.what = MessageUtil.EXCUTE_EXCEPTION;
                        message.obj = e;
                        mHandler.sendMessage(message);
                    }
                }
            });
        }
    }

    protected void baseBind(Context context) {
        this.mContext = context;
        tx_back = findViewById(R.id.backup_btn);
        tx_title = findViewById(R.id.txt_title);
        txt_complete = findViewById(R.id.txt_complete);
        tx_back.setOnClickListener(this);
        txt_complete.setOnClickListener(this);
        txt_complete.setEnabled(false);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("datas");
        if (bundle != null) {
            fieldData = bundle.getString("field_data");
            fieldContent = bundle.getString("content");
            title = bundle.getString("title");
            tx_title.setText(title);
        }

        dialog = new ProgressDialog(mContext);
        dialog.setMessage("正在提交...");
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    Intent intents = new Intent(ReceiverMessage.EDIT_COMPLETED);
                    intents.putExtra("resource", "modify");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intents);
                    dialog.cancel();
                    finish();
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    dialog.cancel();
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    private void updateUser(Map<String, String> p){
        Map<String, Object> user = GlobalDataHelper.getUser(mContext);
        user.put(p.get("field_data"), p.get("content"));
        //存数据
        AppSqlHelper sqlHeper = new AppSqlHelper(mContext);
        ContentValues values = new ContentValues();
        values.put(p.get("field_data"), p.get("content"));
        values.put("phone", (String)user.get("phone"));
        sqlHeper.upsert("users", values, "phone");
        GlobalDataHelper.updateUser(mContext);
    }

    public abstract Map<String, String> getParams();

}
