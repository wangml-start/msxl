package com.cgmn.msxl.ac;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
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
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.LoginBaseActivity;
import com.cgmn.msxl.comp.showPassworCheckBox;
import com.cgmn.msxl.data.User;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.AESUtil;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.cgmn.msxl.utils.MyPatternUtil;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ForgetPasswordActivity extends LoginBaseActivity {
    private static final String TAG = ForgetPasswordActivity.class.getSimpleName();
    private EditText tx_new_pwd;
    private EditText tx_email;
    private EditText tx_valid_code;

    private Button bt_login;
    private Button bt_sent_email;
    private Button backup_btn;
    private showPassworCheckBox ck_show;

    private Context mContext;

    private MyTimeCount time;
    //消息处理
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_pws_layout);
        initMessageHandle();
        bindView();
    }

    @Override
    public void onClick(View v) {
        final Map<String, String> p = new HashMap<>();
        if (v.getId() == R.id.bt_login) {
            p.put("email", tx_email.getText().toString());
            String sercurety = AESUtil.encrypt(tx_new_pwd.getText().toString(), MessageUtil.SERCURETY);
            p.put("pws", sercurety);
            p.put("FORGET_LOGIN", "1");
            p.put("code", tx_valid_code.getText().toString());
            CustmerToast.makeText(mContext, R.string.logining, CustmerToast.LENGTH_LONG).show();
            GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
                @Override
                public void run() {
                    onLoginRequest(p, mContext, mHandler);
                    Log.e(TAG,"NAME="+Thread.currentThread().getName());
                }
            });
        } else if (v.getId() == R.id.bt_send_mail) {
            String em = tx_email.getText().toString();
            if (MyPatternUtil.validEmail(em)) {
                time.start();
                p.put("email", tx_email.getText().toString());
                GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
                    @Override
                    public void run() {
                        sendValidCodeMessage(p);
                        Log.e(TAG,"NAME="+Thread.currentThread().getName());
                    }
                });
            } else {
                StringBuffer tipes = new StringBuffer();
                tipes.append(getSourceString(R.string.sign_email));
                tipes.append(getSourceString(R.string.valid_fails));
                tipes.append("\n");
                CustmerToast.makeText(mContext, tipes.toString()).show();
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
                CustmerToast.makeText(mContext, tipes.toString()).show();
            }
        }else if(v.getId() == R.id.tx_new_user_wd){
            String ws = tx_new_pwd.getText().toString();
            if (CommonUtil.isEmpty(ws) || ws.length() < 8) {
                StringBuffer tipes = new StringBuffer();
                tipes.append(getSourceString(R.string.new_user_wd));
                tipes.append(getSourceString(R.string.valid_fails));
                CustmerToast.makeText(mContext, tipes.toString()).show();
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

    private void initMessageHandle(){
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what == MessageUtil.REQUEST_SUCCESS){
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
                    Exception exception = (Exception) msg.obj;
                    StringBuffer mes = new StringBuffer("服务器异常！");
                    mes.append("\n");
                    mes.append(exception.getMessage());
                    //TODO: 异常处理
                    CustmerToast.makeText(mContext, mes.toString()).show();
                }
                return false;
            }
        });
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
                            Message message = Message.obtain();
                            message.what = MessageUtil.EXCUTE_EXCEPTION;
                            message.obj = new Exception(data.getError());
                            mHandler.sendMessage(message);
                        }
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

        AppApplication.getInstance().addToRequestQueue(request, "Request Send Valid Code:");
    }

    @Override
    public void finish() {
        super.finish();
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
