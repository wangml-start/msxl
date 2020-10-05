package com.cgmn.msxl.data;

public class PageMainItem {
    public static int MODEL_SETTING = 0;
    public static int LEADING_STRATEGY = 1;
    public static int NORMAL_STRATEGY = 2;

    public static int DAY_RANK = 3;
    public static int MONTH_RANK = 4;
    public static int TOTAL_RANK = 5;

    private int aIcon;
    private String aName;
    private int itemType;

    public PageMainItem() {

    }

    public PageMainItem(int aIcon, String aName, int type) {
        this.aIcon = aIcon;
        this.aName = aName;
        this.itemType = type;
    }

    public int getaIcon() {
        return aIcon;
    }

    public String getaName() {
        return aName;
    }

    public void setaIcon(int aIcon) {
        this.aIcon = aIcon;
    }

    public void setaName(String aName) {
        this.aName = aName;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
    public int getItemType() {
        return itemType;
    }
}