package com.cgmn.msxl.ac;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.adpter.CommentExpandAdapter;
import com.cgmn.msxl.comp.view.CommentExpandableListView;
import com.cgmn.msxl.comp.view.NetImageView;
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
import com.cgmn.msxl.utils.ImageUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.apache.shiro.codec.Base64;

import java.util.*;

public class DisgussActivity extends Activity
        implements RefreshListener, CommentListener {
    private static final String TAG = "DisgussActivity";

    private Context mContent;

    private TextView bt_comment;
    private ScrollView scrollView;
    private CommentExpandableListView expandableListView;
    private CommentExpandAdapter adapter;
    private List<CommentDetailBean> commentsList = new ArrayList<>();
    private View commentView = null;
    private RelativeLayout img_back;
    private BottomSheetDialog dialog;
    private View.OnClickListener clickListener;
    //消息处理
    private Handler mHandler;

    private static final int REQUEST_IMAGE_GET = 0;
    private View imageView = null;
    private byte[] pictures = null;
    private String editCommet;

    private int currentSelectedPosition;


    private void onCustmerClick(View v){
        if (v.getId() == R.id.detail_page_do_comment) {
            showCommentDialog();
        }else if(v.getId() == R.id.img_back){
            finish();
        }
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.LOAD_COMMENT_LIST) {
                    commentsList.clear();
                    CommentBean commentBean = new CommentBean(msg.obj);
                    commentBean.getList(commentsList);
                    if(adapter == null){
                        initExpandableListView();
                    }
                    adapter.notifyDataSetChanged();
                } else if (msg.what == MessageUtil.PUBLISHED_COMMENT) {
                    String userName = GlobalDataHelper.getUserName(mContent);
                    String time = CommentBean.analysisTime(new Date());
                    CommentDetailBean detailBean = new CommentDetailBean(userName, editCommet, time);
                    detailBean.setPicture(pictures);
                    byte[] cut = GlobalDataHelper.getUserCut(mContent);
                    if(cut != null && cut.length > 0){
                        detailBean.setUserLogo(cut);
                    }
                    detailBean.setUserId(GlobalDataHelper.getUserId(mContent));
                    detailBean.setId((Integer) msg.obj);
                    adapter.addTheCommentData(detailBean);
                    resetDialog();
                    scrollView.setScrollY(0);
                } else if(msg.what == MessageUtil.PUBLISHED_REPLAY_COMMENT){
                    String userName = GlobalDataHelper.getUserName(mContent);
                    ReplyDetailBean detailBean = new ReplyDetailBean(userName, editCommet);
                    detailBean.setId((Integer) msg.obj);
                    detailBean.setUserId(GlobalDataHelper.getUserId(mContent));
                    adapter.addReplyDataTofirst(detailBean, currentSelectedPosition);
                    expandableListView.expandGroup(currentSelectedPosition);
                    resetDialog();
                } else if (msg.what == MessageUtil.APPROVED_COMMENT) {

                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContent).handlerException((Exception) msg.obj);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disguss_fragment);
        mContent = this;
        initView();
        initMessageHandle();
        loadCommentList();
    }


    private void initView() {
        expandableListView = (CommentExpandableListView)findViewById(R.id.detail_page_lv_comment);
        bt_comment = (TextView) findViewById(R.id.detail_page_do_comment);
        clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCustmerClick(v);
            }
        };
        bt_comment.setOnClickListener(clickListener);
        img_back = findViewById(R.id.img_back);
        img_back.setOnClickListener(clickListener);
        scrollView = findViewById(R.id.scrollView);
    }



    /**
     * 初始化评论和回复列表
     */
    private void initExpandableListView() {
        expandableListView.setGroupIndicator(null);
        //默认展开所有回复
        adapter = new CommentExpandAdapter(mContent, commentsList);
        adapter.setCommentListener(this);
        expandableListView.setAdapter(adapter);
        for (int i = 0; i < commentsList.size(); i++) {
            expandableListView.expandGroup(i);
        }
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                jump2Sub(commentsList.get(groupPosition));
                return true;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                jump2Sub(commentsList.get(groupPosition));
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


    private void loadCommentList(){
        GlobalTreadPools.getInstance(mContent).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContent));
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

    private void appendCommentList(){
        GlobalTreadPools.getInstance(mContent).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("start", (commentsList.size()-1)+"");
                params.put("token", GlobalDataHelper.getToken(mContent));
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


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onSelectFromPho() {
        if (imageView != null) {
            TextView dialog_comment_dis = commentView.findViewById(R.id.dialog_comment_dis);
            dialog_comment_dis.setText("只能添加一张图片");
            return;
        }
        // 权限申请
        if (ContextCompat.checkSelfPermission(mContent,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //权限还没有授予，需要在这里写申请权限的代码
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            // 如果权限已经申请过，直接进行图片选择
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            // 判断系统中是否有处理该 Intent 的 Activity
            if (intent.resolveActivity(mContent.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                CustmerToast.makeText(mContent, "未找到图片查看器").show();
            }
        }
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
                        new OkHttpClientManager.Param("comment", editCommet)
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
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
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
                        new OkHttpClientManager.Param("comment", editCommet),
                        new OkHttpClientManager.Param("comment_id", commentsList.get(currentSelectedPosition).getId()+""),
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
                Log.e(TAG, "NAME=" + Thread.currentThread().getName());
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

    /**
     * 处理权限回调结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    // 判断系统中是否有处理该 Intent 的 Activity
                    if (intent.resolveActivity(mContent.getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(mContent, "未找到图片查看器", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    /**
     * 处理回调结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 回调成功
        if (resultCode == -1) {
            switch (requestCode) {
                // 相册选取
                case REQUEST_IMAGE_GET:
                    Uri selectedImage = data.getData();
                    String[] filePathColumns = {MediaStore.Images.Media.DATA};
                    Cursor c = mContent.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePathColumns[0]);
                    String imagePath = c.getString(columnIndex);
                    byte[] bs = ImageUtil.getCompressBytes(imagePath, 800);
                    if (bs != null && bs.length > 0) {
                        pictures = bs;
                        imageView = LayoutInflater.from(mContent).inflate(R.layout.comment_image_item, null);
                        final LinearLayout comment_parent_view = commentView.findViewById(R.id.comment_parent_view);
                        comment_parent_view.addView(imageView);
                        NetImageView im = imageView.findViewById(R.id.com_picture);
                        im.setImageContent(bs);
                        TextView com_picture_close = imageView.findViewById(R.id.com_picture_close);
                        com_picture_close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                comment_parent_view.removeView(imageView);
                                imageView = null;
                                pictures = null;
                                TextView dialog_comment_dis = commentView.findViewById(R.id.dialog_comment_dis);
                                dialog_comment_dis.setText("");
                            }
                        });
                        invalidCommentView();
                    }
                    break;
            }
        }
    }

    @Override
    public void startRefresh() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    public void hintChange(String hint) {

    }

    @Override
    public void setWidthX(int x) {

    }

    private void jump2Sub(CommentDetailBean bean){
        Intent intent = new Intent(mContent, DisgussSubActivity.class);
        GlobalDataHelper.setDate("comment", bean);
        startActivity(intent);
    }

    @Override
    public void onApproveClick(final Integer position, final String type) {
        GlobalTreadPools.getInstance(mContent).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContent));
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
        showReplyDialog(position);
    }

}
