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
    private Map<Integer, View> views = null;
    private Map<String, View> subViews = null;
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
        views = new HashMap<>();
        subViews = new HashMap<>();
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
        if (!views.containsKey(bean.getNo())) {
            convertView = LayoutInflater.from(context).inflate(R.layout.comment_item_layout, viewGroup, false);
            final GroupHolder groupHolder = new GroupHolder(convertView);
            views.put(bean.getNo(), convertView);

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
                    } else if(view.getId() == R.id.content_view){
                        commentListener.onShowPicture(bean.getPicture(), null);
                    } else if(view.getId() == R.id.comment_item_logo){
                        commentListener.onShowPicture(null, bean.getPhone());
                    }
                }
            };
            //init content
            //head
            if (bean.getUserLogo() != null && bean.getUserLogo().length > 0) {
                groupHolder.logo.setImageContent(bean.getUserLogo());
            } else {
                Glide.with(context).load(R.drawable.user_logo)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop()
                        .into(groupHolder.logo);
            }
            groupHolder.tv_name.setText(bean.getNickName());
            groupHolder.tv_time.setText(bean.getCreateDate());

            //content
            if(!expandAllContent && bean.getContent() != null && bean.getContent().length() > allContentLength){
                groupHolder.tv_content.setText(packUpContent(bean.getContent(), groupPosition));
                groupHolder.tv_content.setMovementMethod(LinkMovementMethod.getInstance());
            }else{
                groupHolder.tv_content.setText(bean.getContent());
            }

            if (bean.getPicture() != null && bean.getPicture().length > 0) {
                View picContent  = LayoutInflater.from(context).inflate(R.layout.content_picture, viewGroup, false);
                NetImageView comment_picture = (NetImageView) picContent;
                groupHolder.content_view.addView(picContent);
                comment_picture.setImageContent(bean.getPicture());
                groupHolder.content_view.setOnClickListener(listener);
            }

            groupHolder.comment_num.setText("");
            if(bean.getReplyList() != null && bean.getReplyList().size() > 0){
                groupHolder.comment_num.setText(bean.getReplyList().size()+"");
            }
            if (!"0".equals(bean.getApprove())) {
                groupHolder.comment_approve.setText(bean.getApprove());
            }
            if (bean.getMyApprove() == 1) {
                groupHolder.iv_like.setImageResource(R.drawable.liked);
            }
            groupHolder.iv_like.setOnClickListener(listener);
            groupHolder.comment_icon.setOnClickListener(listener);
            groupHolder.comment_setting.setOnClickListener(listener);
            groupHolder.logo.setOnClickListener(listener);

        } else {
            convertView = views.get(bean.getNo());
        }

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean b, View convertView, ViewGroup viewGroup) {
        if(!expandAll && childPosition >= expandNum){
            convertView = LayoutInflater.from(context).inflate(R.layout.show_more_item, viewGroup, false);
            TextView more = convertView.findViewById(R.id.show_more);
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentListener.onShowMoreClick(groupPosition);
                }
            });
            return convertView;
        }else{
            final CommentDetailBean bean = commentBeanList.get(groupPosition);
            final ReplyDetailBean subbean = bean.getReplyList().get(childPosition);
            String key = String.format("%s-%s", bean.getNo(), subbean.getNo());
            if (!subViews.containsKey(key)) {
                convertView = LayoutInflater.from(context).inflate(R.layout.comment_reply_item_layout, viewGroup, false);
                final ChildHolder childHolder = new ChildHolder(convertView);
                subViews.put(key, convertView);

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getId() == R.id.txt_reply_from){
                            commentListener.onChildReplayClick(groupPosition, childPosition, subbean.getUserId());
                        } else if(view.getId() == R.id.txt_reply_to){
                            commentListener.onChildReplayClick(groupPosition, childPosition, subbean.getReplayUserId());
                        }
                    }
                };

                childHolder.txt_reply_from.setText(subbean.getReplayFrom());
                childHolder.txt_reply_from.setOnClickListener(listener);
                if(!CommonUtil.isEmpty(subbean.getReplayTo())){
                    childHolder.txt_reply.setVisibility(View.VISIBLE);
                    childHolder.txt_reply_to.setVisibility(View.VISIBLE);
                    childHolder.txt_reply_to.setText(subbean.getReplayTo());
                    childHolder.txt_reply_to.setOnClickListener(listener);
                }
                String content = commentBeanList.get(groupPosition).getReplyList().get(childPosition).getContent();
                if(subbean.getPicture() != null && subbean.getPicture().length > 0){
                    childHolder.tv_content.setText(packUpReplayContent(content, subbean.getPicture()));
                    childHolder.tv_content.setMovementMethod(LinkMovementMethod.getInstance());
                }else{
                    childHolder.tv_content.setText(content);
                }
            } else {
                convertView = subViews.get(key);
            }
            return convertView;
        }
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    private class GroupHolder {
        private NetImageView logo;
        private TextView tv_name, tv_content, tv_time, comment_approve,comment_setting,comment_num;
        private ImageView iv_like, comment_icon;
        private LinearLayout content_view;

        public GroupHolder(View view) {
            logo = (NetImageView) view.findViewById(R.id.comment_item_logo);
            tv_content = (TextView) view.findViewById(R.id.comment_item_content);
            tv_name = (TextView) view.findViewById(R.id.comment_item_userName);
            tv_time = (TextView) view.findViewById(R.id.comment_item_time);
            iv_like = (ImageView) view.findViewById(R.id.comment_item_like);
            comment_icon = (ImageView) view.findViewById(R.id.comment_icon);
            comment_approve = (TextView) view.findViewById(R.id.comment_approve);
            comment_setting = (TextView) view.findViewById(R.id.comment_setting);
            comment_num = (TextView) view.findViewById(R.id.comment_num);
            content_view = view.findViewById(R.id.content_view);
        }
    }

    private class ChildHolder {
        private TextView txt_reply_from,txt_reply,txt_reply_to, tv_content;

        public ChildHolder(View view) {
            txt_reply_from = (TextView) view.findViewById(R.id.txt_reply_from);
            txt_reply = (TextView) view.findViewById(R.id.txt_reply);
            txt_reply_to = (TextView) view.findViewById(R.id.txt_reply_to);
            tv_content = (TextView) view.findViewById(R.id.reply_item_content);
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
        span.setSpan(new UnderlineSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor(color));
                ds.setUnderlineText(false);// 去掉下划线
            }
        }, contentText.length()-5, contentText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return span;
    }

    @SuppressLint("ResourceAsColor")
    private SpannableString packUpReplayContent(String content, final byte[] bs){
        if(content == null){
            content = "";
        }
        StringBuffer contentText = new StringBuffer(content);
        contentText.append("   查看图片");
        SpannableString span = new SpannableString(contentText.toString());
        span.setSpan(new ClickableSpan(){
            @Override
            public void onClick(View widget) {
                commentListener.onShowPicture(bs, null);
            }
        }, contentText.length()-4, contentText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new UnderlineSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor(color));
                ds.setUnderlineText(false);// 去掉下划线
            }
        }, contentText.length()-4, contentText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        Drawable d = context.getResources().getDrawable(R.drawable.photo);
        d.setColorFilter(Color.parseColor(color), PorterDuff.Mode.MULTIPLY);
        d.setBounds(0, 0, 60, 60);
        //8.创建ImageSpan,然后用ImageSpan来替换文本
        ImageSpan imgspan = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
        span.setSpan(imgspan, contentText.length()-6, contentText.length()-5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

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
            views.clear(); //重新绑定事件
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
        views.clear();
        subViews.clear();
    }

}
