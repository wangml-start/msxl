package com.cgmn.msxl.comp.k.time;

public class MinuteCounter {
    String hour;
    String minutes;
    String second;
    Integer distance = 3;

    public void setTime(String timeStr){
        if(timeStr == null || timeStr.length() != 6){
            return;
        }
        hour = timeStr.substring(0, 2);
        minutes = timeStr.substring(2, 4);
        second = timeStr.substring(4, 6);
    }

    public void nextStep(){
        Integer temp = distance + Integer.valueOf(second);
        if(temp == 60){
            carryMinites();
        }else{
            second = String.format("%02d", temp);
        }
    }

    public void carryMinites(){
        Integer temp = 1 + Integer.valueOf(minutes);
        if(temp == 60){
            second = "00";
            carryHour();
        }else{
            minutes = String.format("%02d", temp);
        }
    }

    public void carryHour(){
        minutes = "00";
        Integer temp = 1 + Integer.valueOf(hour);
        hour = String.format("%02d", temp);
    }


    public String getTimeStr(){
        return String.format("%s%s%s", hour,minutes,second);
    }

    public String getTimeMinute(){
        return String.format("%s%s", hour,minutes);
    }
}
