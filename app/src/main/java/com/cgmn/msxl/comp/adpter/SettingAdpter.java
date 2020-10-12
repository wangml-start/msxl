package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.swb.SwitchButton;
import com.cgmn.msxl.data.SettingItem;

import java.util.ArrayList;
import java.util.List;

public class SettingAdpter extends BaseAdapter {
    private Context mContext;
    private List<SettingItem> mData = null;

    public SettingAdpter(Context mContext, List<SettingItem> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingViewHolder holder = null;
        if(convertView == null){
            holder = new SettingViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.mode_settin_item, parent, false);
            holder.txt = convertView.findViewById(R.id.txt_des);
            holder.switchButton = convertView.findViewById(R.id.bt_sw);
            convertView.setTag(R.id.Tag_setting_item, holder);
        }else{
            holder = (SettingViewHolder) convertView.getTag(R.id.Tag_setting_item);
        }

        Object obj = mData.get(position);
        //设置下控件的值
        SettingItem item = (SettingItem) obj;
        if(item != null){
            holder.txt.setText(item.getModeText());
            holder.switchButton.changeStatus(item.getState());
        }
        return convertView;
    }

    //两个不同的ViewHolder
    private static class SettingViewHolder{
        TextView txt;
        SwitchButton switchButton;
    }
}
