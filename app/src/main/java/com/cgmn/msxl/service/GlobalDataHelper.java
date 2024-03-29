package com.cgmn.msxl.service;

import android.content.Context;
import android.os.Environment;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import org.apache.shiro.codec.Base64;

import java.util.HashMap;
import java.util.Map;


public class GlobalDataHelper {
    private static Map<String, Object> pageTranfer = new HashMap<>();

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

    public static String getPortraitCachePath(){
        String storage = Environment.getExternalStorageDirectory().getPath();
        return storage + "/portrait";
    }

    public static String getSaveApkPath(){
        String storage = Environment.getExternalStorageDirectory().getPath();
        return storage + "/apk";
    }

    public static String getUserAcc(Context context){
        Map<String, Object> u = getUser(context);
        return (String) u.get("phone");
    }

    public static Integer getUserId(Context context){
        Map<String, Object> u = getUser(context);
        return Integer.valueOf((String) u.get("user_id"));
    }

    public static byte[] getUserCut(Context context){
        Map<String, Object> u = getUser(context);
        String content = (String) u.get("image_cut");
        if(CommonUtil.isEmpty(content)){
            return null;
        }else{
            return Base64.decode(content);
        }
    }

    public static String getUserName(Context context){
        Map<String, Object> u = getUser(context);
        return (String) u.get("user_name");
    }

    public static String getUserSignature(Context context){
        Map<String, Object> u = getUser(context);
        return (String) u.get("signature");
    }

    public static String getUserGender(Context context){
        Map<String, Object> u = getUser(context);
        return (String) u.get("gender");
    }


    public static String getUserPortraitUrl(Context context){
        Map<String, String> p = new HashMap<>();
        p.put("token", getToken(context));
        return CommonUtil.buildGetUrl(
                ConstantHelper.serverUrl,
                "/user/portrait_view", p);
    }

    public static void setData(String key, Object obj){
        pageTranfer.put(key, obj);
        Integer a = 1;
    }

    public static Object getData(String key){
        Object obj = pageTranfer.get(key);
//        pageTranfer.remove(key);
        return obj;
    }

    public static void removeKey(String key){
        pageTranfer.remove(key);
    }

    public static String getDownloadVersionUrl(Context context){
        return String.format("%s/%s?token=%s",ConstantHelper.serverUrl,
                "common/download_version",GlobalDataHelper.getToken(context));
    }

}
