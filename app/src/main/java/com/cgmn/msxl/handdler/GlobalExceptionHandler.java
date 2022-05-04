package com.cgmn.msxl.handdler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.squareup.okhttp.Request;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class GlobalExceptionHandler {
    private Context mContxt;
    private static String TAG = GlobalExceptionHandler.class.getSimpleName();
    private static GlobalExceptionHandler handler;

    static Integer LOSS_SERVER_COUNT = 0;
    private GlobalExceptionHandler(){

    }

    public static void resetLossCount(){
        LOSS_SERVER_COUNT = 0;
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

    @SuppressLint("WrongConstant")
    public void handlerException(Exception e){
        try{
            if(e instanceof ConnectException){
                CustmerToast.makeText(mContxt, mContxt.getString(R.string.network_loss)).show();
            }else if(e instanceof SocketTimeoutException){
                LOSS_SERVER_COUNT++;
                if(LOSS_SERVER_COUNT > 3){
                    CustmerToast.makeText(mContxt, mContxt.getString(R.string.server_loss), 1).show();
                }else{
                    CustmerToast.makeText(mContxt, mContxt.getString(R.string.network_loss)).show();
                }
            }else {
                if(!CommonUtil.isEmpty(e.getMessage())){
                    CustmerToast.makeText(mContxt, e.getMessage()).show();
                }
            }
        }catch (Exception e1){}
    }

    public void handlerExceptionUpException(final Exception e){
        final String type = "APP_RUNTIMEERROR";
        try{
            if((ConnectException)e instanceof ConnectException){
                CustmerToast.makeText(mContxt, mContxt.getString(R.string.network_loss)).show();
            }
        }catch (Exception e1){
            if(!CommonUtil.isEmpty(e.getMessage())){
                CustmerToast.makeText(mContxt, e.getMessage()).show();
            }
        }

        final String token = GlobalDataHelper.getToken(mContxt);
        if(CommonUtil.isEmpty(token)){
            Log.d(TAG, "No Find TOKEN When send exception!");
            return;
        }
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", token),
                        new OkHttpClientManager.Param("exType", type),
                        new OkHttpClientManager.Param("message", e.getMessage()),
                        new OkHttpClientManager.Param("info", ExceptionUtils.getStackTrace(e))};
                String url = String.format("%s%s",
                        ConstantHelper.serverUrl, "/common/reveive_exception");
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
