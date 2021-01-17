package com.cgmn.msxl.data;

import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.server_interface.ChatAddRecord;
import com.cgmn.msxl.utils.CommonUtil;
import org.apache.shiro.codec.Base64;

import java.util.*;

public class CommentBean {
    private List<CommentDetailBean> commentList;


    public CommentBean(BaseData data, Integer baseIndex) {
        commentList = new ArrayList<>();
        try {
            if(data.getChatMain() == null){
                return;
            }
            Map<Integer, List<ChatAddRecord>> subMap = new HashMap<>();
            for(ChatAddRecord sub : data.getChatSub()){
                Integer pId = sub.getParentId();
                if(!subMap.containsKey(pId)){
                    subMap.put(pId, new ArrayList<ChatAddRecord>());
                }
                subMap.get(pId).add(sub);
            }
            Integer index = baseIndex;
            for (ChatAddRecord item : data.getChatMain()) {
                CommentDetailBean bean = analysisComment(item, subMap);
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
            format = "yyyy-MM-dd HH:mm";
        }

        return CommonUtil.formartTimeString(date, format);
    }

    private CommentDetailBean analysisComment(ChatAddRecord attr, Map<Integer, List<ChatAddRecord>> subMap) {
        String userName = attr.getUserName();
        String content = attr.getContent();
        String date = analysisTime(CommonUtil.parseDateString(attr.getCreatedAt(), "yyyyMMdd HH:mm:ss"));
        CommentDetailBean comment = new CommentDetailBean(userName, content, date);
        comment.setPhone(attr.getPhone());
        comment.setId(attr.getId());
        comment.setUserId(attr.getCreatorId());
        comment.setApprove(attr.getApprove()+"");
        if(CommonUtil.isEmpty(attr.getMyApprove())){
            comment.setMyApprove(0);
        }else {
            comment.setMyApprove(attr.getMyApprove());
        }
        if(CommonUtil.isEmpty(attr.getMyComment())){
            comment.setMyComment(0);
        }else {
            comment.setMyComment(attr.getMyComment());
        }
        comment.setUserId(attr.getCreatorId());
        if(!CommonUtil.isEmpty(attr.getBitContent())){
            comment.setPicture(Base64.decode(attr.getBitContent()));
        }
        if(!CommonUtil.isEmpty(attr.getSmallCut())){
            comment.setUserLogo(Base64.decode(attr.getSmallCut()));
        }

        //解析回复
        if(!CommonUtil.isEmpty(subMap) && subMap.containsKey(attr.getId())){
            List<ChatAddRecord> replay = subMap.get(attr.getId());
            comment.setReplyTotal(replay.size());
            List<ReplyDetailBean> reList = new ArrayList<>();
            comment.setReplyList(reList);
            Integer index = 0;
            for (ChatAddRecord item : replay) {
                Integer replayUserId = 0;
                Integer replayId = 0;
                if(item.getReplyUserId() != null){
                    replayUserId = item.getReplyUserId();
                }
                if(item.getReplyId() != null){
                    replayId = item.getReplyId();
                }
                String recontent = item.getContent();
                ReplyDetailBean replyDetailBean = new ReplyDetailBean(recontent);
                replyDetailBean.setReplayFrom(item.getUserName());
                if(replayId != comment.getId()){
                    replyDetailBean.setReplayTo(item.getReplayUname());
                }
                replyDetailBean.setId(item.getId());
                replyDetailBean.setNo(index++);
                replyDetailBean.setUserId(item.getCreatorId());
                replyDetailBean.setReplayUserId(replayUserId);
                if(!CommonUtil.isEmpty(item.getBitContent())){
                    replyDetailBean.setPicture(Base64.decode(item.getBitContent()));
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
