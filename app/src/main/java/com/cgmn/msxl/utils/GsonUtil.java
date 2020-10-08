package com.cgmn.msxl.utils;

import com.google.gson.Gson;

public class GsonUtil {
    private static Gson gson = new Gson();

    public static String toJson(Object obj){
        return gson.toJson(obj);
    }
}
