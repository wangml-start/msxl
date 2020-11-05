
package com.cgmn.msxl.comp.pop;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import com.cgmn.msxl.R;

/**
 * Created by Lister on 2017-06-12.
 * PopupWindow 工具类
 */
public class PayPop extends PopupWindow {
    private View mView; // PopupWindow 菜单布局
    private Context mContext; // 上下文参数
    private View.OnClickListener mzfbListener; // 支付宝监听器
    private View.OnClickListener mwxListener; // 微信监听器
    private Button btn_zfb, btn_wx, btn_cancel;

    private String amt;

    public PayPop(Activity context, View.OnClickListener mzfbListener, View.OnClickListener mwxListener) {
        super(context);
        this.mContext = context;
        this.mzfbListener = mzfbListener;
        this.mwxListener = mwxListener;
        Init();
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
        btn_wx = mView.findViewById(R.id.btn_wx);
        btn_cancel = mView.findViewById(R.id.btn_cancel);

        btn_zfb.setOnClickListener(mzfbListener);
        btn_wx.setOnClickListener(mwxListener);
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
}
