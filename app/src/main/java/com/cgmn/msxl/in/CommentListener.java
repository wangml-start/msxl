package com.cgmn.msxl.in;

import android.view.View;

public interface CommentListener {
    void onApproveClick(Integer position, String type);
    void onCommentClick(Integer position);
    void onSettingClick(View view, Integer position);
}
