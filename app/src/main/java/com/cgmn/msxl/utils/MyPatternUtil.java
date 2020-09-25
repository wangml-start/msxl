package com.cgmn.msxl.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyPatternUtil {

    public static boolean validEmail(String email){
        StringBuffer ex = new StringBuffer();
        //^表明一行以什么开头；^[0-9a-z]表明要以数字或小写字母开头；\\w*表明匹配任意个大写小写字母或数字或下划线
        ex.append("^[0-9a-z]+\\w*");
        ex.append("@");
        //***.***.***格式的域名，其中*为小写字母或数字;第一个括号代表有至少一个***.匹配单元，而[0-9a-z]$表明以小写字母或数字结尾
        ex.append("([0-9a-z]+\\.)+[0-9a-z]+$");

        Pattern pattern= Pattern.compile(ex.toString());
        Matcher matcher=pattern.matcher(email);
        if(matcher.matches()){
            return true;
        }else {
            return false;
        }
    }
}
