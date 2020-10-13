package com.cgmn.msxl.service;

import android.content.Context;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.utils.CommonUtil;

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
}
