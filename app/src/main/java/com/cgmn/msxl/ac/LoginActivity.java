package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.bean.PopuBean;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.LoginBaseActivity;
import com.cgmn.msxl.comp.view.ClearEditTextView;
import com.cgmn.msxl.comp.view.PopuWindowView;
import com.cgmn.msxl.comp.view.showPassworCheckBox;
import com.cgmn.msxl.data.User;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.TdataListener;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends LoginBaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private ClearEditTextView tx_pwd;
    private ClearEditTextView tx_email;

    private Button bt_login;
    private Button bt_forget_pws;
    private showPassworCheckBox ck_show;
    private ImageView acc_down_list;

    private List<Map<String, Object>> accList;

    private Context mContext;

    //消息处理
    private Handler mHandler;


    @Override
    protected void init(){
        initMessageHandle();
        bindView();
        loadAccountList();
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
            boolean isRegist = true;
            if(!CommonUtil.isEmpty(accList)){
                String em = tx_email.getText().toString().trim();
                for(Map<String, Object> item : accList){
                    if(item.get("phone").toString().equals(em)){
                        isRegist = false;
                        break;
                    }
                }
            }
            if(isRegist){ //首次注册
                validEmail();
            }else{
                onLoginClick();
            }
        } else if (v.getId() == R.id.bt_forget) {
            Intent intent = new Intent(this, ForgetPasswordActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("email", tx_email.getText().toString());
            intent.putExtra("datas", bundle);
            startActivity(intent);
        }else if(v.getId() == R.id.acc_down_list){
            showAccountList();
        }
    }

    public void validEmail(){
        String em = tx_email.getText().toString();
        String url = "https://api.nbhao.org/v1/email/verify";
        OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                new OkHttpClientManager.Param("email", em.trim())
        };
        OkHttpClientManager.postAsyn(url,
                new OkHttpClientManager.ResultCallback<Map<String, Object>>() {
                    @Override
                    public void onError(com.squareup.okhttp.Request request, Exception e) {
                        Message message = Message.obtain();
                        message.what = MessageUtil.MAIL_VALID_REQUEST_404;
                        message.obj = e;
                        mHandler.sendMessage(message);
                    }
                    @Override
                    public void onResponse(Map<String, Object> data) {
                        Message message = Message.obtain();
                        message.what = MessageUtil.MAIL_VALID_REQUEST_200;
                        message.obj = data;
                        mHandler.sendMessage(message);
                    }
                }, params);
    }

    private void onLoginClick(){
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
    }

    private void showAccountList(){
        PopuWindowView popuWindowView = new PopuWindowView(mContext, 1000);
        popuWindowView.setMaxLines(4);
        popuWindowView.initPupoData(new TdataListener() {
            @Override
            public void initPupoData(List<PopuBean> lists) {
                if(accList != null){
                    for (int i = 0; i < accList.size(); i++) {
                        PopuBean popu = new PopuBean();
                        popu.setTitle((String) accList.get(i).get("phone"));
                        popu.setValue((String) accList.get(i).get("password"));
                        lists.add(popu);
                    }
                }
            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position) {
                tx_email.setText((String) accList.get(position).get("phone"));
                String ps = AESUtil.decrypt((String) accList.get(position).get("password"), MessageUtil.SERCURETY);
                tx_pwd.setText(ps);
            }
        });
        popuWindowView.showing(tx_email);
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

        acc_down_list = findViewById(R.id.acc_down_list);
        acc_down_list.setOnClickListener(this);


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
                }else if(msg.what == MessageUtil.MAIL_VALID_REQUEST_404){
                    onLoginClick();
                }else if(msg.what == MessageUtil.MAIL_VALID_REQUEST_200){
                    Map<String, Object> map = (Map<String, Object>) msg.obj;
                    if(!CommonUtil.isEmpty(map) && "true".equals(map.get("result"))){
                        onLoginClick();
                    }else{
                        CustmerToast.makeText(mContext, "该邮箱不存在").show();
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

    private void loadAccountList(){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                AppSqlHelper dbHelper = new AppSqlHelper(mContext);
                accList = dbHelper.getAccountList();
                if(!CommonUtil.isEmpty(accList)){
                    Message message = Message.obtain();
                    message.what = MessageUtil.LOAD_USER_INFOR;
                    message.obj = accList.get(0);
                    mHandler.sendMessage(message);
                }
            }
        });
    }
}
