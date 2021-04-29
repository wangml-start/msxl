package com.cgmn.msxl.data;

public class PageMainItem {
    public static int MODEL_SETTING = 0;
    public static int LEADING_STRATEGY = 1;
    public static int NORMAL_STRATEGY = 2;
    public static int DAY_RANK = 3;
    public static int VIOLATE_MODE_DETAI = 4;
    public static int TOTAL_RANK = 5;

    public static int SUM_PL_LINE = 6;
    public static int LEADING_LINE = 7;
    public static int NORMARL_LINE = 8;

    public static int MY_GENERAL_INFO = 9;
    public static int VIP_INFO = 10;
    public static int CHARGE_INFO = 11;

    public static int DAN_RANK = 12;
    public static int CONTACT_US = 13;
    public static int CHECK_NEW_VERSION = 14;
    public static int TREND_BREAK_UP = 15;
    public static int OPTIONAL_STOCKS = 16;
    public static int USER_AGREMENT = 17;

    public static int COMMENT_TO_ME = 18;
    public static int APPROVE_TO_ME = 19;

    public static int PERSONAL_INFO = 20;
    public static int FREE_KLINE = 21;
    public static int FREE_TIME_SHARE = 22;

    public static int FREE_RANK_LIST = 23;
    public static int FREE_RATE_CURV = 24;

    private int aIcon;
    private String aName;
    private int itemType;
    private int rightColor;

    private String rightDec;

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

    public String getRightDec() {
        return rightDec;
    }

    public void setRightDec(String rightDec) {
        this.rightDec = rightDec;
    }

    public int getRightColor() {
        return rightColor;
    }

    public void setRightColor(int rightColor) {
        this.rightColor = rightColor;
    }
}
