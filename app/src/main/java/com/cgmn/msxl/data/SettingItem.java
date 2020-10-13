package com.cgmn.msxl.data;

import com.cgmn.msxl.comp.swb.State;

public class SettingItem {
    private String modeText;
    private int modedType;
    private State state;

    public SettingItem(int type, String txt){
        modeText = txt;
        modedType = type;
        state = State.OPEN;
    }

    public SettingItem(int type, String txt, int btstats){
        modeText = txt;
        modedType = type;
        setState(btstats);
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

    public State getState() {
        return state;
    }

    public void setState(int btstats) {
        if(State.OPEN.ordinal() == btstats){
            this.state = State.OPEN;
        }else{
            this.state = State.CLOSE;
        }
    }
}
