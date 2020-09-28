package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.LoginBaseActivity;
import com.cgmn.msxl.comp.showPassworCheckBox;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.data.User;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.*;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class loginActivity extends LoginBaseActivity {
    private static final String TAG = loginActivity.class.getSimpleName();
    private EditText tx_pwd;
    private EditText tx_email;

    private Button bt_login;
    private Button bt_forget_pws;
    private showPassworCheckBox ck_show;

    private Context mContext;

    //消息处理
    private Handler mHandler;
    ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        initMessageHandle();
        bindView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_login) {
            String em = tx_email.getText().toString();
            String pws = tx_pwd.getText().toString();
            final Map<String, String> p = new HashMap<>();
            p.put("email", em);
            String sercurety = AESUtil.encrypt(pws, MessageUtil.SERCURETY);
            p.put("pws", sercurety);
            p.put("GENERAL_LOGIN", "1");
            pDialog.setMessage("登录中...");
            pDialog.show();
            GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
                @Override
                public void run() {
                    onLoginRequest(p);
                    Log.e(TAG,"NAME="+Thread.currentThread().getName());
                }
            });
        } else if (v.getId() == R.id.bt_forget) {
            Intent intent = new Intent(this, ForgetPasswordActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("email", tx_email.getText().toString());
            intent.putExtra("datas", bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        validForm();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            return;
        }
        String email = tx_email.getText().toString();
        if (CommonUtil.isEmpty(email) || !MyPatternUtil.validEmail(email)) {
            StringBuffer tipes = new StringBuffer();
            tipes.append(getSourceString(R.string.sign_email));
            tipes.append(getSourceString(R.string.valid_fails));
            Toast.makeText(mContext, tipes.toString(), Toast.LENGTH_SHORT).show();
        }
        validForm();
    }

    private void validForm(){
        String email = tx_email.getText().toString();
        String pws = tx_pwd.getText().toString();
        if (MyPatternUtil.validEmail(email) && pws.length() >= 8) {
            bt_login.setEnabled(true);
        } else {
            bt_login.setEnabled(false);
        }
    }


    @SuppressLint("WrongViewCast")
    private void bindView() {
        mContext = this;
        pDialog = new ProgressDialog(mContext);
        tx_pwd = findViewById(R.id.tx_pwd);
        tx_email = findViewById(R.id.tx_email);

        bt_login = findViewById(R.id.bt_login);
        bt_forget_pws = findViewById(R.id.bt_forget);
        ck_show = findViewById(R.id.ck_dis_pws);

        bt_login.setOnClickListener(this);
        bt_forget_pws.setOnClickListener(this);

        tx_email.setOnFocusChangeListener(this);
        tx_pwd.addTextChangedListener(this);

        ck_show.setPws(tx_pwd);
        ck_show.setOnCheckedChangeListener(ck_show);
    }

    private void initMessageHandle(){
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what == MessageUtil.REQUEST_SUCCESS){
                    pDialog.hide();
                    BaseData data = (BaseData) msg.obj;
                    User user = data.getUser();
                    //跳转
                    Intent intent = new Intent(mContext, AppMainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userName", user.getUserName());
                    intent.putExtra("userDate", bundle);
                    startActivity(intent);
                    finish();
                }else if(msg.what == MessageUtil.EXCUTE_EXCEPTION){
                    pDialog.hide();
                    Exception exception = (Exception) msg.obj;
                    FixStringBuffer mes = new FixStringBuffer();
                    mes.append("登陆失败: %s", exception.getMessage());
                    //TODO: 异常处理
                    Toast.makeText(mContext, mes.toString(), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }


    private void onLoginRequest(Map<String, String> values) {
        String url = CommonUtil.buildGetUrl(
                PropertyService.getInstance().getKey("serverUrl"),
                "/user/login", values);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Message message = Message.obtain();
                        message.what = MessageUtil.REQUEST_SUCCESS;
                        try{
                            Gson gson = new Gson();
                            BaseData data = gson.fromJson(s, BaseData.class);
                            message.obj = data;
                            Integer status = data.getStatus();
                            if(status == null || status == -1){
                                throw new Exception(data.getError());
                            }else{
                                saveUserToDb(data.getUser(), mContext);
                            }
                        }catch (Exception e){
                            message.what = MessageUtil.EXCUTE_EXCEPTION;
                            message.obj = e;
                        }
                        mHandler.sendMessage(message);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Message message = Message.obtain();
                        message.what = MessageUtil.EXCUTE_EXCEPTION;
                        message.obj = volleyError;
                        mHandler.sendMessage(message);
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0, 1.0f));
        AppApplication.getInstance().addToRequestQueue(request, "login");
    }
}
