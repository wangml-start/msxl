package com.cgmn.msxl.data;

import com.contrarywind.interfaces.IPickerViewData;

public class SelectionItem implements IPickerViewData {
    private String value;
    private String text;
    private String type;

    public SelectionItem(String pvalue, String pdes, String ptype){
        value = pvalue;
        text = pdes;
        type = ptype;
    }

    @Override
    public String getPickerViewText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
