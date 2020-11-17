package com.cgmn.msxl.data;

import com.cgmn.msxl.utils.CommonUtil;
import org.apache.shiro.codec.Base64;

import java.util.*;

public class CommentBean {
    private List<CommentDetailBean> commentList;


    public CommentBean(Object list, Integer baseIndex) {
        commentList = new ArrayList<>();
        try {
            if(list == null){
                return;
            }
            List<Map<String, Object>> mList = (List<Map<String, Object>>) list;
            Integer index = baseIndex;
            for (Map<String, Object> item : mList) {
                CommentDetailBean bean = analysisComment(item);
                bean.setNo(index++);
                commentList.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String analysisTime(Date date) {
        if (date == null) {
            return "";
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        Calendar cNow = Calendar.getInstance();
        cNow.setTime(new Date());

        String format = null;
        if (c.get(Calendar.YEAR) == cNow.get(Calendar.YEAR)) {
            if (c.get(Calendar.DAY_OF_MONTH) == cNow.get(Calendar.DAY_OF_MONTH)) {
                format = "HH:mm";
            } else {
                format = "MM-dd HH:mm";
            }
        } else {
            format = "yyyy-MM-dd";
        }

        return CommonUtil.formartTimeString(date, format);
    }

    private CommentDetailBean analysisComment(Map<String, Object> attr) {
        String userName = (String) attr.get("user_name");
        String content = (String) attr.get("content");
        String date = analysisTime(CommonUtil.parseDateString((String) attr.get("created_at"), "yyyyMMdd HH:mm:ss"));
        CommentDetailBean comment = new CommentDetailBean(userName, content, date);
        comment.setId(((Double) attr.get("id")).intValue());
        comment.setUserId(((Double) attr.get("creator_id")).intValue());
        comment.setApprove(((Double) attr.get("approve")).intValue() +"");
        if(CommonUtil.isEmpty(attr.get("my_approve"))){
            comment.setMyApprove(0);
        }else {
            comment.setMyApprove(((Double) attr.get("my_approve")).intValue());
        }
        if(CommonUtil.isEmpty(attr.get("my_comment"))){
            comment.setMyComment(0);
        }else {
            comment.setMyComment(((Double) attr.get("my_comment")).intValue());
        }
        comment.setUserId(((Double) attr.get("creator_id")).intValue());
        if(!CommonUtil.isEmpty(attr.get("bit_content"))){
            comment.setPicture(Base64.decode((String) attr.get("bit_content")));
        }
        if(!CommonUtil.isEmpty(attr.get("small_cut"))){
            comment.setUserLogo(Base64.decode((String) attr.get("small_cut")));
        }

        //解析回复
        if(!CommonUtil.isEmpty(attr.get("replay"))){
            List<Map<String, Object>> replay = (List<Map<String, Object>>) attr.get("replay");
            comment.setReplyTotal(replay.size());
            List<ReplyDetailBean> reList = new ArrayList<>();
            comment.setReplyList(reList);
            Integer index = 0;
            for (Map<String, Object> item : replay) {
                Integer replayUserId = 0;
                if(item.get("reply_user_id") != null){
                    replayUserId = ((Double) item.get("reply_user_id")).intValue();
                }
                String recontent = (String) item.get("content");
                ReplyDetailBean replyDetailBean = new ReplyDetailBean(recontent);
                replyDetailBean.setReplayFrom((String) item.get("user_name"));
                if(replayUserId != comment.getUserId()){
                    replyDetailBean.setReplayTo((String) item.get("replay_uname"));
                }
                replyDetailBean.setNo(index++);
                replyDetailBean.setUserId(((Double) item.get("creator_id")).intValue());
                replyDetailBean.setReplayUserId(replayUserId);
                if(!CommonUtil.isEmpty(item.get("bit_content"))){
                    replyDetailBean.setPicture(Base64.decode((String) item.get("bit_content")));
                }

                reList.add(replyDetailBean);
            }
        }

        return comment;

    }

    public void getList(List<CommentDetailBean> tempList) {
        for (CommentDetailBean bean : commentList) {
            tempList.add(bean);
        }
    }

    public CommentDetailBean getFirst(){
        if(!CommonUtil.isEmpty(commentList)){
            return commentList.get(0);
        }

        return null;
    }
}
