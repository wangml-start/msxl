package com.cgmn.msxl.ac;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.adpter.CommentExpandAdapter;
import com.cgmn.msxl.comp.view.CommentExpandableListView;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.comp.view.RefreshScrollView;
import com.cgmn.msxl.data.CommentBean;
import com.cgmn.msxl.data.CommentDetailBean;
import com.cgmn.msxl.data.ReplyDetailBean;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.in.CommentListener;
import com.cgmn.msxl.in.RefreshListener;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.*;

public class DisgussSubActivity extends BaseOtherActivity
        implements CommentListener,View.OnClickListener, RefreshListener {
    private CommentDetailBean comment;
    private static final String TAG = "DisgussSubActivity";

    private RelativeLayout headView;
    private NetImageView comment_item_logo, comment_picture;
    private TextView userName, time, content,detail_page_do_comment,
            comment_total,approve_total, head_view_tv;

    private RefreshScrollView scrollView;
    private CommentExpandableListView sub_list_comment;
    private CommentExpandAdapter adapter;
    private BottomSheetDialog dialog;

    //消息处理
    private Handler mHandler;
    private List<CommentDetailBean> commentsList = new ArrayList<>();

    private static final int REQUEST_IMAGE_GET = 0;
    private View commentView = null;
    private View imageView = null;
    private byte[] pictures = null;
    private String editCommet;
    private int currentSelectedPosition=-1;
    private int subPosition=-1;
    private boolean appendList = false;

    @Override
    protected int getContentView() {
        return R.layout.disguss_sub;
    }

    @Override
    protected void init() {
        bindView();
        initMessageHandle();
        loadReplayList();
    }

    private void bindView(){
        comment_item_logo = findViewById(R.id.comment_item_logo);
        comment_picture = findViewById(R.id.comment_picture);
        userName = findViewById(R.id.userName);
        time = findViewById(R.id.time);
        content = findViewById(R.id.content);
        detail_page_do_comment = findViewById(R.id.detail_page_do_comment);
        scrollView = findViewById(R.id.scrollView);
        sub_list_comment = findViewById(R.id.sub_list_comment);
        comment_total = findViewById(R.id.comment_total);
        approve_total = findViewById(R.id.approve_total);

        detail_page_do_comment.setOnClickListener(this);
        headView = (RelativeLayout) findViewById(R.id.head_view);
        head_view_tv = (TextView) findViewById(R.id.head_view_tv);
        scrollView.setListsner(this);
        scrollView.setHeadView(headView);

        comment  = (CommentDetailBean) GlobalDataHelper.getDate("comment");
        if(comment != null){
            if(comment.getUserLogo() != null){
                comment_item_logo.setImageContent(comment.getUserLogo());
            }else{
                Glide.with(mContext).load(R.drawable.user_logo)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop()
                        .into(comment_item_logo);
            }
            if(comment.getPicture() != null){
                comment_picture.setImageContent(comment.getPicture());
                comment_picture.setVisibility(View.VISIBLE);
            }
            userName.setText(comment.getNickName());
            time.setText(comment.getCreateDate());
            content.setText(comment.getContent());
            approve_total.setText("赞 "+comment.getApprove());
            if(comment.getReplyList() != null){
                comment_total.setText("评论 "+comment.getReplyList().size());
            }
        }
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.LOAD_REPLAY_LIST) {
                    commentsList.clear();
                    CommentBean commentBean = new CommentBean(msg.obj, commentsList.size());
                    commentBean.getList(commentsList);
                    if(adapter == null){
                        initExpandableListView();
                    }
                    scrollView.stopRefresh();
                    adapter.clearCache();
                    adapter.notifyDataSetChanged();
                    comment_total.setText("评论 "+ commentsList.size());
                } else if(msg.what == MessageUtil.APPEND_LOAD_COMMENT_LIST){
                    CommentBean commentBean = new CommentBean(msg.obj, commentsList.size());
                    commentBean.getList(commentsList);
                    adapter.notifyDataSetChanged();
                    expandList();
                    appendList = false;
                    comment_total.setText("评论 "+ commentsList.size());
                } else if (msg.what == MessageUtil.PUBLISHED_COMMENT) {
                    String userName = GlobalDataHelper.getUserName(mContext);
                    String time = CommentBean.analysisTime(new Date());
                    CommentDetailBean detailBean = new CommentDetailBean(userName, editCommet, time);
                    detailBean.setPicture(pictures);
                    detailBean.setId((Integer) msg.obj);
                    detailBean.setUserId(GlobalDataHelper.getUserId(mContext));
                    byte[] cut = GlobalDataHelper.getUserCut(mContext);
                    if(cut != null && cut.length > 0){
                        detailBean.setUserLogo(cut);
                    }
                    adapter.addTheCommentData(detailBean);
                    resetDialog();
                    scrollView.setScrollY(0);
                } else if(msg.what == MessageUtil.PUBLISHED_REPLAY_COMMENT){
                    String userName = GlobalDataHelper.getUserName(mContext);
                    ReplyDetailBean detailBean = new ReplyDetailBean(userName, editCommet);
                    detailBean.setId((Integer) msg.obj);
                    detailBean.setUserId(GlobalDataHelper.getUserId(mContext));
                    adapter.addTheReplyData(detailBean, currentSelectedPosition);
                    sub_list_comment.expandGroup(currentSelectedPosition);
                    resetDialog();
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    private void resetDialog(){
        commentView = null;
        imageView = null;
        pictures = null;
        editCommet = null;
    }

    private void expandList(){
        for (int i = 0; i < commentsList.size(); i++) {
            sub_list_comment.expandGroup(i);
        }
    }

    private void initExpandableListView() {
        sub_list_comment.setGroupIndicator(null);
        //默认展开所有回复
        adapter = new CommentExpandAdapter(mContext, commentsList);
        adapter.setCommentListener(this);
        adapter.setExpandAll(true);
        sub_list_comment.setAdapter(adapter);
        expandList();
        sub_list_comment.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                currentSelectedPosition = groupPosition;
                subPosition = -1;
                return true;
            }
        });

        sub_list_comment.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                currentSelectedPosition = groupPosition;
                subPosition = childPosition;
                showReplyDialog();
                return false;
            }
        });
    }

    private void showReplyDialog() {
        dialog = new BottomSheetDialog(mContext);
        if (commentView == null) {
            commentView = LayoutInflater.from(mContext).inflate(R.layout.comment_dialog_layout, null);
        }
        if (commentView.getParent() != null) {
            ((ViewGroup) commentView.getParent()).removeView(commentView);
        }
        final EditText commentText = (EditText) commentView.findViewById(R.id.dialog_comment_et);
        final Button bt_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
        final Button dialog_comment_pic = commentView.findViewById(R.id.dialog_comment_pic);
        String text=null;
        if(currentSelectedPosition < 0){
            text = String.format("回复%s的评论:", comment.getNickName());
        }else {
            CommentDetailBean bean = commentsList.get(currentSelectedPosition);
            if(subPosition < 0){
                text = String.format("回复%s的评论:", bean.getNickName());
            }else {
                text = String.format("回复%s的评论:", bean.getReplyList().get(subPosition).getNickName());
            }
        }
        commentText.setHint(text);
        dialog.setContentView(commentView);
        invalidCommentView();

        bt_comment.setOnClickListener(this);
        dialog_comment_pic.setOnClickListener(this);
        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && charSequence.length() > 1) {
                    bt_comment.setBackgroundColor(Color.parseColor("#FFB568"));
                } else {
                    bt_comment.setBackgroundColor(Color.parseColor("#D8D8D8"));
                }
                editCommet = commentText.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
    }

    private void loadReplayList(){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                params.put("comment_id", comment.getId()+"");
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
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
                                    message.obj = data.getRecords();
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
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
            }
        });
    }

    private void appendreplayComment(){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("start", (commentsList.size())+"");
                params.put("comment_id", comment.getId()+"");
                params.put("token", GlobalDataHelper.getToken(mContext));
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
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
                                    message.obj = data.getRecords();
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
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
            }
        });
    }

    private void invalidCommentView() {
        /**
         * 解决bsd显示不全的情况
         */
        View parent = (View) commentView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        commentView.measure(0, 0);
        behavior.setPeekHeight(commentView.getMeasuredHeight());
    }

    /**
     * 回复评论
     * @param
     */
    private void replayComment(){
        CustmerToast.makeText(mContext, "正在回复。。。").show();
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                final String token = GlobalDataHelper.getToken(mContext);
                if(pictures == null){
                    pictures = new byte[]{};
                }
                Integer beReplayUserId=null;
                if(currentSelectedPosition < 0){
                    beReplayUserId = comment.getUserId();
                }else {
                    CommentDetailBean bean = commentsList.get(currentSelectedPosition);
                    if(subPosition < 0){
                        beReplayUserId = bean.getUserId();
                    }else {
                        beReplayUserId = bean.getReplyList().get(subPosition).getUserId();
                    }
                }
                Integer commentId = comment.getId();
                if(currentSelectedPosition > 0){
                    commentId = commentsList.get(currentSelectedPosition).getId();
                }
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", token),
                        new OkHttpClientManager.Param("picture", org.apache.shiro.codec.Base64.encodeToString(pictures)),
                        new OkHttpClientManager.Param("comment", editCommet),
                        new OkHttpClientManager.Param("comment_id", commentId+""),
                        new OkHttpClientManager.Param("comment_user_id", beReplayUserId+"")
                };
                String url = String.format("%s%s",
                        PropertyService.getInstance().getKey("serverUrl"), "/chat/replay_comment");
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
                                if(currentSelectedPosition < 0){
                                    message.what = MessageUtil.PUBLISHED_COMMENT;
                                }else {
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
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onSelectFromPho() {
        if (imageView != null) {
            TextView dialog_comment_dis = commentView.findViewById(R.id.dialog_comment_dis);
            dialog_comment_dis.setText("只能添加一张图片");
            return;
        }
        // 权限申请
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //权限还没有授予，需要在这里写申请权限的代码
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            // 如果权限已经申请过，直接进行图片选择
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            // 判断系统中是否有处理该 Intent 的 Activity
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                CustmerToast.makeText(mContext, "未找到图片查看器").show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.dialog_comment_bt) {
            if (!TextUtils.isEmpty(editCommet)) {
                dialog.dismiss();
                replayComment();
            } else {
                CustmerToast.makeText(mContext, "评论内容不能为空").show();
            }
        } else if (view.getId() == R.id.dialog_comment_pic) {
            onSelectFromPho();
        } else if(view.getId() == R.id.detail_page_do_comment){
            currentSelectedPosition = -1;
            subPosition = -1;
            showReplyDialog();
        }
    }

    @Override
    public void onApproveClick(final Integer position, final String type) {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                params.put("type", type);
                params.put("comment_id", commentsList.get(position).getId()+"");
                params.put("comment_user_id", commentsList.get(position).getUserId()+"");

                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        "/chat/approve_comment", params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                            }

                            @Override
                            public void onResponse(BaseData data) {
                            }
                        });
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
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
    public void startRefresh() {
        loadReplayList();
    }

    @Override
    public void loadMore() {
        if(!appendList){
            appendList = true;
            appendreplayComment();
        }
    }

    @Override
    public void hintChange(String hint) {

    }
}
