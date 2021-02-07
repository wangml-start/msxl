package com.cgmn.msxl.service;

import android.content.Context;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.MarqueeAdapter;
import com.cgmn.msxl.comp.view.MyMarqueeView;
import com.cgmn.msxl.data.CatchItem;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.*;

public class MarqueeManager {
    private String action = "/common/query_marquee_datas";
    private Context mContext;
    MyMarqueeView marqueeview;
    List<CatchItem> nodes = null;

    public MarqueeManager(Context context, MyMarqueeView view) {
        mContext = context;
        marqueeview = view;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void startMarquee(){
        final Map<String, String> params = new HashMap<>();
        params.put("token", GlobalDataHelper.getToken(mContext));
        Object obj = GlobalDataHelper.getData("MARQUEE_LAST_NUM");
        if(obj != null){
            params.put("LAST_NUMBER", obj.toString());
        }
        marqueeview.setCompleteListener(new MyMarqueeView.RotationListener() {
            @Override
            public void completeDisplay() {
                if(nodes != null && nodes.size() > 0){
                    CatchItem item = nodes.get(nodes.size()-1);
                    params.put("LAST_NUMBER",item.getRecordId()+"");
                    GlobalDataHelper.setData("MARQUEE_LAST_NUM", item.getRecordId());
                    reloadData(params);
                }else{
                    marqueeview.clearAnimation();
                    marqueeview.stopFilp();
                }
            }
        });


        Timer mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if(CommonUtil.isEmpty(nodes)){
                    reloadData(params);
                }
            }
        };
        //2s后开始执行，间隔为6s
        mTimer.schedule(mTimerTask, 2000,6000);

    }


    private void reloadData(final Map<String, String> params){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        action, params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                            }
                            @Override
                            public void onResponse(BaseData data) {
                                nodes = data.getMarqueeList();
                                if(nodes != null && nodes.size() > 0){
                                    MarqueeAdapter marqueeAdapter = new MarqueeAdapter(mContext, nodes);
                                    marqueeview.setAdapter(marqueeAdapter);
                                }
                            }
                        });
            }
        });
    }


}
