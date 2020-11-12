package com.cgmn.msxl.ac;

import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.view.CommentExpandableListView;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.CommentDetailBean;

public class DisgussSubActivity extends BaseOtherActivity{
    private CommentDetailBean comment;

    private NetImageView comment_item_logo, comment_picture;
    private TextView userName, time, content,detail_page_do_comment;

    private ScrollView scrollView;
    private CommentExpandableListView sub_list_comment;

    @Override
    protected int getContentView() {
        return R.layout.disguss_sub;
    }

    @Override
    protected void init() {

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





        Bundle bundle = getIntent().getExtras();
        comment  = (CommentDetailBean) bundle.get("comment");
    }
}
