
package com.cgmn.msxl.comp.pop;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import com.alipay.sdk.app.PayTask;
import com.cgmn.msxl.R;
import com.cgmn.msxl.ac.MainActivity;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.bean.AliPayResult;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.PaymentListener;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.VipDataSetting;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.cgmn.msxl.utils.ShowDialog;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by Lister on 2017-06-12.
 * PopupWindow 工具类
 */
public class PayPop extends PopupWindow {
    public final static int CHARGE_TYPE_VIP = 0;
    public final static int CHARGE_TYPE_MARKET = 1;
    public final static int CHARGE_TYPE_CASH = 2;

    private View mView; // PopupWindow 菜单布局
    private Context mContext; // 上下文参数
    private Button btn_zfb, btn_wx, btn_cancel;

    private Handler mHandler;
    private Map<String, String> params;

    private String amt;

    private PaymentListener paymentListener;

    public PayPop(Context context, PaymentListener payls) {
        super(context);
        this.mContext = context;
        paymentListener = payls;
        Init();
        initMessageHandler();
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    private void initMessageHandler(){
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(MessageUtil.VIP_CHARGE_ZFB_RESPONSE == msg.what){
                    alipay((String) msg.obj);
                } else if(MessageUtil.ZFB_LOCAL_RESPONSE == msg.what){
                    AliPayResult payResult = new AliPayResult((Map<String, String>) msg.obj);
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if ("9000".equals(resultStatus)) {
                        paymentListener.afterPayment(true);
                        new ShowDialog().showTips(mContext, "支付成功" + resultInfo ,"");
                    } else {
                        paymentListener.afterPayment(false);
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        new ShowDialog().showTips(mContext, "支付失败" + resultInfo ,"");
                    }
                }else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });

    }

    public void setAmt(String amt) {
        this.amt = amt;
        if(amt != null){
            btn_zfb.setText(String.format("支付宝(￥%s)", amt));
            btn_wx.setText(String.format("微信支付(￥%s)", amt));
        }
    }

    /**
     * 设置布局以及点击事件
     */
    private void Init() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.pop_pay_item, null);

        btn_zfb = mView.findViewById(R.id.btn_zfb);
//        btn_wx = mView.findViewById(R.id.btn_wx);
        btn_cancel = mView.findViewById(R.id.btn_cancel);

        View.OnClickListener ls = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn_zfb){
                    onZFBCilck();
                    dismiss();
                }
            }
        };
        btn_zfb.setOnClickListener(ls);
//        btn_wx.setOnClickListener(mwxListener);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 导入布局
        this.setContentView(mView);
        // 设置动画效果
        this.setAnimationStyle(R.style.popwindow_anim_style);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(dw);
        // 单击弹出窗以外处 关闭弹出窗
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mView.findViewById(R.id.ll_pop).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }


    private void onZFBCilck(){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/payment/alipay_add_signature";
                Map<String, String> params = getParams();
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        action, params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.VIP_CHARGE_ZFB_RESPONSE;
                                try {
                                    message.obj = data.getAlipaySignature();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    }
                                } catch (Exception e) {
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                }
                                mHandler.sendMessage(message);
                            }
                        });
            }
        });
    }

    /**
     * 支付（加签过程不允许在客户端进行，必须在服务端，否则有极大的安全隐患）
     *
     * @param orderInfo 加签后的支付请求参数字符串（主要包含商户的订单信息，key=value形式，以&连接）。
     */
    private void alipay(final String orderInfo) {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask((Activity) mContext);
                //第二个参数设置为true，将会在调用pay接口的时候直接唤起一个loading
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = MessageUtil.ZFB_LOCAL_RESPONSE;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        });
    }


    private void onWXCilck(){

    }
}
