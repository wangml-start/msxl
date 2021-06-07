package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.CommentBean;
import com.cgmn.msxl.server_interface.RelatedToMe;
import com.cgmn.msxl.utils.CommonUtil;
import org.apache.shiro.codec.Base64;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommentsAdpter extends RelatedBaseAdapter {

    public CommentsAdpter(Context mContext, List<RelatedToMe> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpand, View convertView, ViewGroup viewGroup) {
        final RelatedToMe item = mData.get(groupPosition);
        //设置下控件的值
        CommentViewHolder holder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.comments_item, viewGroup, false);
            holder = new CommentViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (CommentViewHolder) convertView.getTag();
        }

        if (!CommonUtil.isEmpty(item.getSmallCut())) {
            byte[] bytes = Base64.decode(item.getSmallCut());
            holder.comment_item_logo.setImageContent(bytes);
        } else {
            holder.comment_item_logo.setImageResource(R.drawable.user_logo);
        }
        String content = String.format("%s 回复我的帖子：%s", item.getUserName(), item.getMyContent());
        holder.comment_item_userName.setText(item.getUserName());
        Date reDate = CommonUtil.parseDateString(item.getCreatedAt(), "yyyyMMdd HH:mm:ss");
        holder.comment_item_time.setText(CommentBean.analysisTime(reDate));
        holder.txt_app_des.setText(item.getHimContent());
        holder.txt_mycontent.setText(content);

        return convertView;
    }

    private class CommentViewHolder{
        private NetImageView comment_item_logo;
        private TextView comment_item_userName, comment_item_time,txt_app_des;
        private TextView txt_mycontent;

        public CommentViewHolder(View view){
            comment_item_logo = view.findViewById(R.id.comment_item_logo);
            comment_item_userName = view.findViewById(R.id.comment_item_userName);
            comment_item_time = view.findViewById(R.id.comment_item_time);
            txt_app_des = view.findViewById(R.id.txt_app_des);
            txt_mycontent = view.findViewById(R.id.txt_mycontent);
        }
    }

}
