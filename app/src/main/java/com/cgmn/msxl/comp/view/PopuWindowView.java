package com.cgmn.msxl.comp.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import com.cgmn.msxl.R;
import com.cgmn.msxl.bean.PopuBean;
import com.cgmn.msxl.comp.adpter.PopuWindowAdapter;
import com.cgmn.msxl.in.TdataListener;


import java.util.ArrayList;
import java.util.List;
public class PopuWindowView implements AdapterView.OnItemClickListener {

    View viewItem = null;
    ListView pupoListView;
    PopupWindow pullDownView;// 弹出窗口
    private List<PopuBean> popuLists = new ArrayList<>();
    private PopuWindowAdapter mPopuWindowAdapter;
    private Context mContext;
    private TdataListener mTdataListener;
    private int maxLine = 5;

    public PopuWindowView(Context mContext, int widthGravity) {
        this.mContext = mContext;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        viewItem = inflater.inflate(R.layout.dialogui_popu_options, null);
        pupoListView = (ListView) viewItem.findViewById(R.id.customui_list);
        mPopuWindowAdapter = new PopuWindowAdapter(mContext, popuLists);
        pupoListView.setAdapter(mPopuWindowAdapter);
        pullDownView = new PopupWindow(viewItem, widthGravity,
                LayoutParams.WRAP_CONTENT, true);
        pullDownView.setOutsideTouchable(true);
        pupoListView.setOnItemClickListener(this);
        pullDownView.setFocusable(true);
        //为了点击非悬浮框处或者按返回键悬浮框消失，需要如下设置。
        //而且必须设置一个背景才有效。
        pullDownView.setTouchable(true);
        pullDownView.setBackgroundDrawable(new ColorDrawable(0x00000000));
    }

    /**
     * 设置下拉框的数据
     */
    public void initPupoData(TdataListener tdataListener) {
        mTdataListener = tdataListener;
        if (mTdataListener != null) {
            mTdataListener.initPupoData(popuLists);
        }
        if (popuLists != null && popuLists.size() > maxLine) {
            pullDownView.setHeight(dip2px(maxLine * 40));
        }
        mPopuWindowAdapter.notifyDataSetChanged();
    }

    private int dip2px(int dip) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * 设置最大行popuWindow
     */
    public void setMaxLines(int maxLines) {
        maxLine = maxLines;
    }

    /**
     * 显示popuWindow
     */
    public void showing(View v) {
        pullDownView.showAsDropDown(v, 0, 0);
    }

    public void showing(View v, int x, int y) {
        pullDownView.showAsDropDown(v, x, y);
    }

    /**
     * 关闭popuWindow
     */
    public void dismiss() {
        pullDownView.dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (mTdataListener != null) {
            dismiss();
            mTdataListener.onItemClick(adapterView, view, position);
        }
    }

    /**
     * 获取选择的名称
     */
    public String getTitle(int popuPosition) {
        return popuLists.get(popuPosition).getTitle();
    }

    /**
     * 获取选择的id
     */
    public String getValue(int popuPosition) {
        return popuLists.get(popuPosition).getValue();
    }

    public List<PopuBean> getPopuLists() {
        return popuLists;
    }
}
