package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Moos
 * E-mail: moosphon@gmail.com
 * Date:  18/4/20.
 * Desc: 评论与回复列表的适配器
 */

public class CommentExpandAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "CommentExpandAdapter";
    private List<CommentDetailBean> commentBeanList;
    private List<ReplyDetailBean> replyBeanList;
    private Context context;
    private Map<Integer, View> views = null;
    private Map<String, View> subViews = null;
    private CommentListener commentListener;
    private boolean expandAll = false;
    private Integer expandNum = 2;
    private boolean expandAllContent = true;
    private Integer allContentLength = 150;

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
                    }else if(view.getId() == R.id.show_more){
                        commentListener.onShowMoreClick(groupPosition);
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
                groupHolder.tv_content.setText(bean.getContent().substring(0, allContentLength) + "...");
                View moreContent = LayoutInflater.from(context).inflate(R.layout.show_more_content, viewGroup, false);
                TextView more = (TextView) moreContent;
                more.setOnClickListener(listener);
                groupHolder.content_view.addView(moreContent);
            }else{
                groupHolder.tv_content.setText(bean.getContent());
            }

            if (bean.getPicture() != null && bean.getPicture().length > 0) {
                View picContent  = LayoutInflater.from(context).inflate(R.layout.content_picture, viewGroup, false);
                NetImageView comment_picture = (NetImageView) picContent;
                groupHolder.content_view.addView(picContent);
                comment_picture.setImageContent(bean.getPicture());
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
            if(bean.getMyComment() == 1){
                groupHolder.comment_setting.setVisibility(View.VISIBLE);
                groupHolder.comment_setting.setOnClickListener(listener);
            }

        } else {
            convertView = views.get(bean.getNo());
        }

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean b, View convertView, ViewGroup viewGroup) {
        if(!expandAll && childPosition >= expandNum){
            convertView = LayoutInflater.from(context).inflate(R.layout.show_more_item, viewGroup, false);
            return convertView;
        }else{
            CommentDetailBean bean = commentBeanList.get(groupPosition);
            ReplyDetailBean subbean = bean.getReplyList().get(childPosition);
            String key = String.format("%s-%s", bean.getNo(), subbean.getNo());
            if (!subViews.containsKey(key)) {
                convertView = LayoutInflater.from(context).inflate(R.layout.comment_reply_item_layout, viewGroup, false);
                final ChildHolder childHolder = new ChildHolder(convertView);
                String replyUser = subbean.getNickName();
                subViews.put(key, convertView);
                if (!TextUtils.isEmpty(replyUser)) {
                    childHolder.tv_name.setText(replyUser + ":");
                } else {
                    childHolder.tv_name.setText("无名" + ":");
                }
                childHolder.tv_content.setText(commentBeanList.get(groupPosition).getReplyList().get(childPosition).getContent());
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
        private TextView tv_name, tv_content;

        public ChildHolder(View view) {
            tv_name = (TextView) view.findViewById(R.id.reply_item_user);
            tv_content = (TextView) view.findViewById(R.id.reply_item_content);
        }
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
