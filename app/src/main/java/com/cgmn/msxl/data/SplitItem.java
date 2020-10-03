package com.cgmn.msxl.data;

public class SplitItem {
    private String splitText;

    public SplitItem(String content){
        splitText = content;
    }

    public String getSplitText() {
        return splitText;
    }

    public void setSplitText(String splitText) {
        this.splitText = splitText;
    }
}
