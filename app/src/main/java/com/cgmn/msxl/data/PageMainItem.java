package com.cgmn.msxl.data;

public class PageMainItem {
    private int aIcon;
    private String aName;

    public PageMainItem() {
    }

    public PageMainItem(int aIcon, String aName) {
        this.aIcon = aIcon;
        this.aName = aName;
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
}
