package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.data.SettingItem;

import java.util.List;

public class MissModeAdpter extends BaseAdapter {
    private Context mContext;
    private List<SettingItem> mData = null;

    public MissModeAdpter(Context mContext, List<SettingItem> mData) {
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
        Object obj = mData.get(position);
        //设置下控件的值
        SettingItem item = (SettingItem) obj;
        SettingViewHolder holder = null;
        if(convertView == null){
            holder = new SettingViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.mode_miss_item, parent, false);
            holder.txt = convertView.findViewById(R.id.txt_des);
            holder.txt_num = convertView.findViewById(R.id.txt_num);
            convertView.setTag(holder);

        }else{
            holder = (SettingViewHolder) convertView.getTag();
        }

        if(item != null){
            holder.txt.setText(item.getModeText());
            holder.txt_num.setText(item.getMiss());
        }
        return convertView;
    }

    private static class SettingViewHolder{
        TextView txt;
        TextView txt_num;
    }


}
