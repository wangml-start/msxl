package com.cgmn.msxl.comp.adpter;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.server_interface.TrendStock;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarketAdapter extends BaseAdapter {
    //定义两个类别标志
    private Context mContext;
    private List<TrendStock> mData = null;


    public MarketAdapter(Context mContext, List<TrendStock> mData) {
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
        ItemViewHolder itemHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.market_list_item, parent, false);
            itemHolder = new ItemViewHolder(convertView);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (MarketAdapter.ItemViewHolder) convertView.getTag();
        }

        final TrendStock item = mData.get(position);
        itemHolder.txt_s_name.setText(item.getStackName());
        itemHolder.txt_s_code.setText(item.getStackCode());
        itemHolder.txt_s_ch_rate.setText(item.getExchageRate());
        itemHolder.txt_s_end.setText(item.getEnd());
        itemHolder.txt_s_up_rate.setText(item.getUpRate());
        int color = mContext.getResources().getColor(R.color.text_black);
        if(item.getUpDown() > 0){
            color = mContext.getResources().getColor(R.color.kline_up);
        }else if(item.getUpDown() < 0){
            color = mContext.getResources().getColor(R.color.kline_down);
        }
        itemHolder.txt_s_end.setTextColor(color);
        itemHolder.txt_s_up_rate.setTextColor(color);

        return convertView;
    }

    //两个不同的ViewHolder
    private static class ItemViewHolder {
        TextView txt_s_name,txt_s_code,txt_s_end;
        TextView txt_s_up_rate,txt_s_ch_rate;

        public ItemViewHolder(View view){
            txt_s_name = view.findViewById(R.id.txt_s_name);
            txt_s_code = view.findViewById(R.id.txt_s_code);
            txt_s_end = view.findViewById(R.id.txt_s_end);
            txt_s_up_rate = view.findViewById(R.id.txt_s_up_rate);
            txt_s_ch_rate = view.findViewById(R.id.txt_s_ch_rate);
        }
    }
}
