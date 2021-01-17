package com.cgmn.msxl.ac;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.cgmn.msxl.comp.adpter.CommentExpandAdapter;
import com.cgmn.msxl.comp.view.NumImageView;
import com.cgmn.msxl.data.CommentBean;
import com.cgmn.msxl.data.CommentDetailBean;
import com.cgmn.msxl.data.ReplyDetailBean;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.*;

public class DisgussActivity extends DisgussBaseActivity {
    private static final String TAG = "DisgussActivity";
    private Context mContent;
    private int currentSelectedPosition;
    private TabLayout tabLayout;
    private NumImageView mail;
    private Integer tabPosition=0;
    private BaseData response;

    @Override
    protected int getView() {
        return R.layout.disguss_fragment;
    }

    @Override
    protected void init() {
        mContent = this;
        initView();
        initMessageHandle();
        loadCommentList();
        loadRaleatedTome();
    }

    @Override
    protected void onCustmerClick(View v){
        if (v.getId() == R.id.detail_page_do_comment) {
            showCommentDialog();
        } else if(v.getId() == R.id.img_mail){
            Intent intent = new Intent(mContext, RelatedToMeActivity.class);
//            GlobalDataHelper.setDate("relate", response);
            startActivity(intent);
            mail.setNum(0);
            response = null;
        } else if(v.getId() == R.id.img_back){
            finish();
        }
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.LOAD_COMMENT_LIST) {
                    commentsList.clear();
                    CommentBean commentBean = new CommentBean((BaseData) msg.obj, commentsList.size());
                    commentBean.getList(commentsList);
                    if(adapter == null){
                        initExpandableListView();
                    }
                    expandList();
                    adapter.notifyDataSetChanged();
                    scrollView.stopRefresh();
                    scrollView.setScrollY(0);
                } else if(msg.what == MessageUtil.APPEND_LOAD_COMMENT_LIST){
                    CommentBean commentBean = new CommentBean((BaseData) msg.obj, commentsList.size());
                    int baseSize = commentsList.size();
                    commentBean.getList(commentsList);
                    if(baseSize < commentsList.size()){
                        adapter.notifyDataSetChanged();
                        expandList();
                    }
                    appendList = false;
                }else if (msg.what == MessageUtil.PUBLISHED_COMMENT) {
                    loadCommentList();
                    resetDialog();
                    scrollView.setScrollY(0);
                } else if(msg.what == MessageUtil.PUBLISHED_REPLAY_COMMENT){
                    String userName = GlobalDataHelper.getUserName(mContent);
                    ReplyDetailBean detailBean = new ReplyDetailBean(editCommet);
                    detailBean.setReplayFrom(userName);
                    detailBean.setId((Integer) msg.obj);
                    detailBean.setUserId(GlobalDataHelper.getUserId(mContent));
                    detailBean.setPicture(pictures);
                    adapter.addReplyDataTofirst(detailBean, currentSelectedPosition);
                    expandableListView.expandGroup(currentSelectedPosition);
                    adapter.notifyDataSetChanged();
                    resetDialog();
                } else if (msg.what == MessageUtil.DELETED_COMMENT) {
                    CustmerToast.makeText(mContext, "删除成功").show();
                    loadCommentList();
                } else if(MessageUtil.LOAD_RELATED_TO_ME == msg.what){
                    response = (BaseData) msg.obj;
                    if(response != null){
                        mail.setNum(response.getApproveToMe()+response.getCommentToMe());
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContent).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    private void initView() {
        tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.addTab(tabLayout.newTab().setText("最新"));
        tabLayout.addTab(tabLayout.newTab().setText("热门"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            //选中的时候
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                scrollView.handleHeaderView(null, 100);
                hintChange("正在刷新");
                loadCommentList();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mail = findViewById(R.id.img_mail);
        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCustmerClick(v);
            }
        });
    }



    /**
     * 初始化评论和回复列表
     */
    private void initExpandableListView() {
        expandableListView.setGroupIndicator(null);
        //默认展开所有回复
        adapter = new CommentExpandAdapter(mContent, commentsList);
        adapter.setCommentListener(this);
        adapter.setExpandAllContent(false);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
//                jump2Sub(commentsList.get(groupPosition));
                return true;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
//                jump2Sub(commentsList.get(groupPosition));
                return true;
            }
        });

