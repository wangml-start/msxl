package com.cgmn.msxl.comp.frag;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.ApprovesAdpter;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.RelatedToMe;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
public class ApproveFragment extends RelatedFrgment {
    private ApprovesAdpter adpter;

    public ApproveFragment() {
    }

    public static ApproveFragment newInstance() {
        ApproveFragment fragment = new ApproveFragment();
        return fragment;
    }


    @Override
    protected void bindView(View view) {
        adpter = new ApprovesAdpter(mContext, mData);
        listView.setAdapter(adpter);

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    if(action.equals(REFRESH)){
                        mData.clear();
                        scrollView.stopRefresh();
                        scrollView.setScrollY(0);
                    }else{
                        appendList = false;
                    }
                    if (!CommonUtil.isEmpty(msg.obj)) {
                        mData.addAll((List<RelatedToMe>) msg.obj);
                        adpter.notifyDataSetChanged();
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });

        currentPos = 0;
    }

    private String getUrl(Integer start) {
        String action = "/chat/query_approve_list";
        Map<String, String> params = new HashMap<>();
        params.put("token", GlobalDataHelper.getToken(mContext));
        params.put("start", start + "");
        return CommonUtil.buildGetUrl(
                ConstantHelper.serverUrl,
                action, params);
    }

    @Override
    protected void loadList(final Integer start) {
        //加载用户信息
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClientManager.getAsyn(getUrl(start),
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
                                    message.obj = data.getAboveMe();
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

}