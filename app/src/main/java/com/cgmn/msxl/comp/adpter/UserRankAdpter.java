package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.List;
import java.util.Map;

public class UserRankAdpter extends BaseAdapter {
    private Context mContext;
    private List<Map<String, Object>> mData = null;

    public UserRankAdpter(Context mContext, List<Map<String, Object>> mData) {
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
        Map<String, Object> map = mData.get(position);
        //设置下控件的值
        RankViewHolder holder = null;
        if(convertView == null){
            holder = new UserRankAdpter.RankViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_rank_item, parent, false);
            holder.txt_no = convertView.findViewById(R.id.txt_no);
            holder.txt_user_name = convertView.findViewById(R.id.txt_user_name);
            holder.txt_amt = convertView.findViewById(R.id.txt_amt);

            convertView.setTag(R.id.tag_main_item,holder);
        }else{
            holder = (UserRankAdpter.RankViewHolder) convertView.getTag(R.id.tag_main_item);
        }

        holder.txt_no.setText((position+1)+"");
        holder.txt_user_name.setText((String) map.get("user_name"));
        holder.txt_amt.setText(CommonUtil.formatAmt(map.get("st_amt")));
        return convertView;

    }

    private static class RankViewHolder{
        TextView txt_no;
        TextView txt_user_name;
        TextView txt_amt;
    }

}
