package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.LoginBaseActivity;
import com.cgmn.msxl.comp.showPassworCheckBox;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MyPatternUtil;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class loginActivity extends LoginBaseActivity {
    private EditText tx_pwd;
    private EditText tx_email;

    private Button bt_login;
    private Button bt_forget_pws;
    private showPassworCheckBox ck_show;

    private Context mContext;

    private static final String TAG = "loginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        bindView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_login) {
            String em = tx_email.getText().toString();
            String pws = tx_pwd.getText().toString();
            Map<String, String> p = new HashMap<>();
            p.put("email", em);
            p.put("pws", pws);
            p.put("GENERAL_LOGIN", "1");
            onLoginRequest(p);
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


    private void onLoginRequest(Map<String, String> values) {
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("登录中...");
        pDialog.show();
        String url = CommonUtil.buildGetUrl(
                PropertyService.getInstance().getKey("serverUrl"),
                "/user/login", values);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        pDialog.hide();
                        try{
                            Gson gson = new Gson();
                            BaseData data = gson.fromJson(s, BaseData.class);
                            Integer status = data.getStatus();
                            if(status == null || status == -1){
                                Toast.makeText(mContext, data.getError(), Toast.LENGTH_SHORT).show();
                            }else{
                                afterLoginSuccess(data.getUser(), mContext);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
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
}
