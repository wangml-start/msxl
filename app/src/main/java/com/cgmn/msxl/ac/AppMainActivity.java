package com.cgmn.msxl.ac;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.manager.DownloadManager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.view.GuideIconView;
import com.cgmn.msxl.comp.frag.MayFragment;
import com.cgmn.msxl.comp.frag.TrainFragment;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.service.TokenInterceptor;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppMainActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = AppMainActivity.class.getSimpleName();
    private Context mContxt;
    //UI Object
    private GuideIconView txt_xunlian, txt_guba, txt_may;
    private FrameLayout ly_content;

    //Fragment Object
    private TrainFragment trainFrag;
    private MayFragment myFrag;
    private FragmentManager fManager;
    private Handler mHandler;

    private DownloadManager manager;

    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_layout);
        mContxt = this;
        bindAppMainView();
        fManager = getFragmentManager();
        txt_xunlian.performClick();
        checkVersion();

        registerTokenListener();
    }


    private void bindAppMainView() {
        mContxt = this;
        txt_guba =  findViewById(R.id.txt_guba);
        txt_xunlian = findViewById(R.id.txt_xunlian);
        txt_may =  findViewById(R.id.txt_may);
        ly_content = (FrameLayout) findViewById(R.id.ly_content);

        txt_xunlian.setOnClickListener(this);
        txt_guba.setOnClickListener(this);
        txt_may.setOnClickListener(this);

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(MessageUtil.REQUEST_SUCCESS == msg.what){
                    List<String> list = (List<String>) msg.obj;
                    if(!CommonUtil.isEmpty(list)){
                        UpdateConfiguration configuration = new UpdateConfiguration()
                                //输出错误日志
                                .setEnableLog(true)
                                //设置自定义的下载
                                //.setHttpManager()
                                //下载完成自动跳动安装页面
                                .setJumpInstallPage(true)
                                //设置对话框背景图片 (图片规范参照demo中的示例图)
                                //.setDialogImage(R.drawable.ic_dialog)
                                //设置按钮的颜色
                                //.setDialogButtonColor(Color.parseColor("#E743DA"))
                                //设置对话框强制更新时进度条和文字的颜色
                                //.setDialogProgressBarColor(Color.parseColor("#E743DA"))
                                //设置按钮的文字颜色
                                .setDialogButtonTextColor(Color.WHITE)
                                //设置是否显示通知栏进度
                                .setShowNotification(true)
                                //设置是否提示后台下载toast
                                .setShowBgdToast(true)
                                //设置是否上报数据
                                .setUsePlatform(false)
                                //设置强制更新
                                .setForcedUpgrade(true);
                                //设置对话框按钮的点击监听
//                                .setButtonClickListener(this)
                                //设置下载过程的监听
//                                .setOnDownloadListener(listenerAdapter);

                        manager = DownloadManager.getInstance(mContxt);
                        String url = GlobalDataHelper.getDownloadVersionUrl(mContxt);
                        manager.setApkName("tzwd.apk")
                                .setApkUrl(url)
                                .setSmallIcon(R.drawable.app_main)
                                .setShowNewerToast(true)
                                .setConfiguration(configuration)
                                .setApkVersionCode(2)
//                                .setApkVersionName("2.1.8")
//                                .setApkSize("4.0")
                                .setApkDescription("当前版本太低,请更新到最新版本!")
                                .download();
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContxt).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    //重置所有文本的选中状态
    private void reset() {
        txt_xunlian.setIconAlpha(0);
        txt_may.setIconAlpha(0);
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (trainFrag != null) fragmentTransaction.hide(trainFrag);
        if (myFrag != null) fragmentTransaction.hide(myFrag);
    }


    @Override
    public void onClick(View v) {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        switch (v.getId()) {
            case R.id.txt_xunlian:
                hideAllFragment(fTransaction);
                reset();
                txt_xunlian.setIconAlpha(1);
                if (trainFrag == null) {
                    trainFrag = new TrainFragment();
                    fTransaction.add(R.id.ly_content, trainFrag);
                } else {
                    fTransaction.show(trainFrag);
                }
                break;
            case R.id.txt_guba:
                Intent intent = new Intent(mContxt, DisgussActivity.class);
                startActivity(intent);
                break;
            case R.id.txt_may:
                hideAllFragment(fTransaction);
                reset();
                txt_may.setIconAlpha(1);
                if (myFrag == null) {
                    myFrag = new MayFragment();
                    fTransaction.add(R.id.ly_content, myFrag);
                } else {
                    fTransaction.show(myFrag);
                }
                break;
        }
        fTransaction.commit();
    }

    private void checkVersion(){
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/common/query_new_version";
                Map<String, String> params = new HashMap<>();
                params.put("version", PropertyService.getInstance().getKey("version"));
                params.put("type", "android");
                params.put("required", "1");
                params.put("token", GlobalDataHelper.getToken(mContxt));
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        action, params);                                                                                                                  OkHttpClientManager.getAsyn(url,
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
                                    message.obj = data.getInfoList();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    }
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

    private void registerTokenListener(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                CustmerToast.makeText(mContxt, bundle.getString("message")).show();
                startActivity(new Intent(mContxt, LoginActivity.class));
                finish();
            }
        };
        broadcastManager = LocalBroadcastManager.getInstance(mContxt);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ReceiverMessage.TOKEN_INVALID); //监听的事件key
        broadcastManager.registerReceiver(receiver, intentFilter);

        OkHttpClientManager.getInstance().addIntercept(new TokenInterceptor(mContxt));
    }

}
