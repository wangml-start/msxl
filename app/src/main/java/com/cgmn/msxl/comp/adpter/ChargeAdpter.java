package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.data.VipItem;
import com.cgmn.msxl.server_interface.VipDataSetting;

import java.util.List;

public class ChargeAdpter extends BaseAdapter {
    private Context mContext;
    private List<Integer> mData = null;

    public ChargeAdpter(Context mContext, List<Integer> mData) {
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
        //设置下控件的值
        ChargeViewHolder holder = null;
        if(convertView == null){
            holder = new ChargeViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vip_charge_item, parent, false);
            holder.txt_day_des = convertView.findViewById(R.id.txt_day_des);
            holder.txt_amt = convertView.findViewById(R.id.txt_amt);

            convertView.setTag(R.id.tag_main_item,holder);
        }else{
            holder = (ChargeAdpter.ChargeViewHolder) convertView.getTag(R.id.tag_main_item);
        }
        Integer item = mData.get(position);
        if(item != null){
            holder.txt_day_des.setText(String.format("%s万操盘金", item));
            StringBuffer contentText = new StringBuffer(String.format("￥%s", item));
            SpannableString span = new SpannableString(contentText.toString());
            span.setSpan(new AbsoluteSizeSpan(14, true), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            holder.txt_amt.setText(span);
        }
        return convertView;
    }

    private static class ChargeViewHolder{
        TextView txt_day_des;
        TextView txt_amt;
    }


}
