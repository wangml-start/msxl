package com.cgmn.msxl.service;

import android.content.Context;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.db.DBHelper;
import com.cgmn.msxl.utils.CommonUtil;


public class TokenHelper {

    public static String getToken(Context context){
        String token = AppApplication.getInstance().getToken();
        if(CommonUtil.isEmpty(token)){
            DBHelper dbHelper = new DBHelper(context);
            token = dbHelper.getToken();
            if(!CommonUtil.isEmpty(token)){
                AppApplication.getInstance().setToken(token);
            }
        }

        return token;
    }
}
