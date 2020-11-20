package com.cgmn.msxl.utils;

import android.content.Context;
import com.cgmn.msxl.bean.CustomerDialog;
import com.cgmn.msxl.bean.TipDialog;

public class ShowDialog {

    private CustomerDialog customDialog;

    private TipDialog tipDialog;

    public ShowDialog() {

    }
    public void showTips(final Context context, String message) {
        tipDialog = new TipDialog(context);
        tipDialog.setMessage(message);
        tipDialog.show();
    }
    public void showTips(final Context context, String message,String title) {
        tipDialog = new TipDialog(context);
        tipDialog.setMessage(message);
        tipDialog.setTitle(title);
        tipDialog.show();
    }
    public void show(final Context context, String message, final OnBottomClickListener onBottomClickListener) {
        customDialog = new CustomerDialog(context);
        customDialog.setMessage(message);
        customDialog.setYesOnClickListener("确定", new CustomerDialog.onYesOnClickListener() {
            @Override
            public void onYesClick() {
                if (onBottomClickListener != null) {
                    onBottomClickListener.positive();
                }
                customDialog.dismiss();
            }
        });

        customDialog.setNoOnClickListener("取消", new CustomerDialog.onNoClickListener() {
            @Override
            public void onNoClick() {
                if (onBottomClickListener != null) {
                    onBottomClickListener.negative();
                }
                customDialog.dismiss();
            }
        });
        customDialog.show();

    }
    public void show(final Context context, String message,String confirm, final OnBottomClickListener onBottomClickListener) {
        customDialog = new CustomerDialog(context);
        customDialog.setMessage(message);
        customDialog.setYesOnClickListener(confirm, new CustomerDialog.onYesOnClickListener() {
            @Override
            public void onYesClick() {
                if (onBottomClickListener != null) {
                    onBottomClickListener.positive();
                }
                customDialog.dismiss();
            }
        });

        customDialog.setNoOnClickListener("取消", new CustomerDialog.onNoClickListener() {
            @Override
            public void onNoClick() {
                if (onBottomClickListener != null) {
                    onBottomClickListener.negative();
                }
                customDialog.dismiss();
            }
        });
        customDialog.show();

    }
    public interface OnBottomClickListener {
        void positive();

        void negative();

    }
}