//        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                //toast("展开第"+groupPosition+"个分组");
//
//            }
//        });

    }

    private String getCommentType(){
        String type = "lastest";
        if(tabPosition == 1){
            type = "hot";
        }
        return type;
    }

    private void loadCommentList(){
        GlobalTreadPools.getInstance(mContent).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContent));
                params.put("load_type", getCommentType());
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        "/chat/query_comment_list", params);
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
                                message.what = MessageUtil.LOAD_COMMENT_LIST;
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

    private void appendCommentList(){
        GlobalTreadPools.getInstance(mContent).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("start", (commentsList.size())+"");
                params.put("token", GlobalDataHelper.getToken(mContent));
                params.put("load_type", getCommentType());
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        "/chat/query_comment_list", params);
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


    private void loadRaleatedTome(){
        GlobalTreadPools.getInstance(mContent).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContent));
                String url = CommonUtil.buildGetUrl(
                        PropertyService.getInstance().getKey("serverUrl"),
                        "/chat/related_to_me", params);
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
                                message.what = MessageUtil.LOAD_RELATED_TO_ME;
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
     * 上传评论
     */
    private void publishComment(){
        CustmerToast.makeText(mContent, "正在评论。。。").show();
        GlobalTreadPools.getInstance(mContent).execute(new Runnable() {
            @Override
            public void run() {
                if(pictures == null){
                    pictures = new byte[]{};
                }
                final String token = GlobalDataHelper.getToken(mContent);
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", token),
                        new OkHttpClientManager.Param("picture", org.apache.shiro.codec.Base64.encodeToString(pictures)),
                        new OkHttpClientManager.Param("comment", editCommet.trim())
                };
                String url = String.format("%s%s",
                        PropertyService.getInstance().getKey("serverUrl"), "/chat/publish_comment");
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
                                message.what = MessageUtil.PUBLISHED_COMMENT;
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

    /**
     * 回复评论
     * @param
     */
    private void replayComment(){
        CustmerToast.makeText(mContent, "正在回复。。。").show();
        GlobalTreadPools.getInstance(mContent).execute(new Runnable() {
            @Override
            public void run() {
                final String token = GlobalDataHelper.getToken(mContent);
                if(pictures == null){
                    pictures = new byte[]{};
                }
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", token),
                        new OkHttpClientManager.Param("picture", org.apache.shiro.codec.Base64.encodeToString(pictures)),
                        new OkHttpClientManager.Param("comment", editCommet.trim()),
                        new OkHttpClientManager.Param("comment_id", commentsList.get(currentSelectedPosition).getId()+""),
                        new OkHttpClientManager.Param("replay_id", commentsList.get(currentSelectedPosition).getId()+""),
                        new OkHttpClientManager.Param("comment_user_id", commentsList.get(currentSelectedPosition).getUserId()+"")
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
                                message.what = MessageUtil.PUBLISHED_REPLAY_COMMENT;
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

    private void showCommentDialog() {
        dialog = new BottomSheetDialog(mContent);
        if (commentView == null) {
            commentView = LayoutInflater.from(mContent).inflate(R.layout.comment_dialog_layout, null);
        }
        if (commentView.getParent() != null) {
            ((ViewGroup) commentView.getParent()).removeView(commentView);
        }
        final EditText commentText = (EditText) commentView.findViewById(R.id.dialog_comment_et);
        final Button bt_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
        final Button dialog_comment_pic = commentView.findViewById(R.id.dialog_comment_pic);
        dialog.setContentView(commentView);

        invalidCommentView();
        View.OnClickListener listener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.dialog_comment_bt) {
                    if (!TextUtils.isEmpty(editCommet)) {
                        dialog.dismiss();
                        publishComment();
                    } else {
                        CustmerToast.makeText(mContent, "评论内容不能为空").show();
                    }
                } else if (view.getId() == R.id.dialog_comment_pic) {
                    onSelectFromPho();
                }
            }
        };
        bt_comment.setOnClickListener(listener);
        dialog_comment_pic.setOnClickListener(listener);

        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && charSequence.length() > 1) {
                    bt_comment.setBackgroundColor(getColor(R.color.grey_dark_bg));
                } else {
                    bt_comment.setBackgroundColor(getColor(R.color.replay_bg));
                }
                editCommet = commentText.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
    }

    private void showReplyDialog(final int position) {
        currentSelectedPosition = position;
        dialog = new BottomSheetDialog(mContent);
        if (commentView == null) {
            commentView = LayoutInflater.from(mContent).inflate(R.layout.comment_dialog_layout, null);
        }
        if (commentView.getParent() != null) {
            ((ViewGroup) commentView.getParent()).removeView(commentView);
        }
        final EditText commentText = (EditText) commentView.findViewById(R.id.dialog_comment_et);
        final Button bt_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
        final Button dialog_comment_pic = commentView.findViewById(R.id.dialog_comment_pic);
        commentText.setHint("回复 " + commentsList.get(position).getNickName() + " 的评论:");
        dialog.setContentView(commentView);
        invalidCommentView();

        View.OnClickListener listener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.dialog_comment_bt) {
                    if (!TextUtils.isEmpty(editCommet)) {
                        dialog.dismiss();
                        replayComment();
                    } else {
                        CustmerToast.makeText(mContent, "评论内容不能为空").show();
                    }
                } else if (view.getId() == R.id.dialog_comment_pic) {
                    onSelectFromPho();
                }
            }
        };
        bt_comment.setOnClickListener(listener);
        dialog_comment_pic.setOnClickListener(listener);
        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && charSequence.length() > 2) {
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


    @Override
    public void startRefresh() {
        loadCommentList();
    }

    @Override
    public void loadMore() {
        if(!appendList){
            appendList = true;
            appendCommentList();
        }
    }


    private void jump2Sub(CommentDetailBean bean){
        Intent intent = new Intent(mContent, DisgussSubActivity.class);
        GlobalDataHelper.setDate("comment", bean);
        startActivity(intent);
    }



    @Override
    public void onCommentClick(Integer position) {
        showReplyDialog(position);
    }

    @Override
    public void onShowMoreClick(Integer position) {
        jump2Sub(commentsList.get(position));
    }

    @Override
    public void onChildReplayClick(Integer position, Integer childPos, Integer replayUserId) {
        jump2Sub(commentsList.get(position));
    }

}
