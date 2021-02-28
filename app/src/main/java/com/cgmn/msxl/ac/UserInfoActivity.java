package com.cgmn.msxl.ac;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.view.CenterImageSpan;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.UserDetailInfo;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends BaseOtherActivity {
    private static final String TAG = UserInfoActivity.class.getSimpleName();

    private Handler mHandler;
    private NetImageView icon_head;
    private TextView txt_user_name, txt_account,txt_user_type,txt_valid_at;
    private TextView txt_total_cash, txt_pl,txt_train_times,txt_win_rate;
    private TextView txt_dan, txt_b_day,txt_signature,btn_update;

    @Override
    protected int getContentView() {
        return R.layout.user_detail_info_layout;
    }

    @Override
    protected String setTitle(){
        return getString(R.string.acct_info);
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
                String action = "/common/query_user_info";
                Map<String, String> params = new HashMap<>();
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
                                    message.obj = data.getUserDetailInfo();
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
        icon_head = findViewById(R.id.icon_head);
        txt_user_name = findViewById(R.id.txt_user_name);
        txt_account = findViewById(R.id.txt_account);
        txt_user_type = findViewById(R.id.txt_user_type);
        txt_valid_at = findViewById(R.id.txt_valid_at);
        txt_total_cash = findViewById(R.id.txt_total_cash);
        txt_pl = findViewById(R.id.txt_pl);
        txt_train_times = findViewById(R.id.txt_train_times);
        txt_win_rate = findViewById(R.id.txt_win_rate);
        txt_dan = findViewById(R.id.txt_dan);
        txt_b_day = findViewById(R.id.txt_b_day);
        txt_signature = findViewById(R.id.txt_signature);
        btn_update = findViewById(R.id.btn_update);
        txt_complete.setText("编辑");
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(MessageUtil.REQUEST_SUCCESS == msg.what){
                    setValues((UserDetailInfo) msg.obj);
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
        View.OnClickListener ls = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn_update){
                    AppSqlHelper sqlHeper = new AppSqlHelper(mContext);
                    sqlHeper.excuteSql(String.format("UPDATE users SET last_active=2 WHERE phone='%s'", GlobalDataHelper.getUserAcc(mContext)));
                    startActivity(new Intent(mContext, LoginActivity.class));
                }else if(v.getId() == R.id.txt_complete){
                    Intent intent = new Intent(mContext, AccountInfoActivity.class);
                    startActivity(intent);
                }

            }
        };
        btn_update.setOnClickListener(ls);
        txt_complete.setOnClickListener(ls);
    }

    @Override
    protected boolean showRight(){
        return false;
    }
    @Override
    protected boolean showComplate(){
        return true;
    }
    @Override
    protected void onCompletedClick(){

    }

    private void setValues(UserDetailInfo info){
        byte[] cut = GlobalDataHelper.getUserCut(mContext);
        if(cut != null && cut.length > 0){
            icon_head.setImageContent(cut);
        }else{
            icon_head.setImageName(GlobalDataHelper.getUserAcc(mContext));
            icon_head.setImageURL(GlobalDataHelper.getUserPortraitUrl(mContext));
        }
        String uName = GlobalDataHelper.getUserName(mContext);
        String gender = GlobalDataHelper.getUserGender(mContext);
        txt_account.setText("账号: " + GlobalDataHelper.getUserAcc(mContext));
        txt_signature.setText(GlobalDataHelper.getUserSignature(mContext));

        String tempName = "操盘手名: " + uName + " ";
        if(gender != null){
            Drawable d = null;
            if(gender.equals("0")){
                d = mContext.getResources().getDrawable(R.drawable.male);
            }else{
                d = mContext.getResources().getDrawable(R.drawable.female);
            }
            d.setBounds(0, 0, 40, 40);
            CenterImageSpan imageSpan = new CenterImageSpan(d);
            SpannableString span = new SpannableString(tempName);
            span.setSpan(imageSpan, tempName.length()-1, tempName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txt_user_name.setText(span);
        }else{
            txt_user_name.setText(tempName);
        }

        if(info == null){
             return;
        }
        if(!CommonUtil.isEmpty(info.getVipType())){
            txt_user_type.setText(info.getVipType());
        }else{
            txt_user_type.setText("普通会员");
        }
        if(!CommonUtil.isEmpty(info.getVipEnd())){
            txt_valid_at.setText(info.getVipEnd());
        }
        if(!CommonUtil.isEmpty(info.getTotalAmt())){
            txt_total_cash.setText(CommonUtil.formatAmt(info.getTotalAmt()));
        }
        if(!CommonUtil.isEmpty(info.getTotalPl())){
            txt_pl.setText(CommonUtil.formatAmt(info.getTotalPl()));
        }
        if(!CommonUtil.isEmpty(info.getTrainTimes())){
            txt_train_times.setText(info.getTrainTimes());
        }
        if(!CommonUtil.isEmpty(info.getWinRate())){
            txt_win_rate.setText(info.getWinRate());
        }
        if(!CommonUtil.isEmpty(info.getAccDan())){
            txt_dan.setText(info.getAccDan());
        }
        if(!CommonUtil.isEmpty(info.getBirthDay())){
            txt_b_day.setText(info.getBirthDay());
        }
    }
}