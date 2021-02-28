package com.cgmn.msxl.ac;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.dialog.NumberProgressBar;
import com.azhon.appupdate.listener.OnDownloadListenerAdapter;
import com.azhon.appupdate.manager.DownloadManager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewVersionActivity extends BaseOtherActivity {
    private static final String TAG = NewVersionActivity.class.getSimpleName();

    private Handler mHandler;
    private TextView txt_content, btn_update;
    private NumberProgressBar progressBar;
    private DownloadManager manager;
    @Override
    protected int getContentView() {
        return R.layout.new_version;
    }

    @Override
    protected String setTitle(){
        return getString(R.string.check_new_version);
    }


    @Override
    protected void init(){
        bindView();
        loadInfo();

    }
    private void loadInfo(){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/common/query_new_version";
                Map<String, String> params = new HashMap<>();
                params.put("version", PropertyService.getInstance().getKey("version"));
                params.put("type", "android");
                params.put("token", GlobalDataHelper.getToken(mContext));
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

    private void bindView(){
        txt_content = findViewById(R.id.txt_content);
        btn_update = findViewById(R.id.btn_update);
        progressBar = findViewById(R.id.number_progress_bar);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String cVersion = PropertyService.getInstance().getKey("version");
                if(MessageUtil.REQUEST_SUCCESS == msg.what){
                    StringBuffer message = new StringBuffer();
                    if(msg.obj != null){
                        message.append("当前版本: V");
                        message.append(cVersion);
                        message.append("\n\n");
                        List<String> list = (List<String>) msg.obj;
                        message.append(StringUtils.join(list, "\n\n"));
                        btn_update.setVisibility(View.VISIBLE);
                    }else{
                        message.append("当前版本已是最新版V"+ cVersion);
                        btn_update.setVisibility(View.GONE);
                    }
                    txt_content.setText(message.toString());
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdateClick();
            }
        });

    }

    @Override
    protected boolean showRight(){
        return false;
    }
    @Override
    protected boolean showComplate(){
        return false;
    }

    private void onUpdateClick(){
        progressBar.setVisibility(View.VISIBLE);
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
                .setUsePlatform(true)
                //设置强制更新
                .setForcedUpgrade(false)
                .setOnDownloadListener(listenerAdapter);

        manager = DownloadManager.getInstance(this);
        manager.setApkName("tzwd.apk")
                .setApkUrl(GlobalDataHelper.getDownloadVersionUrl(mContext))
                .setSmallIcon(R.drawable.app_main)
                .setShowNewerToast(true)
                .setConfiguration(configuration)
                .setApkVersionCode(2)
                .setApkDescription(txt_content.getText().toString())
                .download();

    }

    private OnDownloadListenerAdapter listenerAdapter = new OnDownloadListenerAdapter() {
        /**
         * 下载中
         *
         * @param max      总进度
         * @param progress 当前进度
         */
        @Override
        public void downloading(int max, int progress) {
            int curr = (int) (progress / (double) max * 100.0);
            progressBar.setMax(100);
            progressBar.setProgress(curr);
        }
    };

}