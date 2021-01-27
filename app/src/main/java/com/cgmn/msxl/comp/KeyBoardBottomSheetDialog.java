package com.cgmn.msxl.comp;


import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class KeyBoardBottomSheetDialog extends BottomSheetDialog {

    public KeyBoardBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    public KeyBoardBottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected KeyBoardBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void dismiss() {
        //因为dismiss之后当前焦点的EditText无法获取，所以自定义一下
        hideKeyBoard();
        super.dismiss();
    }

    public void hideKeyBoard() {
        View view = null;
        InputMethodManager imm = null;
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getWindow() != null) {
            view = getWindow().getCurrentFocus();
        }
        if (null != view && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
