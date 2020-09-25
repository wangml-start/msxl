package com.cgmn.msxl.comp;

import android.content.Context;
import android.text.InputType;
import android.widget.CompoundButton;
import android.widget.EditText;
import com.cgmn.msxl.R;

public class showPassworCheckBox extends androidx.appcompat.widget.AppCompatCheckBox
        implements CompoundButton.OnCheckedChangeListener{

    public showPassworCheckBox(Context context) {
        super(context);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        EditText pws = findViewById(R.id.tx_pwd);
        if(pws == null){
            pws = findViewById(R.id.tx_new_user_wd);
        }

        if(isChecked){
            //选择状态 显示明文--设置为可见的密码
            pws.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            pws.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }
}
