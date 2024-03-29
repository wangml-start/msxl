package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.RequiresApi;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.KeyBoardBottomSheetDialog;
import com.cgmn.msxl.comp.adpter.CommentExpandAdapter;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.CommentBean;
import com.cgmn.msxl.data.CommentDetailBean;
import com.cgmn.msxl.data.ReplyDetailBean;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.*;

public class DisgussSubActivity extends DisgussBaseActivity {
    private CommentDetailBean comment;
    private static final String TAG = "DisgussSubActivity";

    private NetImageView comment_item_logo, comment_picture;
    private TextView userName, time, content, comment_total, approve_total;

    private int currentSelectedPosition = -1;
    private int subPosition = -1;
    private int replayUserId = -1;
    private int recordId = 0;

    @Override
    protected int getView() {
        return R.layout.disguss_sub;
    }

    @Override
    protected void init() {
        bindView();
        initMessageHandle();
        if(recordId > 0){
            loadCommentInfo();
        }else{
            starLoadChatList();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCustmerClick(View view) {
        if (view.getId() == R.id.dialog_comment_bt) {
            if (!TextUtils.isEmpty(editCommet)) {
                dialog.dismiss();
                replayComment();
            } else {
                CustmerToast.makeText(mContext, "评论内容不能为空").show();
            }
        } else if (view.getId() == R.id.dialog_comment_pic) {
            onSelectFromPho();
        } else if (view.getId() == R.id.detail_page_do_comment) {
            currentSelectedPosition = -1;
            subPosition = -1;
            replayUserId = -1;
            showReplyDialog();
        } else if (view.getId() == R.id.img_back) {
            finish();
        }
    }

    private void bindView() {
        comment_item_logo = findViewById(R.id.comment_item_logo);
        comment_picture = findViewById(R.id.comment_picture);
        userName = findViewById(R.id.userName);
        time = findViewById(R.id.time);
        content = findViewById(R.id.content);
        comment_total = findViewById(R.id.comment_total);
        approve_total = findViewById(R.id.approve_total);
        comment = (CommentDetailBean) GlobalDataHelper.getData("comment");
        GlobalDataHelper.removeKey("comment");
        setHeader();
        Object obj = GlobalDataHelper.getData("viewId");
        GlobalDataHelper.removeKey("viewId");
        if(!CommonUtil.isEmpty(obj)){
            recordId = (int) obj;
        }
    }

    private void setHeader(){
        if (comment != null) {
            if (comment.getUserLogo() != null) {
                comment_item_logo.setImageContent(comment.getUserLogo());
            } else {
                comment_item_logo.setImageResource(R.drawable.user_logo);
            }
            if (comment.getPicture() != null) {
                comment_picture.setImageContent(comment.getPicture());
                comment_picture.setVisibility(View.VISIBLE);
            }
            userName.setText(comment.getNickName());
            time.setText(comment.getCreateDate());
            content.setText(comment.getContent());
            approve_total.setText("赞 " + comment.getApprove());
            if (comment.getReplyList() != null) {
                comment_total.setText("评论 " + comment.getReplyList().size());
            }
        }
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.LOAD_REPLAY_LIST) {
                    commentsList.clear();
                    CommentBean commentBean = new CommentBean((BaseData) msg.obj, commentsList.size());
                    commentBean.getList(commentsList);
                    if (adapter == null) {
                        initExpandableListView();
                    }
                    expandList();
                    scrollView.stopRefresh();
                    adapter.notifyDataSetChanged();
                    comment_total.setText("评论 " + commentsList.size());
                } else if (msg.what == MessageUtil.APPEND_LOAD_COMMENT_LIST) {
                    CommentBean commentBean = new CommentBean((BaseData) msg.obj, commentsList.size());
                    commentBean.getList(commentsList);
                    adapter.notifyDataSetChanged();
                    expandList();
                    appendList = false;
                    comment_total.setText("评论 " + commentsList.size());
                } else if(msg.what == MessageUtil.LOAD_COMMENT_INFO){
                    CommentBean commentBean = new CommentBean((BaseData) msg.obj, 0);
                    comment = commentBean.getFirst();
                    loadReplayList();
                    setHeader();
                }else if (msg.what == MessageUtil.PUBLISHED_COMMENT) {
                    loadReplayList();
                    resetDialog();
                    scrollView.setScrollY(0);
                } else if (msg.what == MessageUtil.PUBLISHED_REPLAY_COMMENT) {
                    String userName = GlobalDataHelper.getUserName(mContext);
                    ReplyDetailBean detailBean = new ReplyDetailBean(editCommet);
                    detailBean.setReplayFrom(userName);
                    detailBean.setId((Integer) msg.obj);
                    detailBean.setUserId(GlobalDataHelper.getUserId(mContext));
                    detailBean.setPicture(pictures);
                    if(subPosition >= 0){
                        detailBean.setReplayTo(commentsList.get(currentSelectedPosition).getReplyList().get(subPosition).getReplayFrom());
                    }
                    adapter.addReplyDataToEnd(detailBean, currentSelectedPosition);
                    expandableListView.expandGroup(currentSelectedPosition);
                    adapter.notifyDataSetChanged();
                    resetDialog();
                } else if (msg.what == MessageUtil.DELETED_COMMENT) {
                    CustmerToast.makeText(mContext, "删除成功").show();
                    loadReplayList();

                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }


    private void initExpandableListView() {
        expandableListView.setGroupIndicator(null);
        //默认展开所有回复
        adapter = new CommentExpandAdapter(mContext, commentsList);
        adapter.setCommentListener(this);
        adapter.setExpandAll(true);
        expandableListView.setAdapter(adapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                return true;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                return true;
            }
        });

        expandList();
    }

