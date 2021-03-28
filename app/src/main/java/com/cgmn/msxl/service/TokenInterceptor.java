package com.cgmn.msxl.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.utils.CommonUtil;
import com.google.gson.Gson;
import com.squareup.okhttp.*;
import okio.Buffer;
import okio.BufferedSource;

import java.io.IOException;
import java.nio.charset.Charset;

public class TokenInterceptor implements Interceptor {
    private static final String TAG = TokenInterceptor.class.getSimpleName();
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private Context mContext;

    public TokenInterceptor(Context co){
        mContext = co;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response originalResponse = chain.proceed(request);
        ResponseBody responseBody = originalResponse.body();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();
        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(UTF8);
        }

        //bodyString: {"error":"Token已经失效,或账号在别处登录！","status":-100}
        String bodyString = buffer.clone().readString(charset);
        BaseData data = new Gson().fromJson(bodyString, BaseData.class);
//        if(bodyString != null){
//            String kb = CommonUtil.formatNumer(bodyString.getBytes().length/1024.0f);
//            Log.e(TAG,request.urlString().substring(0, 60) +" KB:" + kb);
//        }
        /***************************************/
        if (data.getStatus() == -100){
            //token过期 发通知
            Intent intents1 = new Intent(ReceiverMessage.STOP_TIMER_STATK);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intents1);
            Intent intents2 = new Intent(ReceiverMessage.TOKEN_INVALID);
            intents2.putExtra("message", data.getError());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intents2);
        }

        // otherwise just pass the original response on
        return originalResponse;
    }
}
