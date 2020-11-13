package com.cgmn.msxl.in;

public interface RefreshListener {
    void startRefresh(); //刷新
    void loadMore();  //加载
    void hintChange(String hint);  //提示文字
}
