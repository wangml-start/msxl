package com.cgmn.msxl.bean;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.cgmn.msxl.R;
import com.cgmn.msxl.utils.CommonUtil;

public class TipDialog extends Dialog {
    private Button ok;
    private TextView txt_message, txt_title; //消息提示文本
    private String titleStr; //从外界设置的title文本
    private String messageStr; //从外界设置的消息文本
    private String okStr; //确定文本的显示内容
    private Window window = null;

    public TipDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_tip_layout);
        //点击dialog以外的空白处是否隐藏
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();
        //设置窗口显示
        windowDeploy();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        ok = (Button) findViewById(R.id.ok);
        txt_message = (TextView) findViewById(R.id.d_message);
        txt_title = (TextView) findViewById(R.id.d_title);
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        if (!CommonUtil.isEmpty(titleStr)) {
            txt_title.setText(titleStr);
        } else {
            txt_title.setText("温馨提示");
        }
        if (messageStr != null) {
            txt_message.setText(messageStr);
        }
        if (okStr != null) {
            ok.setText(okStr);
        }
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void windowDeploy() {
        window = getWindow();
        window.setGravity(Gravity.CENTER); //设置窗口显示位置
//        window.setWindowAnimations(R.style.dialogWindowAnim); //设置窗口弹出动画
    }

    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        titleStr = title;
    }

    /**
     * 从外界Activity为Dialog设置dialog的message
     *
     * @param message
     */
    public void setMessage(String message) {
        messageStr = message;
    }

}
