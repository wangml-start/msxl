package com.cgmn.msxl.service;

import android.os.Handler;
import android.util.Log;
import com.cgmn.msxl.bean.TradeStatus;
import com.cgmn.msxl.in.AutoNextListener;

import java.util.Timer;
import java.util.TimerTask;

public class TradeAutoRunManager {
    private TradeStatus autoRunStatus;
    Timer mTimer;
    TimerTask mTimerTask;
    private Integer total = 2;
    private Integer counter=0;

    private Handler mHandler;

    private AutoNextListener listener;

    public TradeAutoRunManager(){
        autoRunStatus = TradeStatus.ready;
        this.mHandler = new Handler();
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public void startManager() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i("A", "Auto Trade Running");
                if(autoRunStatus == TradeStatus.running){
                    if(counter < total){
                        if(listener != null){
                            final Integer dis = total-counter;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onTicket(dis);
                                }
                            });
                        }
                    }else {
                        autoRunStatus = TradeStatus.arrived;
                        if(listener != null){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onComplete();
                                }
                            });
                        }
                    }
                    counter++;
                }
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 0, 1000);

    }

    public TradeStatus getAutoRunStatus() {
        return autoRunStatus;
    }

    public void pause(){
        autoRunStatus = TradeStatus.suspended;
    }

    public void resumeManager(){
        autoRunStatus = TradeStatus.running;
    }

    public void resetManager(){
        autoRunStatus = TradeStatus.running;
        counter = 0;
    }


    public void setListener(AutoNextListener listener) {
        this.listener = listener;
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
