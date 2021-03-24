
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
public class UnlockPop extends PopupWindow {
    private View mView; // PopupWindow 菜单布局
    private Context mContext; // 上下文参数
    private View.OnClickListener chargeListener; //充值解锁
    private View.OnClickListener calcAddAmtListener; //累计消费解锁

    public UnlockPop(Activity context, View.OnClickListener chargeListener,
                     View.OnClickListener calcAddAmtListener) {
        super(context);
        this.mContext = context;
        this.chargeListener = chargeListener;
        this.calcAddAmtListener = calcAddAmtListener;
        Init();
    }

    /**
     * 设置布局以及点击事件
     */
    private void Init() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.pop_unlock_view, null);

        Button btn_pay = mView.findViewById(R.id.btn_pay);
        Button btn_calc_add = mView.findViewById(R.id.btn_calc_add);
        Button btn_cancel = mView.findViewById(R.id.icon_btn_cancel);

        btn_pay.setOnClickListener(chargeListener);
        btn_calc_add.setOnClickListener(calcAddAmtListener);
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
