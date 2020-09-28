package com.cgmn.msxl.application;

import android.app.Application;

public class AppApplication extends Application {

    public static final String TAG = AppApplication.class.getSimpleName();

    private static AppApplication mInstance;

    private String token;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

//        GlobalExceptionHandler mException = GlobalExceptionHandler.getInstance();
//        mException.init(getApplicationContext());  //注册
    }

    public static synchronized AppApplication getInstance() {
        return mInstance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
