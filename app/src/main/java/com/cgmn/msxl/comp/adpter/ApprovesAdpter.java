package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.CommentBean;
import com.cgmn.msxl.server_interface.RelatedToMe;

import java.util.HashMap;
import java.util.List;

public class ApprovesAdpter extends RelatedBaseAdapter {

    public ApprovesAdpter(Context mContext, List<RelatedToMe> mData) {
        this.mContext = mContext;
        this.mData = mData;
        views = new HashMap<>();
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpand, View convertView, ViewGroup viewGroup) {
        RelatedToMe item = mData.get(groupPosition);
        //设置下控件的值
        if(!views.containsKey(groupPosition)){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.approves_item, viewGroup, false);
            views.put(groupPosition, convertView);
            ApproveViewHolder holder = new ApproveViewHolder(convertView);
            if (item.getSmallCut() != null && item.getSmallCut().length > 0) {
                holder.comment_item_logo.setImageContent(item.getSmallCut());
            } else {
                Glide.with(mContext).load(R.drawable.user_logo)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop()
                        .into(holder.comment_item_logo);
            }

            holder.comment_item_userName.setText(item.getUserName());
            holder.comment_item_time.setText(CommentBean.analysisTime(item.getCreatedAt()));
            holder.txt_app_des.setText(String.format("%s赞了我的帖子", item.getUserName()));
            holder.txt_mycontent.setText(item.getMyContent());
        }else{
            convertView = views.get(groupPosition);
        }

        return convertView;

    }

    private class ApproveViewHolder{
        private NetImageView comment_item_logo;
        private TextView comment_item_userName, comment_item_time,txt_app_des;
        private TextView txt_mycontent;

        public ApproveViewHolder(View view){
            comment_item_logo = view.findViewById(R.id.comment_item_logo);
            comment_item_userName = view.findViewById(R.id.comment_item_userName);
            comment_item_time = view.findViewById(R.id.comment_item_time);
            txt_app_des = view.findViewById(R.id.txt_app_des);
            txt_mycontent = view.findViewById(R.id.txt_mycontent);
        }
    }

}
