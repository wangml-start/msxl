package com.cgmn.msxl.ac;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.os.Bundle;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.LoginBaseActivity;
import com.cgmn.msxl.data.User;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.*;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends LoginBaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Context mContext;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        //home建重启问题
        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }
//        btn = findViewById(R.id.register_btn);
//        btn.setOnClickListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        Integer time = 500;    //设置等待时间，单位为毫秒
        Handler handler = new Handler();
        //当计时结束时，跳转至主界面
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!NetworkUtil.isNetworkConnected(mContext)){
                    CustmerToast.makeText(mContext, R.string.no_network).show();
                }else{
                    autoLogin();
                }
            }
        }, time);

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    private void autoLogin() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.LOAD_USER_INFOR) {
                    Map<String, Object> map = (Map<String, Object>) msg.obj;
                    if (!CommonUtil.isEmpty(map)) {
                        final Map<String, String> p = new HashMap<>();
                        p.put("email", (String) map.get("phone"));
                        p.put("pws", (String) map.get("password"));
                        p.put("GENERAL_LOGIN", "1");
//                        CustmerToast.makeText(mContext, R.string.logining).show();
                        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
                            @Override
                            public void run() {
                                onLoginRequest(p, mContext, mHandler);
                                Log.e(TAG,"NAME="+Thread.currentThread().getName());
                            }
                        });
                    }
                }if(msg.what == MessageUtil.REQUEST_SUCCESS){
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
                    startActivity(new Intent(mContext, LoginActivity.class));
                    finish();
                }else if(msg.what == MessageUtil.NEED_TO_LOGIN_PAGE){
                    startActivity(new Intent(mContext, LoginActivity.class));
                    finish();
                }
                return false;
            }
        });

        //加载用户信息
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> map = GlobalDataHelper.getUser(mContext);
                Message message = Message.obtain();
                if(map == null){
                    message.what = MessageUtil.NEED_TO_LOGIN_PAGE;
                }else{
                    message.what = MessageUtil.LOAD_USER_INFOR;
                }
                message.obj = map;
                mHandler.sendMessage(message);
            }
        });
    }

//
//    @SuppressLint("ResourceType")
//    @Override
//    protected void onResume() {
//        super.onResume();
//        RelativeLayout layout = (RelativeLayout)findViewById(R.id.main_layout);
//        InputStream is ;
//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        opt.inPurgeable = true;
//        opt.inInputShareable = true;
//        opt.inSampleSize = 2;
//        is= getResources().openRawResource(R.drawable.main);
//        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
//        BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
//        layout.setBackgroundDrawable(bd);
//    }

}