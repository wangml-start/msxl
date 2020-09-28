package com.cgmn.msxl.ac;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.FixStringBuffer;
import com.cgmn.msxl.utils.MessageUtil;
import com.google.gson.Gson;
import com.squareup.okhttp.Request;

import java.util.HashMap;
import java.util.Map;

public class AppMainActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = AppMainActivity.class.getSimpleName();
    private Context mContxt;
    //消息处理
    private Handler mHandler;

    Button bt_post;
    Button bt_get;
    TextView display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_layout);
        initMessageHandle();
        bindAppMainView();
    }


    private void bindAppMainView() {
        mContxt = this;
        bt_post = findViewById(R.id.bt_post);
        bt_get = findViewById(R.id.bt_get);
        display = findViewById(R.id.lb_display);
        bt_post.setOnClickListener(this);
        bt_get.setOnClickListener(this);
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    BaseData data = (BaseData) msg.obj;
                    Gson g = new Gson();
                    display.setText(g.toJson(data));
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    Exception exception = (Exception) msg.obj;
                    FixStringBuffer mes = new FixStringBuffer();
                    mes.append("登陆失败: %s", exception.getMessage());
                    //TODO: 异常处理
                    CustmerToast.makeText(mContxt, mes.toString()).show();
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_get) {
            CustmerToast.makeText(mContxt, R.string.logining, CustmerToast.LENGTH_LONG).show();
            GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
                @Override
                public void run() {
                    AppSqlHelper sqlHeper = new AppSqlHelper(mContxt);
                    Map<String, Object> map = sqlHeper.getActiveUser();
                    String email = (String) map.get("phone");
                    String password = (String) map.get("password");
                    Map<String, String> p = new HashMap<>();
                    p.put("pws", password);
                    p.put("email", email);
                    p.put("GENERAL_LOGIN", "1");
                    String url = CommonUtil.buildGetUrl(
                            PropertyService.getInstance().getKey("serverUrl"),
                            "/user/login", p);
                    OkHttpClientManager.getAsyn(url,
                            new OkHttpClientManager.ResultCallback<BaseData>() {
                                @Override
                                public void onError(Request request, Exception e) {
                                    Message message = Message.obtain();
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                    mHandler.sendMessage(message);
                                }
                                @Override
                                public void onResponse(BaseData data) {
                                    Message message = Message.obtain();
                                    message.what = MessageUtil.REQUEST_SUCCESS;
                                    message.obj = data;
                                    mHandler.sendMessage(message);
                                }
                            });
                }
            });
        } else {
            CustmerToast.makeText(mContxt, R.string.logining, CustmerToast.LENGTH_LONG).show();
            GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
                @Override
                public void run() {
                    AppSqlHelper sqlHeper = new AppSqlHelper(mContxt);
                    Map<String, Object> map = sqlHeper.getActiveUser();
                    String email = (String) map.get("phone");
                    String password = (String) map.get("password");
                    OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                            new OkHttpClientManager.Param("pws", password),
                            new OkHttpClientManager.Param("GENERAL_LOGIN", "1"),
                            new OkHttpClientManager.Param("email", email)};
                    String url = String.format("%s%s",
                            PropertyService.getInstance().getKey("serverUrl"), "/user/login");
                    OkHttpClientManager.postAsyn(url,
                            new OkHttpClientManager.ResultCallback<BaseData>() {
                                @Override
                                public void onError(Request request, Exception e) {
                                    Message message = Message.obtain();
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                    mHandler.sendMessage(message);
                                }
                                @Override
                                public void onResponse(BaseData data) {
                                    Message message = Message.obtain();
                                    message.what = MessageUtil.REQUEST_SUCCESS;
                                    message.obj = data;
                                    mHandler.sendMessage(message);
                                }
                            },
                            params);
                }
            });
        }
    }
}
