package com.cgmn.msxl.utils;

import android.net.Uri;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtil {
    private static DecimalFormat formatP = new DecimalFormat("0.00%");
    private static DecimalFormat format2 = new DecimalFormat("#,##0.00");
    private static DecimalFormat format3 = new DecimalFormat("#,##0.000");

    private static DecimalFormat formatAmt = new DecimalFormat("#,##0");

    public static boolean isEmpty(Object o){
        if(o == null){
            return true;
        }else if(o instanceof String){
            String str = (String) o;
            if(str.trim().length() == 0){
                return true;
            }
        }else if(o instanceof List && ((List)o).isEmpty()){
            return true;
        }else if(o instanceof Map){
            if(((Map) o).keySet().size() == 0){
                return true;
            }
        }

        return false;
    }

    public static String buildGetUrl(String url,String action, Map<String, String> params){
        StringBuffer res = new StringBuffer(url);
        res.append(action);
        if(isEmpty(params)){
            params = new HashMap<>();
        }
        if(!isEmpty(params)){
            String paramStr = buildParamters(res.toString(), params);
            res.append("?");
            res.append(paramStr);
        }
        return res.toString();
    }

    public static String buildParamters(String url, Map<String, String> params){
        Uri uri = Uri.parse(url);
        Uri.Builder builder = uri.buildUpon();
        for(Map.Entry<String, String> entry:params.entrySet()){
            if(entry.getValue() == null){
                continue;
            }
            builder.appendQueryParameter(entry.getKey(), URLEncoder.encode(entry.getValue()));
        }
        return builder.build().getQuery();
    }

    public static void jsonToMap(JSONObject jsonObject, Map<String, Object> resMap){
        Iterator it = jsonObject.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = null;
            try {
                value = jsonObject.get(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(value == null){
                continue;
            }
            if(value instanceof JSONObject){
                Map<String, Object> tMap = new HashMap<>();
                resMap.put(key, tMap);
                jsonToMap((JSONObject) value, tMap);
            }else{
                resMap.put(key, value);
            }
        }
    }

    public static float castFloatFromString(String num){
        return Float.valueOf(num);
    }

    public static String formatPercent(Object num){
        return formatP.format(num);
    }

    public static String formatNumer(Object num, int tail){
        if(tail ==3){
            return format3.format(num);
        }else {
            return format2.format(num);
        }
    }

    public static String formatAmt(Object num){
        return formatAmt.format(num);
    }

    public static String formatNumer(Object num){
        return format2.format(num);
    }

    public static boolean floatNumEqual(float num1, float num2){
        float delta = num1 - num2;
        if(Math.abs(delta) < 0.00001){
            return true;
        }
        return false;
    }

    public static boolean doubleNumEqual(Double num1, Double num2){
        Double delta = num1 - num2;
        if(Math.abs(delta) < 0.00001){
            return true;
        }
        return false;
    }

    public static String formartTimeString(Object time, String format){
        if(StringUtils.isEmpty(format)){
            format = "yyyy-MM-dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String str = null;
        try{
            str = sdf.format((Date)time);
        }catch (Exception e){
            try{
                str = sdf.format((Timestamp)time);
            }catch (Exception e2){
            }
        }

        return str;
    }

    public static Date parseDateString(String dataStr){
        String[] formats = {"yyyy-MM-dd", "MM/dd/yyyy", "yyyyMMdd"};
        Date data = null;
        if(dataStr == null || "00000000".equals(dataStr)){
            return null;
        }
        for(String f : formats){
            if (data == null){
                SimpleDateFormat sf = new SimpleDateFormat(f);
                try {
                    data = sf.parse(dataStr);
                } catch (Exception e) {

                }
            }else{
                break;
            }
        }

        return data;
    }

    public static Date parseDateString(String dataStr, String format){
        Date data = null;
        if(dataStr == null || "00000000".equals(dataStr)){
            return null;
        }
        SimpleDateFormat sf = new SimpleDateFormat(format);
        try {
            data = sf.parse(dataStr);
        } catch (Exception e) {

        }

        return data;
    }
}
