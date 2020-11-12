package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.LoginBaseActivity;
import com.cgmn.msxl.comp.view.showPassworCheckBox;
import com.cgmn.msxl.data.User;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.*;

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


    @Override
    protected void init(){
        initMessageHandle();
        bindView();
    }

    protected String setTitle(){
        return getString(R.string.signin);
    }

    @Override
    protected boolean showBackUp(){
        return false;
    };

    @Override
    protected int getContentView() {
        return R.layout.login_layout;
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
            CustmerToast.makeText(mContext, R.string.logining).show();
            GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
                @Override
                public void run() {
                    onLoginRequest(p, mContext, mHandler);
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
            CustmerToast.makeText(mContext, tipes.toString()).show();
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


        //加载用户信息
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> map = GlobalDataHelper.getUser(mContext);
                Message message = Message.obtain();
                message.what = MessageUtil.LOAD_USER_INFOR;
                message.obj = map;
                mHandler.sendMessage(message);
            }
        });
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
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }else if(msg.what == MessageUtil.LOAD_USER_INFOR){
                    Map<String, Object> map = (Map<String, Object>) msg.obj;
                    if(!CommonUtil.isEmpty(map)){
                        tx_email.setText((String) map.get("phone"));
                        String ps = AESUtil.decrypt((String) map.get("password"), MessageUtil.SERCURETY);
                        tx_pwd.setText(ps);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();//注释掉这行,back键不退出activity
        Log.i(TAG, "onBackPressed");
    }
}
