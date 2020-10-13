package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.MutiLayoutAdapter;
import com.cgmn.msxl.comp.adpter.SettingAdpter;
import com.cgmn.msxl.data.*;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.List;
import java.util.Map;

public class ModeSettingActivity extends AppCompatActivity {
    private static final String TAG = ModeSettingActivity.class.getSimpleName();
    private ListView list_content;
    private Context mContext;
    private SettingAdpter myAdapter = null;

    //消息处理
    private Handler mHandler;
    private List<SettingItem> mData=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_setting_layout);
        initMessageHandle();
        bindView();
        initAdpter();
    }


    private void initAdpter(){
        //加载用户信息
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                mData = ModeList.getInstance().getList();
                AppSqlHelper sqlHeper = new AppSqlHelper(mContext);
                Map<String, Object> map = GlobalDataHelper.getUser(mContext);
                Map<String, String> hash = sqlHeper.getUserModelSettings((String) map.get("id"));
                if(!CommonUtil.isEmpty(hash)){
                    for(SettingItem item : mData){
                        String type = item.getModedType()+"";
                        if(hash.containsKey(type)){
                            item.setState(Integer.valueOf(hash.get(type)));
                        }
                    }
                }
                Message message = Message.obtain();
                message.what = MessageUtil.REQUEST_SUCCESS;
                mHandler.sendMessage(message);
            }
        });
    }

    @SuppressLint("WrongViewCast")
    private void bindView() {
        mContext = this;
        list_content = findViewById(R.id.list_content);
    }

    private void initMessageHandle(){
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what == MessageUtil.REQUEST_SUCCESS){
                    myAdapter = new SettingAdpter(mContext, mData);
                    list_content.setAdapter(myAdapter);
                }else if(msg.what == MessageUtil.EXCUTE_EXCEPTION){
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);

                }
                return false;
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
    }
}
