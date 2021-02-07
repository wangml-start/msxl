package com.cgmn.msxl.data;

public class CatchItem {
    private Integer recordId;

    private String text;

    public CatchItem(Integer id, String des){
        recordId = id;
        text = des;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
