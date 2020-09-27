package com.cgmn.msxl.ac;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.comp.LoginBaseActivity;
import com.cgmn.msxl.comp.showPassworCheckBox;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MyPatternUtil;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ForgetPasswordActivity extends LoginBaseActivity {
    private EditText tx_new_pwd;
    private EditText tx_email;
    private EditText tx_valid_code;

    private Button bt_login;
    private Button bt_sent_email;
    private Button backup_btn;
    private showPassworCheckBox ck_show;

    private Context mContext;

    private MyTimeCount time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_pws_layout);
        bindView();
    }

    @Override
    public void onClick(View v) {
        Map<String, String> p = new HashMap<>();
        if (v.getId() == R.id.bt_login) {
            p.put("email", tx_email.getText().toString());
            p.put("pws", tx_new_pwd.getText().toString());
            p.put("FORGET_LOGIN", "1");
            p.put("code", tx_valid_code.getText().toString());
            onLogin(p);
        } else if (v.getId() == R.id.bt_send_mail) {
            String em = tx_email.getText().toString();
            if (MyPatternUtil.validEmail(em)) {
                time.start();
                p.put("email", tx_email.getText().toString());
                sendValidCodeMessage(p);
            } else {
                StringBuffer tipes = new StringBuffer();
                tipes.append(getSourceString(R.string.sign_email));
                tipes.append(getSourceString(R.string.valid_fails));
                tipes.append("\n");
                Toast.makeText(mContext, tipes.toString(), Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.backup_btn) {
            finish();
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
        if(v.getId() == R.id.tx_email){
            String email = tx_email.getText().toString();
            if (CommonUtil.isEmpty(email) || !MyPatternUtil.validEmail(email)) {
                StringBuffer tipes = new StringBuffer();
                tipes.append(getSourceString(R.string.sign_email));
                tipes.append(getSourceString(R.string.valid_fails));
                Toast.makeText(mContext, tipes.toString(), Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId() == R.id.tx_new_user_wd){
            String ws = tx_new_pwd.getText().toString();
            if (CommonUtil.isEmpty(ws) || ws.length() < 8) {
                StringBuffer tipes = new StringBuffer();
                tipes.append(getSourceString(R.string.new_user_wd));
                tipes.append(getSourceString(R.string.valid_fails));
                Toast.makeText(mContext, tipes.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        validForm();
    }

    private void validForm() {
        String em = tx_email.getText().toString();
        String pws = tx_new_pwd.getText().toString();
        String code = tx_valid_code.getText().toString();
        if (MyPatternUtil.validEmail(em) && pws.length() >= 8 && code.length() == 6) {
            bt_login.setEnabled(true);
        } else {
            bt_login.setEnabled(false);
        }
    }


    private void bindView() {
        mContext = this;
        tx_new_pwd = findViewById(R.id.tx_new_user_wd);
        tx_email = findViewById(R.id.tx_email);
        tx_valid_code = findViewById(R.id.tx_valid_code);
        bt_login = findViewById(R.id.bt_login);
        bt_sent_email = findViewById(R.id.bt_send_mail);
        backup_btn = findViewById(R.id.backup_btn);
        time = new MyTimeCount(60000, 1000);

        ck_show = findViewById(R.id.ck_dis_pws);

        bt_login.setOnClickListener(this);
        bt_sent_email.setOnClickListener(this);
        backup_btn.setOnClickListener(this);

        tx_email.setOnFocusChangeListener(this);
        tx_new_pwd.setOnFocusChangeListener(this);
        tx_valid_code.addTextChangedListener(this);

        ck_show.setPws(tx_new_pwd);
        ck_show.setOnCheckedChangeListener(ck_show);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("datas");
        String email = bundle.getString("email");
        tx_email.setText(email);
    }

    private void sendValidCodeMessage(Map<String, String> p) {
        String url = CommonUtil.buildGetUrl(
                PropertyService.getInstance().getKey("serverUrl"),
                "/user/valid_code", p);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Gson gson = new Gson();
                        BaseData data = gson.fromJson(s, BaseData.class);
                        Integer status = data.getStatus();
                        if (status == null || status == -1) {
                            Toast.makeText(mContext, data.getError(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(mContext, "服务器异常！", Toast.LENGTH_SHORT).show();
                    }
                });

        AppApplication.getInstance().addToRequestQueue(request, "Request Send Valid Code:");
    }

    private void onLogin(Map<String, String> params){
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("登录中...");
        pDialog.show();
        String url = CommonUtil.buildGetUrl(
                PropertyService.getInstance().getKey("serverUrl"),
                "/user/login", params);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        pDialog.hide();
                        Gson gson = new Gson();
                        BaseData data = gson.fromJson(s, BaseData.class);
                        Integer status = data.getStatus();
                        if(status == null || status == -1){
                            Toast.makeText(mContext, data.getError(), Toast.LENGTH_SHORT).show();
                        }else{
                            afterLoginSuccess(data.getUser(), mContext);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        pDialog.hide();
                        Toast.makeText(mContext, "登陆失败，服务器异常！", Toast.LENGTH_SHORT).show();
                    }
                });

        AppApplication.getInstance().addToRequestQueue(request, "login");
    }


    class MyTimeCount extends CountDownTimer {
        public MyTimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            bt_sent_email.setEnabled(false);
            String text = getSourceString(R.string.send_email);
            bt_sent_email.setText(String.format("%s(%s)", text, millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            bt_sent_email.setEnabled(true);
            String text = getSourceString(R.string.send_email);
            bt_sent_email.setText(text);
        }
    }
}
