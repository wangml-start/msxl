package com.cgmn.msxl.data;

public class EditInfoItem {
    private int aIcon;
    private String title;
    private String content;
    private String field_data;

    public EditInfoItem(int aIcon, String title, String content, String field_data) {
        this.aIcon = aIcon;
        this.title = title;
        this.content = content;
        this.field_data = field_data;
    }

    public int getaIcon() {
        return aIcon;
    }

    public void setaIcon(int aIcon) {
        this.aIcon = aIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getField_data() {
        return field_data;
    }

    public void setField_data(String field_data) {
        this.field_data = field_data;
    }
}
