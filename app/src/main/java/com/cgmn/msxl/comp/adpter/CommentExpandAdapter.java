package com.cgmn.msxl.comp.adpter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.view.CenterImageSpan;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.CommentDetailBean;
import com.cgmn.msxl.data.ReplyDetailBean;
import com.cgmn.msxl.in.CommentListener;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentExpandAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "CommentExpandAdapter";
    private List<CommentDetailBean> commentBeanList;
    private List<ReplyDetailBean> replyBeanList;
    private Context context;
    private CommentListener commentListener;
    private boolean expandAll = false;
    private Integer expandNum = 3;
    private boolean expandAllContent = true;
    private Integer allContentLength = 150;

    private String color = "#4D6AC3";

    public void setCommentListener(CommentListener commentListener) {
        this.commentListener = commentListener;
    }

    public void setExpandAll(boolean expandAll) {
        this.expandAll = expandAll;
    }

    public void setExpandAllContent(boolean expandAllContent) {
        this.expandAllContent = expandAllContent;
    }

    public CommentExpandAdapter(Context context, List<CommentDetailBean> commentBeanList) {
        this.context = context;
        this.commentBeanList = commentBeanList;
    }

    @Override
    public int getGroupCount() {
        return commentBeanList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        if (commentBeanList.get(i).getReplyList() == null) {
            return 0;
        } else {
            Integer count = commentBeanList.get(i).getReplyList().size();
            return (expandAll) ? count : Math.min(count, expandNum+1);
        }

    }

    @Override
    public Object getGroup(int i) {
        return commentBeanList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return commentBeanList.get(i).getReplyList().get(i1);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getCombinedChildId(groupPosition, childPosition);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpand, View convertView, ViewGroup viewGroup) {
        final CommentDetailBean bean = commentBeanList.get(groupPosition);
        GroupHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.comment_item_layout, viewGroup, false);
            holder = new GroupHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }

        final GroupHolder groupHolder = holder;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.comment_item_like){
                    if(bean.getMyApprove() == 1){
                        groupHolder.iv_like.setImageResource(R.drawable.icon_comment_like);
                        bean.setMyApprove(0);
                        bean.setApprove((Integer.valueOf(bean.getApprove()) - 1)+"");
                        groupHolder.comment_approve.setText(bean.getApprove());
                        commentListener.onApproveClick(groupPosition,"unapprove");
                    }else {
                        groupHolder.iv_like.setImageResource(R.drawable.liked);
                        bean.setMyApprove(1);
                        bean.setApprove((Integer.valueOf(bean.getApprove()) + 1)+"");
                        groupHolder.comment_approve.setText(bean.getApprove());
                        commentListener.onApproveClick(groupPosition,"approve");
                    }
                } else if(view.getId() == R.id.comment_icon){
                    commentListener.onCommentClick(groupPosition);
                } else if(view.getId() == R.id.comment_setting){
                    commentListener.onSettingClick(view, groupPosition);
                } else if(view.getId() == R.id.comment_picture){
                    commentListener.onShowPicture(bean.getPicture(), null);
                } else if(view.getId() == R.id.comment_item_logo){
                    commentListener.onShowPicture(null, bean.getPhone());
                }
            }
        };
        groupHolder.iv_like.setOnClickListener(listener);
        groupHolder.comment_icon.setOnClickListener(listener);
        groupHolder.comment_setting.setOnClickListener(listener);
        groupHolder.logo.setOnClickListener(listener);
        groupHolder.comment_picture.setOnClickListener(listener);
        //init content
        //head
        if (bean.getUserLogo() != null && bean.getUserLogo().length > 0) {
            holder.logo.setImageContent(bean.getUserLogo());
        } else {
            Glide.with(context).load(R.drawable.user_logo)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(holder.logo);
        }
        holder.tv_name.setText(bean.getNickName());
        holder.tv_time.setText(bean.getCreateDate());

        //content
        if(!expandAllContent && bean.getContent() != null && bean.getContent().length() > allContentLength){
            holder.tv_content.setText(packUpContent(bean.getContent(), groupPosition));
            holder.tv_content.setMovementMethod(LinkMovementMethod.getInstance());
        }else{
            holder.tv_content.setText(bean.getContent());
        }

        holder.comment_picture.setImageContent(bean.getPicture());
        if (bean.getPicture() != null && bean.getPicture().length > 0) {
            holder.comment_picture.setVisibility(View.VISIBLE);
        }else{
            holder.comment_picture.setVisibility(View.GONE);
        }

        holder.comment_num.setText("");
        if(bean.getReplyList() != null && bean.getReplyList().size() > 0){
            holder.comment_num.setText(bean.getReplyList().size()+"");
        }
        if (!"0".equals(bean.getApprove())) {
            holder.comment_approve.setText(bean.getApprove());
        }
        if (bean.getMyApprove() == 1) {
            holder.iv_like.setImageResource(R.drawable.liked);
        }


        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean b, View convertView, ViewGroup viewGroup) {
        if(!expandAll && childPosition >= expandNum){
            ChildHolder childHolder=null;
            if(convertView == null || convertView.getTag(R.id.tag_edit_item) == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.show_more_item, viewGroup, false);
                childHolder = new ChildHolder(convertView);
                convertView.setTag(R.id.tag_edit_item, childHolder);
            }else {
                childHolder = (ChildHolder) convertView.getTag(R.id.tag_edit_item);
            }
            childHolder.show_more.setText(String.format("查看全部%s条评论>", commentBeanList.get(groupPosition).getReplyList().size()));
            childHolder.show_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentListener.onShowMoreClick(groupPosition);
                }
            });
            return convertView;
        }else{
            ChildHolder childHolder=null;
            if (convertView == null ||  convertView.getTag() == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.comment_reply_item_layout, viewGroup, false);
                childHolder = new ChildHolder(convertView);
                convertView.setTag(childHolder);
            } else {
                childHolder = (ChildHolder) convertView.getTag();
            }

            childHolder.tv_content.setText(packUpReplayContent(groupPosition, childPosition));
            childHolder.tv_content.setMovementMethod(LinkMovementMethod.getInstance());

            return convertView;
        }
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    private class GroupHolder {
        private NetImageView logo, comment_picture;
        private TextView tv_name, tv_content, tv_time, comment_approve,comment_setting,comment_num;
        private ImageView iv_like, comment_icon;

        public GroupHolder(View view) {
            logo = (NetImageView) view.findViewById(R.id.comment_item_logo);
            comment_picture = (NetImageView) view.findViewById(R.id.comment_picture);
            tv_content = (TextView) view.findViewById(R.id.comment_item_content);
            tv_name = (TextView) view.findViewById(R.id.comment_item_userName);
            tv_time = (TextView) view.findViewById(R.id.comment_item_time);
            iv_like = (ImageView) view.findViewById(R.id.comment_item_like);
            comment_icon = (ImageView) view.findViewById(R.id.comment_icon);
            comment_approve = (TextView) view.findViewById(R.id.comment_approve);
            comment_setting = (TextView) view.findViewById(R.id.comment_setting);
            comment_num = (TextView) view.findViewById(R.id.comment_num);
        }
    }

    private class ChildHolder {
        private TextView tv_content;
        private TextView show_more;

        public ChildHolder(View view) {
            tv_content = (TextView) view.findViewById(R.id.reply_item_content);
            show_more = (TextView) view.findViewById(R.id.show_more);
        }
    }

    @SuppressLint("ResourceAsColor")
    private SpannableString packUpContent(String content, final Integer groupPosition){
        StringBuffer contentText = new StringBuffer();
        contentText.append(content.substring(0, allContentLength));
        contentText.append("... ");
        contentText.append("查看全文>");
        SpannableString span = new SpannableString(contentText.toString());
        span.setSpan(new ClickableSpan(){
            @Override
            public void onClick(View widget) {
                commentListener.onShowMoreClick(groupPosition);
            }
        }, contentText.length()-5, contentText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        updateDrawState(span, contentText.length()-5, contentText.length());
        return span;
    }

    private void updateDrawState(SpannableString span, Integer start, Integer end){
        span.setSpan(new UnderlineSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor(color));
                ds.setUnderlineText(false);// 去掉下划线
            }
        }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    @SuppressLint("ResourceAsColor")
    private SpannableString packUpReplayContent(final int groupPosition, final int childPosition){
        final ReplyDetailBean subbean = commentBeanList.get(groupPosition).getReplyList().get(childPosition);
        List<Integer[]> pos = new ArrayList<>();
        Integer start = 0;
        StringBuffer contentText = new StringBuffer(subbean.getReplayFrom());
        pos.add(new Integer[]{start, contentText.length()});
        start = contentText.length();
        if(!CommonUtil.isEmpty(subbean.getReplayTo())){
            contentText.append("回复");
            contentText.append(subbean.getReplayTo());
            contentText.append(":");
            pos.add(new Integer[]{start+2, start+2+subbean.getReplayTo().length()});
        }else{
            contentText.append(":");
        }
        contentText.append(subbean.getContent());
        if(subbean.getPicture() != null && subbean.getPicture().length > 0) {
            contentText.append("   查看图片");
        }
        SpannableString span = new SpannableString(contentText.toString());
        if(pos.size() > 0){
            Integer[] from = pos.get(0);
            span.setSpan(new ClickableSpan(){
                @Override
                public void onClick(View widget) {
                    commentListener.onChildReplayClick(groupPosition, childPosition, subbean.getUserId());
                }
            }, from[0], from[1], Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            updateDrawState(span, from[0], from[1]);
        }
//        if(pos.size() > 1){
//            Integer[] to = pos.get(1);
//            span.setSpan(new ClickableSpan(){
//                @Override
//                public void onClick(View widget) {
//                    commentListener.onChildReplayClick(groupPosition, childPosition, subbean.getReplayUserId());
//                }
//            }, to[0], to[1], Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//            updateDrawState(span, to[0], to[1]);
//        }

        //处理图片
        if(subbean.getPicture() != null && subbean.getPicture().length > 0){
            span.setSpan(new ClickableSpan(){
                @Override
                public void onClick(View widget) {
                    commentListener.onShowPicture(subbean.getPicture(), null);
                }
            }, contentText.length()-7, contentText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            updateDrawState(span, contentText.length()-7, contentText.length());
            Drawable d = context.getResources().getDrawable(R.drawable.photo);
            d.setColorFilter(Color.parseColor(color), PorterDuff.Mode.MULTIPLY);
            d.setBounds(0, 0, 70, 60);
            //8.创建ImageSpan,然后用ImageSpan来替换文本
            CenterImageSpan imageSpan = new CenterImageSpan(d);
            span.setSpan(imageSpan, contentText.length()-6, contentText.length()-5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return span;
    }


    /**
     * by moos on 2018/04/20
     * func:评论成功后插入一条数据
     *
     * @param commentDetailBean 新的评论数据
     */
    public void addTheCommentData(CommentDetailBean commentDetailBean) {
        if (commentDetailBean != null) {
            commentDetailBean.setNo(commentBeanList.size());
            commentBeanList.add(0, commentDetailBean);
            notifyDataSetChanged();
        } else {
            throw new IllegalArgumentException("评论数据为空!");
        }

    }

    /**
     * func:回复成功后插入一条数据
     *
     * @param replyDetailBean 新的回复数据
     */
    public void addTheReplyData(ReplyDetailBean replyDetailBean, int groupPosition) {
        if (replyDetailBean != null) {
            CommentDetailBean bean = commentBeanList.get(groupPosition);
            if (bean.getReplyList() != null) {
                replyDetailBean.setNo(bean.getReplyList().size());
                bean.getReplyList().add(replyDetailBean);
            } else {
                replyDetailBean.setNo(0);
                List<ReplyDetailBean> replyList = new ArrayList<>();
                replyList.add(replyDetailBean);
                bean.setReplyList(replyList);
            }
            notifyDataSetChanged();
        } else {
            throw new IllegalArgumentException("回复数据为空!");
        }

    }

    public void addReplyDataTofirst(ReplyDetailBean replyDetailBean, int groupPosition) {
        if (replyDetailBean != null) {
            CommentDetailBean bean = commentBeanList.get(groupPosition);
            if (bean.getReplyList() != null) {
                replyDetailBean.setNo(bean.getReplyList().size());
                bean.getReplyList().add(0, replyDetailBean);
            } else {
                replyDetailBean.setNo(0);
                List<ReplyDetailBean> replyList = new ArrayList<>();
                replyList.add(replyDetailBean);
                bean.setReplyList(replyList);
            }
            notifyDataSetChanged();
        } else {
            throw new IllegalArgumentException("回复数据为空!");
        }

    }



    /**
     * by moos on 2018/04/20
     * func:添加和展示所有回复
     *
     * @param replyBeanList 所有回复数据
     * @param groupPosition 当前的评论
     */
    private void addReplyList(List<ReplyDetailBean> replyBeanList, int groupPosition) {
        if (commentBeanList.get(groupPosition).getReplyList() != null) {
            commentBeanList.get(groupPosition).getReplyList().clear();
            commentBeanList.get(groupPosition).getReplyList().addAll(replyBeanList);
        } else {

            commentBeanList.get(groupPosition).setReplyList(replyBeanList);
        }

        notifyDataSetChanged();
    }

    public void clearCache(){

    }

}
