package com.cgmn.msxl.ac;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.service.GlobalDataHelper;

public class ImageViewActivity extends AppCompatActivity {
    public Context mContext;

    private RelativeLayout view;
    private NetImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.text_topbar));
        }

        mContext = this;
        view = findViewById(R.id.main_layout);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.main_layout){
                    finish();
                }
            }
        });
        image = findViewById(R.id.image);
        Object obj = GlobalDataHelper.getDate("content");
        if(obj != null){
            image.setImageContent((byte[]) obj);
        }else {
            obj = GlobalDataHelper.getDate("email");
            image.setImageName((String) obj);
            image.setImageURL(GlobalDataHelper.getUserPortraitUrl(mContext));
        }


    }
    @Override
    public void finish() {
        image.relased();
        super.finish();
    }

}