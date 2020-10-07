package com.cgmn.msxl.comp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.cgmn.msxl.R;

public class CustmerToast extends Toast {
    private static CustmerToast toast;
    private static LayoutInflater inflater;
    private static View layout;
    private static TextView toast_text;

    private CustmerToast(Context context) {
        super(context);
    }

    /**
     * 显示一个纯文本吐司
     *
     * @param context  上下文
     * @param stringId 显示的文本的Id
     */
    public static CustmerToast makeText(Context context, int stringId) {
        return getToast(context, context.getString(stringId), 0);
    }

    /**
     * 显示一个纯文本吐司
     *
     * @param context 上下文
     * @param text    显示的文本
     */
    public static CustmerToast makeText(Context context, CharSequence text) {
        return getToast(context, text, 0);
    }

    /**
     * 显示一个纯文本吐司
     *
     * @param context  上下文
     * @param stringId 显示的文本的Id
     * @param time     持续的时间
     */
    public static CustmerToast makeText(Context context, int stringId, int time) {
        return getToast(context, context.getString(stringId), time);
    }

    /**
     * 显示一个纯文本吐司
     *
     * @param context 上下文
     * @param text    显示的文本
     * @param time    持续的时间
     */
    public static CustmerToast makeText(Context context, CharSequence text, int time) {
        return getToast(context, text, time);
    }

    /**
     * 获取Toast对象
     *
     * @param context  上下文
     * @param text     显示的文本
     * @param time     持续的时间
     */
    public static CustmerToast getToast(Context context, CharSequence text, int time) {
        initToast(context, text);

        if (time == Toast.LENGTH_LONG) {
            toast.setDuration(Toast.LENGTH_LONG);
        } else {
            toast.setDuration(Toast.LENGTH_SHORT);
        }

        return toast;
    }

    /**
     * 初始化Toast
     *
     * @param context 上下文
     * @param text    显示的文本
     */
    private static void initToast(Context context, CharSequence text) {
        try {
            cancelToast();

            toast = new CustmerToast(context);
            // 获取LayoutInflater对象
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 由layout文件创建一个View对象
            layout = inflater.inflate(R.layout.toast_layout, null);

            // 实例化ImageView和TextView对象
            toast_text = layout.findViewById(R.id.toast_text);
            toast_text.setText(text);
            toast.setView(layout);
            toast.setGravity(Gravity.BOTTOM, 0, 70);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏当前Toast
     */
    public static void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    public void cancel() {
        try {
            super.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        try {
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
