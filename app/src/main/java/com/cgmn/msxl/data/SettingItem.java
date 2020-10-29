package com.cgmn.msxl.data;

import com.cgmn.msxl.comp.swb.State;

public class SettingItem {
    private String modeText;
    private int modedType;
    private int state;
    private String miss;

    public SettingItem(int type, String txt){
        this.modeText = txt;
        this.modedType = type;
        this.state = State.OPEN;
    }

    public SettingItem(int type, String txt, int btstats){
        this.modeText = txt;
        this.modedType = type;
        this.state = btstats;
    }

    public SettingItem(String modeText, String miss){
        this.modeText = modeText;
        this.miss = miss;
    }

    public String getModeText() {
        return modeText;
    }

    public void setModeText(String modeText) {
        this.modeText = modeText;
    }

    public int getModedType() {
        return modedType;
    }

    public void setModedType(int modedType) {
        this.modedType = modedType;
    }

    public int getState() {
        return state;
    }

    public void setState(int btstats) {
        this.state = btstats;
    }

    public String getMiss() {
        return miss;
    }

    public void setMiss(String miss) {
        this.miss = miss;
    }
}
