package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cgmn.msxl.R;
import com.cgmn.msxl.ac.ImageViewActivity;
import com.cgmn.msxl.comp.view.CenterImageSpan;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.RankEntity;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.CommonUtil;
import org.apache.shiro.codec.Base64;

import java.util.List;

public class UserRankAdpter extends BaseAdapter {
    private Context mContext;
    private List<RankEntity> mData = null;
    private String tranType;

    private Boolean hiddeCash=false;

    public UserRankAdpter(Context mContext, List<RankEntity> mData, String tranType) {
        this.mContext = mContext;
        this.mData = mData;
        this.tranType = tranType;
    }

    public void setHiddeCash(Boolean hiddeCash) {
        this.hiddeCash = hiddeCash;
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
        RankViewHolder holder=null;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_rank_item, parent, false);
            holder = new RankViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (RankViewHolder) convertView.getTag();
        }
        final RankEntity map = mData.get(position);
        //性别
        StringBuffer userName = new StringBuffer(map.getUserName()+" ");
        SpannableString span = new SpannableString(userName.toString());
        if(map.getGender() != null){
            Drawable d = null;
            if(map.getGender() == 0){
                d = mContext.getResources().getDrawable(R.drawable.male);
            }else{
                d = mContext.getResources().getDrawable(R.drawable.female);
            }
            d.setBounds(0, 0, 40, 40);
            CenterImageSpan imageSpan = new CenterImageSpan(d);
            span.setSpan(imageSpan, userName.length()-1, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

        holder.txt_user_name.setText(span);
        holder.txt_amt.setText(CommonUtil.formatAmt(map.getStAmt()));
        if(tranType.equals(StockHolder.RANK_SUMMARY+"") || tranType.equals("1")  || tranType.equals("2") ){
            holder.txt_rate.setText(CommonUtil.formatPercent(map.getRate()));
        }else{
            holder.txt_rate.setText(CommonUtil.formatAmt(map.getRate()));
        }

        if(map.getRate() >= 0){
            holder.txt_rate.setTextColor(convertView.getResources().getColor(R.color.main_red_color));
        }else{
            holder.txt_rate.setTextColor(convertView.getResources().getColor(R.color.kline_down));
        }
        holder.head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalDataHelper.setData("email", map.getPhone());
                Intent intent = new Intent(mContext, ImageViewActivity.class);
                mContext.startActivity(intent);
            }
        });
        holder.txt_no.setText((position+1)+"");
        if(!CommonUtil.isEmpty(map.getSmallCut())){
            holder.head.setImageContent(Base64.decode(map.getSmallCut()));
        }else{
            Glide.with(mContext).load(R.drawable.user_logo)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(holder.head);
        }

        return convertView;
    }

    private class RankViewHolder{
        TextView txt_no;
        TextView txt_user_name;
        TextView txt_amt;
        TextView txt_rate;
        NetImageView head;

        public RankViewHolder(View convertView){
            txt_no = convertView.findViewById(R.id.txt_no);
            txt_user_name = convertView.findViewById(R.id.txt_user_name);
            txt_amt = convertView.findViewById(R.id.txt_amt);
            txt_rate = convertView.findViewById(R.id.txt_rate);
            head = convertView.findViewById(R.id.img_head);

            if(hiddeCash){
                txt_amt.setVisibility(View.GONE);
            }
        }
    }

}
