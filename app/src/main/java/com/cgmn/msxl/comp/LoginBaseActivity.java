package com.cgmn.msxl.comp;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.data.User;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.Map;


public class LoginBaseActivity extends AppCompatActivity
        implements TextWatcher, View.OnClickListener, View.OnFocusChangeListener {

    protected String getSourceString(int sourceId) {
        return getResources().getString(sourceId);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    public void onLoginRequest(Map<String, String> values,
                               final Context mContext, final Handler mHandler) {
        String url = CommonUtil.buildGetUrl(
                PropertyService.getInstance().getKey("serverUrl"),
                "/user/login", values);
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
                            message.obj = data;
                            Integer status = data.getStatus();
                            if (status == null || status == -1) {
                                throw new RuntimeException(data.getError());
                            } else {
                                saveUserToDb(data.getUser(), mContext);
                            }
                        } catch (Exception e) {
                            message.what = MessageUtil.EXCUTE_EXCEPTION;
                            message.obj = e;
                        }
                        mHandler.sendMessage(message);
                    }
                });
    }

    public void saveUserToDb(User user, Context context) {
        //存数据
        AppSqlHelper sqlHeper = new AppSqlHelper(context);
        String token = user.getToken();
        ContentValues values = new ContentValues();
        values.put("phone", user.getPhone());
        values.put("user_name", user.getUserName());
        values.put("password", user.getPassword());
        values.put("token", token);
        values.put("gender", user.getGender());
        values.put("signature", user.getSignature());
        values.put("last_active", 1);
        sqlHeper.upsert("users", values, "phone");
        sqlHeper.delete("users", String.format("phone <>'%s'", user.getPhone()));
        GlobalDataHelper.updateUser(context);
    }
}
