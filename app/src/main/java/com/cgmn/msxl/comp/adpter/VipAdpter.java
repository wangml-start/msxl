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
import com.cgmn.msxl.utils.CommonUtil;

import java.util.List;

public class VipAdpter extends BaseAdapter {
    private Context mContext;
    private List<VipItem> mData = null;
    private float rate = 1;

    public VipAdpter(Context mContext, List<VipItem> mData) {
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
        VipViewHolder holder = null;
        if(convertView == null){
            holder = new VipViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vip_charge_item, parent, false);
            holder.txt_day_des = convertView.findViewById(R.id.txt_day_des);
            holder.txt_amt = convertView.findViewById(R.id.txt_amt);
            holder.txt_day_des_next = convertView.findViewById(R.id.txt_day_des_next);

            convertView.setTag(R.id.tag_main_item,holder);
        }else{
            holder = (VipAdpter.VipViewHolder) convertView.getTag(R.id.tag_main_item);
        }
        VipItem item = mData.get(position);
        if(item != null){
            if(item.getTimeType().equals(VipDataSetting.DAY_TYPE)){
                holder.txt_day_des.setText(String.format("%s天", item.getNum()));
                holder.txt_day_des_next.setText(String.format("%s/天",CommonUtil.formatNumer(rate * item.getAmt()/item.getNum())));
            }else{
                String discount = "";
                if(item.getNum() == 3){
                    discount = "8折";
                }else if(item.getNum() == 12){
                    discount = "7折";
                }else if(item.getNum() == 24){
                    discount = "6折";
                }else if(item.getNum() == 36){
                    discount = "5折";
                }
                Float daily = (rate * item.getAmt()) / (item.getNum()*30);
                holder.txt_day_des.setText(String.format("%s个月",item.getNum()));
                holder.txt_day_des_next.setText(String.format("%s %s/天",discount, CommonUtil.formatNumer(daily)));
            }
            Float money = rate * item.getAmt();

            StringBuffer contentText = new StringBuffer(String.format("￥%s", money.intValue()));
            SpannableString span = new SpannableString(contentText.toString());
            span.setSpan(new AbsoluteSizeSpan(14, true), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            holder.txt_amt.setText(span);
        }
        return convertView;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    private static class VipViewHolder{
        TextView txt_day_des,txt_day_des_next;
        TextView txt_amt;
    }


}
