package com.cgmn.msxl.data;

public class ModeKeyValue {
    private int type;
    private String content;

    public ModeKeyValue(int type, String content){
        this.type = type;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
