package com.cgmn.msxl.service;

import android.content.Context;
import android.os.Environment;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.HashMap;
import java.util.Map;


public class GlobalDataHelper {

    public static String getToken(Context context){
        String token = AppApplication.getInstance().getToken();
        if(CommonUtil.isEmpty(token)){
            Map<String, Object> userMap = getUser(context);
            if(!CommonUtil.isEmpty(userMap)){
                token = (String) userMap.get("token");
            }
        }

        return token;
    }

    public static void updateUser(Context context){
        AppSqlHelper dbHelper = new AppSqlHelper(context);
        Map<String, Object> user = dbHelper.getActiveUser();
        AppApplication.getInstance().setUser(user);
    }

    public static Map<String, Object> getUser(Context context){
        Map<String, Object> user = AppApplication.getInstance().getUser();
        if(CommonUtil.isEmpty(user)){
            AppSqlHelper dbHelper = new AppSqlHelper(context);
            user = dbHelper.getActiveUser();
            if(!CommonUtil.isEmpty(user)){
                AppApplication.getInstance().setUser(user);
            }
        }

        return user;
    }

    public static String getCachePath(){
        String storage = Environment.getExternalStorageDirectory().getPath();
        return storage + "/icon";
    }

    public static String getUserAcc(Context context){
        Map<String, Object> u = getUser(context);
        return (String) u.get("phone");
    }

    public static String getUserPortraitUrl(Context context){
        Map<String, String> p = new HashMap<>();
        p.put("token", getToken(context));
        return CommonUtil.buildGetUrl(
                PropertyService.getInstance().getKey("serverUrl"),
                "/user/portrait_view", p);
    }
}
