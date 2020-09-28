package com.cgmn.msxl.utils;

public class FixStringBuffer {
    private StringBuffer message;

    public FixStringBuffer(){
        this.message = new StringBuffer();
    }

    public FixStringBuffer(String content){
        if(content != null){
            this.message = new StringBuffer(content);
        }else{
            this.message = new StringBuffer();
        }
    }

    public void append(String str, Object... args){
        this.message.append(String.format(str, args));
    }

    public String toString(){
        return this.message.toString();
    }

    public void clear(){
        this.message.setLength(0);
    }

    public Integer length(){
        return message.length();
    }
}