    private void showReplyDialog() {
        dialog = new KeyBoardBottomSheetDialog(mContext, R.style.BottomSheetStyle);
        if (commentView == null) {
            commentView = LayoutInflater.from(mContext).inflate(R.layout.comment_dialog_layout, null);
        }
        if (commentView.getParent() != null) {
            ((ViewGroup) commentView.getParent()).removeView(commentView);
        }
        final EditText commentText = (EditText) commentView.findViewById(R.id.dialog_comment_et);
        final Button bt_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
        final Button dialog_comment_pic = commentView.findViewById(R.id.dialog_comment_pic);
        autoShowkeyboard(commentText);
        String text = null;
        if (currentSelectedPosition < 0) {
            text = String.format("回复%s的评论:", comment.getNickName());
        } else {
            CommentDetailBean bean = commentsList.get(currentSelectedPosition);
            if (subPosition < 0) {
                text = String.format("回复%s的评论:", bean.getNickName());
            } else {
                text = String.format("回复%s的评论:", bean.getReplyList().get(subPosition).getReplayUserName(replayUserId));
            }
        }
        commentText.setHint(text);
        dialog.setContentView(commentView);
        invalidCommentView();

        bt_comment.setOnClickListener(clickListener);
        dialog_comment_pic.setOnClickListener(clickListener);
        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editCommet = commentText.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
    }

    private void loadReplayList() {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                params.put("comment_id", comment.getId() + "");
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/chat/query_replay_list", params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.LOAD_REPLAY_LIST;
                                try {
                                    message.obj = data;
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    }
                                } catch (Exception e) {
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                }
                                mHandler.sendMessage(message);
                            }
                        });
            }
        });
    }

    private void loadCommentInfo(){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                params.put("view_record_id", recordId + "");
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/chat/query_comment_info", params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.LOAD_COMMENT_INFO;
                                try {
                                    message.obj = data;
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    }
                                } catch (Exception e) {
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                }
                                mHandler.sendMessage(message);
                            }
                        });
            }
        });
    }

    private void appendreplayComment() {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("start", (commentsList.size()) + "");
                params.put("comment_id", comment.getId() + "");
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/chat/query_replay_list", params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.APPEND_LOAD_COMMENT_LIST;
                                try {
                                    message.obj = data;
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    }
                                } catch (Exception e) {
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                }
                                mHandler.sendMessage(message);
                            }
                        });
            }
        });
    }

    /**
     * 回复评论
     *
     * @param
     */
    private void replayComment() {
        CustmerToast.makeText(mContext, "正在回复。。。").show();
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                final String token = GlobalDataHelper.getToken(mContext);
                if (pictures == null) {
                    pictures = new byte[]{};
                }

                Integer beReplayUserId = null;
                Integer replayId = null;
                if (currentSelectedPosition < 0) { //最外层的回复
                    beReplayUserId = comment.getUserId();
                    replayId = comment.getId();
                } else {
                    CommentDetailBean bean = commentsList.get(currentSelectedPosition);
                    if (subPosition < 0) { //评论回复
                        beReplayUserId = bean.getUserId();
                        replayId = bean.getId();
                    } else {  //评论下的用户回复
                        beReplayUserId = replayUserId;
                        replayId = bean.getReplyList().get(subPosition).getId();
                    }
                }
                Integer commentId = comment.getId();
                if (currentSelectedPosition >= 0) {
                    commentId = commentsList.get(currentSelectedPosition).getId();
                }
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", token),
                        new OkHttpClientManager.Param("picture", org.apache.shiro.codec.Base64.encodeToString(pictures)),
                        new OkHttpClientManager.Param("comment", editCommet),
                        new OkHttpClientManager.Param("comment_id", commentId + ""),
                        new OkHttpClientManager.Param("replay_id", replayId + ""),
                        new OkHttpClientManager.Param("comment_user_id", beReplayUserId + "")
                };
                String url = String.format("%s%s",
                        ConstantHelper.serverUrl, "/chat/replay_comment");
                OkHttpClientManager.postAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                if (currentSelectedPosition < 0) {
                                    message.what = MessageUtil.PUBLISHED_COMMENT;
                                } else {
                                    message.what = MessageUtil.PUBLISHED_REPLAY_COMMENT;
                                }
                                try {
                                    message.obj = data.getRecordId();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    }
                                } catch (Exception e) {
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                }
                                mHandler.sendMessage(message);
                            }
                        }, params);
            }
        });
    }

    @Override
    public void onCommentClick(Integer position) {
        currentSelectedPosition = position;
        subPosition = -1;
        showReplyDialog();
    }

    @Override
    public void onChildReplayClick(Integer position, Integer childPos, Integer replayId) {
        currentSelectedPosition = position;
        subPosition = childPos;
        replayUserId = replayId;
        showReplyDialog();
    }

    @Override
    public void startRefresh() {
        loadReplayList();
    }

    @Override
    public void loadMore() {
        if (!appendList) {
            appendList = true;
            appendreplayComment();
        }
    }

}
