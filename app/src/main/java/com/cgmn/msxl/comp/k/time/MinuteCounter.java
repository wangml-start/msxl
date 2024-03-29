package com.cgmn.msxl.comp.k.time;

public class MinuteCounter {
    String hour;
    String minutes;
    String second;
    Integer distance = 3;
    Integer minutye_distance = 60;

    Boolean isOver = false;

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public void setTime(String timeStr){
        if(timeStr == null || timeStr.length() != 6){
            return;
        }
        hour = timeStr.substring(0, 2);
        minutes = timeStr.substring(2, 4);
        second = timeStr.substring(4, 6);
    }

    public void nextStep(){
        if(isOver){
            return;
        }
        Integer temp = distance + Integer.valueOf(second);
        if(temp == 60){
            second = "00";
            carryMinites();
        }else{
            second = String.format("%02d", temp);
        }
    }

    public void nextMinutes(){
        if(isOver){
            return;
        }
        Integer temp = minutye_distance + Integer.valueOf(second);
        if(temp == 60){
            second = "00";
            carryMinites();
        }else{
            second = String.format("%02d", temp);
        }
    }

    public void carryMinites(){
        Integer temp = 1 + Integer.valueOf(minutes);
        if(temp == 60){
            minutes = "00";
            carryHour();
        }else{
            if(Integer.valueOf(String.format("%s%s", hour,minutes)).equals(1130)){
                hour="13";
                minutes = "00";
            }else{
                minutes = String.format("%02d", temp);
            }
        }
    }

    public void carryHour(){
        Integer temp = 1 + Integer.valueOf(hour);
        hour = String.format("%02d", temp);
        if(15 <= Integer.valueOf(hour)){
            isOver = true;
        }
    }


    public String getTimeStr(){
        return String.format("%s%s%s", hour,minutes,second);
    }

    public String showTimeStr(){
        return String.format("%s:%s:%s", hour,minutes,second);
    }

    public String getTimeMinute(){
        return String.format("%s%s", hour,minutes);
    }

    public Boolean getOver() {
        return isOver;
    }
}
