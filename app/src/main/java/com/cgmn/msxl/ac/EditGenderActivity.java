package com.cgmn.msxl.ac;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.EditBaseActivity;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class EditGenderActivity extends EditBaseActivity {
    private static final String TAG = EditGenderActivity.class.getSimpleName();
    private Context mContext;
    private ImageView img_man, img_weman;
    private RelativeLayout first_relative, last_relative;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_ugender_layout);
        mContext = this;
        baseBind(mContext);
        bindView();
    }

    private void bindView() {
        img_man = findViewById(R.id.img_man);
        img_weman = findViewById(R.id.img_weman);
        first_relative = findViewById(R.id.first_relative);
        last_relative = findViewById(R.id.last_relative);
        first_relative.setOnClickListener(this);
        last_relative.setOnClickListener(this);
        txt_complete.setVisibility(View.GONE);

        if("0".equals(fieldContent)){
            img_man.setImageResource(R.drawable.check);
        }else if("1".equals(fieldContent)){
            img_weman.setImageResource(R.drawable.check);
        }
    }

    private void saveData(final String type){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> p = getParams();
                p.put("content", type);
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
                                    Map<String, Object> user = GlobalDataHelper.getUser(mContext);
                                    user.put(p.get("field_data"), p.get("content"));
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
    public void onClick(View v) {
        if (v.getId() == R.id.backup_btn) {
            finish();
        } else if (v.getId() == R.id.first_relative) {
            dialog.show();
            saveData("0");
        }else if(v.getId() == R.id.last_relative){
            dialog.show();
            saveData("1");
        }
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        Map<String, Object> user = GlobalDataHelper.getUser(mContext);
        params.put("phone", (String) user.get("phone"));
        params.put("field_data", fieldData);
        params.put("token", GlobalDataHelper.getToken(mContext));
        return params;
    }

    @Override
    public void finish() {
        dialog.cancel();
        dialog.dismiss();
        super.finish();
    }

}