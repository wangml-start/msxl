package com.cgmn.msxl.application;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.util.Map;

public class AppApplication extends Application {

    public static final String TAG = AppApplication.class.getSimpleName();

    private static AppApplication mInstance;

    private Map<String, Object> user;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Looper.loop();
                    } catch (Throwable e) {
                        //e为捕获到的主线程产生的异常
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                //e为捕获到的子线程产生的异常
                e.printStackTrace();
            }
        });
    }

    public static synchronized AppApplication getInstance() {
        return mInstance;
    }

    public String getToken() {
        return user.get("token").toString();
    }

    public Map<String, Object> getUser() {
        return user;
    }

    public void setUser(Map<String, Object> user) {
        this.user = user;
    }
}
