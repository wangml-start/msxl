package com.cgmn.msxl.ac;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.EditBaseActivity;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.HashMap;
import java.util.Map;

public class EditSignatureActivity extends EditBaseActivity
        implements TextWatcher {
    private static final String TAG = EditNameActivity.class.getSimpleName();
    private Context mContext;
    private EditText txt_content;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_usign_layout);
        mContext = this;
        baseBind(mContext);
        txt_content = findViewById(R.id.txt_content);
        txt_content.setText(fieldContent);
        txt_content.addTextChangedListener(this);
    }


    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        Map<String, Object> user = GlobalDataHelper.getUser(mContext);
        params.put("phone", (String) user.get("phone"));
        params.put("field_data", fieldData);
        params.put("content", txt_content.getText().toString());
        params.put("token", GlobalDataHelper.getToken(mContext));
        return params;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(CommonUtil.isEmpty(txt_content.getText().toString())){
            txt_complete.setEnabled(false);
        }else{
            txt_complete.setEnabled(true);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void finish() {
        dialog.cancel();
        dialog.dismiss();
        super.finish();
    }
}