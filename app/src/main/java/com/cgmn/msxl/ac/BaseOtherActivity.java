package com.cgmn.msxl.ac;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.cgmn.msxl.R;

public abstract class BaseOtherActivity extends AppCompatActivity {
    public Context mContext;
    protected RelativeLayout img_back;
    protected TextView txt_title, txt_complete;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_other);
        mContext = this;
        initView();
    }

    private void initView() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.img_back){
                    onBackUpClick();
                }else if(v.getId() == R.id.txt_complete){
                    onCompletedClick();
                }
            }
        };
        // 绑定控件
        toolbar = findViewById(R.id.toolbar);
        FrameLayout container = findViewById(R.id.container);

        img_back = findViewById(R.id.img_back);
        txt_title = findViewById(R.id.txt_title);
        img_back.setOnClickListener(listener);
        txt_complete = findViewById(R.id.txt_complete);
        txt_complete.setOnClickListener(listener);

        setSupportActionBar(toolbar);
        // 将继承了BaseActivity的布局文件解析到 container 中，这样 BaseActivity 就能显示 MainActivity 的布局文件了
        LayoutInflater.from(this).inflate(getContentView(), container);

        if(showTitle()){
            txt_title.setVisibility(View.VISIBLE);
        }
        if(showBackUp()){
            img_back.setVisibility(View.VISIBLE);
        }
        init();
        changeTooBarColor();
        // 初始化设置Toolbar
        txt_title.setText(setTitle());
    }

    /**
     * 获取要显示内容的布局文件的资源id
     *
     * @return 显示的内容界面的资源id
     */
    protected abstract int getContentView();

    protected String setTitle(){
        return null;
    };

    protected void init(){

    };

    protected boolean showTitle(){
        return true;
    };
    protected boolean showBackUp(){
        return true;
    };
    protected boolean showRight(){
        return true;
    };
    protected boolean showComplate(){
        return false;
    }

    protected void onBackUpClick(){
        finish();
    };
    protected void onCompletedClick(){

    }

    protected void changeTooBarColor(){};
}