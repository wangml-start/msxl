package com.cgmn.msxl.data;

import java.util.ArrayList;
import java.util.List;

public class ModeList {
    private List<SettingItem> list = null;
    private static ModeList model = null;

    private ModeList(){
        list = new ArrayList<>();
        list.add(new SettingItem(1001, "只参与多头趋势"));
        list.add(new SettingItem(1002, "破位十日均线离场"));
    }

    public static ModeList getInstance(){
        if(model == null){
            model = new ModeList();
        }

        return model;
    }

    public List<SettingItem> getList(){
        return list;
    }
}
