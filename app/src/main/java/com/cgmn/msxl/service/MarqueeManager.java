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
    LinkedList<CatchItem> totalList = new LinkedList<>();
    LinkedList<CatchItem> nodes = new LinkedList<>();
    Timer mTimer;
    TimerTask mTimerTask;

    public MarqueeManager(Context context, MyMarqueeView view) {
        mContext = context;
        marqueeview = view;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void startMarquee() {
        final Map<String, String> params = new HashMap<>();
        params.put("token", GlobalDataHelper.getToken(mContext));
        marqueeview.setCompleteListener(new MyMarqueeView.RotationListener() {
            @Override
            public void completeDisplay() {
                marqueeview.clearAnimation();
                marqueeview.stopFilp();
            }
            @Override
            public void oneDisplayed(Integer pos) {
                totalList.remove(nodes.get(pos));
            }
        });


        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                reloadData(params);
            }
        };
        //2s后开始执行，间隔为3s
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 2000, 4000);

    }


    private void reloadData(final Map<String, String> params) {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Object obj = GlobalDataHelper.getData("MARQUEE_LAST_NUM");
                if (obj != null) {
                    params.put("LAST_NUMBER", obj.toString());
                }
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
                                if (!CommonUtil.isEmpty(data.getMarqueeList())) {
                                    totalList.addAll(0, data.getMarqueeList());
                                    CatchItem item = data.getMarqueeList().get(0);
                                    GlobalDataHelper.setData("MARQUEE_LAST_NUM", item.getRecordId());
                                    nodes.clear();
                                    nodes.addAll(totalList);
                                    marqueeview.clearAnimation();
                                    marqueeview.stopFilp();
                                    MarqueeAdapter marqueeAdapter = new MarqueeAdapter(mContext, nodes);
                                    marqueeview.setAdapter(marqueeAdapter);
                                    marqueeview.startFlip();
                                }
                            }
                        });
            }
        });
    }

    public void tiemrCancel() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
    }
}
