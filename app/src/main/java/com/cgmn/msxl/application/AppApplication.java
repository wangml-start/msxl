package com.cgmn.msxl.application;

import android.app.Application;
import com.cgmn.msxl.data.User;

import java.util.HashMap;
import java.util.Map;

public class AppApplication extends Application {

    public static final String TAG = AppApplication.class.getSimpleName();

    private static AppApplication mInstance;

    private Map<String, Object> user;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
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
