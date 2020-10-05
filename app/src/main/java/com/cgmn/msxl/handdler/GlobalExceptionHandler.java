package com.cgmn.msxl.handdler;

import android.content.ContentValues;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.squareup.okhttp.Request;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.ConnectException;

public class GlobalExceptionHandler {
    private Context mContxt;
    private static String TAG = GlobalExceptionHandler.class.getSimpleName();
    private static GlobalExceptionHandler handler;

    private GlobalExceptionHandler(){

    }

    public Context getmContxt() {
        return mContxt;
    }

    public void setmContxt(Context mContxt) {
        this.mContxt = mContxt;
    }

    public static GlobalExceptionHandler getInstance(Context c){
        if(handler == null){
            handler = new GlobalExceptionHandler();
        }
        handler.setmContxt(c);
        return handler;
    }

    public void handlerException(final Exception e){
        final String type = "APP_RUNTIMEERROR";
        try{
            if((ConnectException)e instanceof ConnectException){
                CustmerToast.makeText(mContxt, mContxt.getString(R.string.network_loss)).show();
            }
        }catch (Exception e1){
            CustmerToast.makeText(mContxt, e.getMessage()).show();
        }

        String token = AppApplication.getInstance().getToken();
        if(CommonUtil.isEmpty(token)){
            Log.d(TAG, "No Find TOKEN When send exception!");
            return;
        }
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", AppApplication.getInstance().getToken()),
                        new OkHttpClientManager.Param("exType", type),
                        new OkHttpClientManager.Param("message", e.getMessage()),
                        new OkHttpClientManager.Param("info", ExceptionUtils.getStackTrace(e))};
                String url = String.format("%s%s",
                        PropertyService.getInstance().getKey("serverUrl"), "/common/reveive_exception");
                OkHttpClientManager.postAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                Log.d(TAG, "SEND FAILED!");
                            }
                            @Override
                            public void onResponse(BaseData data) {
                                Log.d(TAG, "SEND SUCCESS!");
                            }
                        },
                        params);
                Log.e(TAG,"NAME="+Thread.currentThread().getName());
            }
        });
    }
}
