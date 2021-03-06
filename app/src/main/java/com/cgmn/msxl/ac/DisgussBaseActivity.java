package com.cgmn.msxl.ac;

import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.bean.PopuBean;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.adpter.CommentExpandAdapter;
import com.cgmn.msxl.comp.view.CommentExpandableListView;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.comp.view.PopuWindowView;
import com.cgmn.msxl.comp.view.RefreshScrollView;
import com.cgmn.msxl.data.CommentDetailBean;
import com.cgmn.msxl.in.CommentListener;
import com.cgmn.msxl.in.RefreshListener;
import com.cgmn.msxl.in.TdataListener;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.ImageUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.*;

public abstract class DisgussBaseActivity extends Activity
        implements RefreshListener, CommentListener {

    protected static final int REQUEST_IMAGE_GET = 0;
    //消息处理
    protected Handler mHandler;
    protected Context mContext;
    protected View commentView = null;
    protected View imageView = null;
    protected RelativeLayout headView;
    private RelativeLayout img_back;
    protected TextView bt_comment, head_view_tv;
    protected RefreshScrollView scrollView;
    protected CommentExpandableListView expandableListView;
    protected CommentExpandAdapter adapter;
    protected BottomSheetDialog dialog;
    protected View.OnClickListener clickListener;

    protected byte[] pictures = null;
    protected String editCommet;
    protected boolean appendList = false;
    protected List<CommentDetailBean> commentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getView());
        mContext = this;
        initailize();
    }

    protected abstract int getView();

    protected abstract void init();

    protected abstract void onCustmerClick(View v);

    private void initailize() {
        expandableListView = (CommentExpandableListView) findViewById(R.id.detail_page_lv_comment);
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

        headView = (RelativeLayout) findViewById(R.id.head_view);
        head_view_tv = (TextView) findViewById(R.id.head_view_tv);
        scrollView.setListsner(this);
        scrollView.setHeadView(headView);

        init();
    }

    protected void starLoadChatList(){
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;// 表示屏幕的像素宽度，单位是px（像素）
        scrollView.startRefresh(width, 100);
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
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        CustmerToast.makeText(mContext, "未找到图片查看器").show();
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
                    Cursor c = mContext.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePathColumns[0]);
                    String imagePath = c.getString(columnIndex);
                    byte[] bs = ImageUtil.getCompressBytes(imagePath);
                    if (bs != null && bs.length > 0) {
                        pictures = bs;
                        imageView = LayoutInflater.from(mContext).inflate(R.layout.comment_image_item, null);
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

    protected void invalidCommentView() {
        /**
         * 解决bsd显示不全的情况
         */
        View parent = (View) commentView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        commentView.measure(0, 0);
        behavior.setPeekHeight(commentView.getMeasuredHeight());
    }

    protected void autoShowkeyboard(EditText commentText){
        commentText.setFocusable(true);
        commentText.setFocusableInTouchMode(true);
        commentText.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) commentText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(commentText, 0);
    }

    @Override
    public void onApproveClick(final Integer position, final String type) {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                params.put("type", type);
                params.put("comment_id", commentsList.get(position).getId() + "");
                params.put("comment_user_id", commentsList.get(position).getUserId() + "");

                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
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
            }
        });
    }

    protected void resetDialog() {
        commentView = null;
        imageView = null;
        pictures = null;
        editCommet = null;
    }

    protected void expandList() {
        for (int i = 0; i < commentsList.size(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onSelectFromPho() {
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

    @Override
    public void hintChange(String hint) {
        head_view_tv.setText(hint);
    }

    @Override
    public void onShowMoreClick(Integer position) {
    }

    @Override
    public void onShowPicture(byte[] bytes, String phone) {
        GlobalDataHelper.setData("content", bytes);
        GlobalDataHelper.setData("email", phone);
        Intent intent = new Intent(mContext, ImageViewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSettingClick(View view, Integer position) {
        CommentDetailBean bean = commentsList.get(position);
        showPopupMenu(view, bean);
    }

    protected void showPopupMenu(final View view, final CommentDetailBean bean) {
        final PopuWindowView popuWindowView = new PopuWindowView(mContext, 230);
        popuWindowView.setMaxLines(4);
        popuWindowView.initPupoData(new TdataListener() {
            @Override
            public void initPupoData(List<PopuBean> lists) {
                if (bean.getMyComment() == 1) {
                    PopuBean popu = new PopuBean();
                    popu.setTitle("删除");
                    popu.setValue("delete");
                    popu.setRes(R.drawable.del_icon);
                    lists.add(popu);
                }
                PopuBean popu = new PopuBean();
                popu.setTitle("复制");
                popu.setValue("copy");
                popu.setRes(R.drawable.copy);
                lists.add(popu);
            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position) {
                PopuBean pop = popuWindowView.getPopuLists().get(position);
                if(pop.getValue().equals("delete")){
                    if (bean.getMyComment() == 1) {
                        deleteComment(bean.getId());
                    }
                }else if(pop.getValue().equals("copy")){
                    ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(bean.getContent());
                    CustmerToast.makeText(mContext, "复制成功").show();
                }
            }
        });
        popuWindowView.showing(view, -200, -30);
    }


    private void deleteComment(final Integer commentId) {
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContext));
                params.put("comment_id", commentId + "");

                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/chat/delete_comment", params);
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
                                message.what = MessageUtil.DELETED_COMMENT;
                                try {
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

}
