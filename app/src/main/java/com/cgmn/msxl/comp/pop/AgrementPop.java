package com.cgmn.msxl.comp.pop;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.ac.UserAgrementActivity;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.service.GlobalDataHelper;

public class AgrementPop extends PopupWindow
        implements View.OnClickListener {

    private View view;
    private Context mContext;
    private TextView txt_agrement, btn_no, btn_yes;

    public AgrementPop(Context mContext, Integer width) {
        this.mContext = mContext;
        view =  LayoutInflater.from(mContext).inflate(R.layout.agreement_layout, null);
        view.setBackgroundResource(R.drawable.rounded_corners_pop);
        txt_agrement = (TextView) view.findViewById(R.id.txt_agrement);
        btn_no = (TextView) view.findViewById(R.id.btn_no);
        btn_yes = (TextView) view.findViewById(R.id.btn_yes);

        txt_agrement.setOnClickListener(this);
        btn_no.setOnClickListener(this);
        btn_yes.setOnClickListener(this);

        setAgrementText();

        // 设置外部可点击
        this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = view.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        /* 设置弹出窗口特征 */
        // 设置视图
        this.setContentView(this.view);
        // 设置动画效果
        this.setAnimationStyle(R.style.popwindow_anim_style);
        this.setWidth(width);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(dw);
    }



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_no) {
            // 销毁弹出框
            dismiss();
            System.exit(0);
        }else if(v.getId() == R.id.btn_yes){
            AppSqlHelper dbHelper = new AppSqlHelper(mContext);
            ContentValues values = new ContentValues();
            values.put("setting_name", "user_agrements");
            values.put("setting_value", "1");
            dbHelper.upsert("user_settings", values, "setting_name");
            GlobalDataHelper.updateUser(mContext);
            dismiss();
        }
    }

    private void setAgrementText(){
        String base = "    请您阅读、并充分理解《用户协议和隐私政策》的详细类容。如果您同意该协议，请点击同意，开始接受我们的服务。";
        String agrement = "《用户协议和隐私政策》";
        Integer starIndex = base.indexOf(agrement);
        SpannableString span = new SpannableString(base);
        span.setSpan(new ClickableSpan(){
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(mContext, UserAgrementActivity.class);
                mContext.startActivity(intent);
            }
        }, starIndex, starIndex+agrement.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        txt_agrement.setText(span);
        txt_agrement.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
