package com.cgmn.msxl.service;

import android.content.Context;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.utils.CommonUtil;


public class TokenHelper {

    public static String getToken(Context context){
        String token = AppApplication.getInstance().getToken();
        if(CommonUtil.isEmpty(token)){
            AppSqlHelper dbHelper = new AppSqlHelper(context);
            token = dbHelper.getToken();
            if(!CommonUtil.isEmpty(token)){
                AppApplication.getInstance().setToken(token);
            }
        }

        return token;
    }
}
