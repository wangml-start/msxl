package com.cgmn.msxl.comp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import com.cgmn.msxl.R;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class showPassworCheckBox extends CheckBox
        implements CompoundButton.OnCheckedChangeListener{

    public void setPws(EditText pws) {
        if(this.pwsList == null){
            this.pwsList = new ArrayList<>();
        }
        this.pwsList.add(pws);
    }

    private List<EditText> pwsList;

    public showPassworCheckBox(Context context) {
        super(context);
    }
    public showPassworCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public showPassworCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for(EditText pws : pwsList){
            if(isChecked){
                //选择状态 显示明文--设置为可见的密码
                pws.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                pws.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    }
}
