package com.cgmn.msxl.comp;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cgmn.msxl.R;

@SuppressLint("ValidFragment")
public class MayFragment extends Fragment {
    private String content;
    public MayFragment(String content) {
        this.content = content;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.train_fragment, container, false);
//        TextView txt_content = (TextView) view.findViewById(R.id.txt_content);
//        txt_content.setText(content);
        return view;
    }
}
