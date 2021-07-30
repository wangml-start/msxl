package com.cgmn.msxl.utils;

import android.net.Uri;
import com.cgmn.msxl.service.OkHttpClientManager;
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
    private static DecimalFormat formatLargeAmt = new DecimalFormat("#,##0.00");

    private static DecimalFormat formatVol = new DecimalFormat("##0");
    private static DecimalFormat formatLargeVol = new DecimalFormat("##0.0");

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
        Double number = Double.valueOf(num.toString());
        if(number >= 100){
            return format2.format(number/100) + "百倍";
        }else{
            return formatP.format(num);
        }
    }

    public static String formatNumer(Object num, int tail){
        if(tail ==3){
            return format3.format(num);
        }else {
            return format2.format(num);
        }
    }

    public static String formatAmt(Object num){
        Integer symbol = 1;
        if(isEmpty(num)){
            return "";
        }
        if(Double.valueOf(num.toString()) < 0){
            symbol = -1;
        }
        Double number = Math.abs(Double.valueOf(num.toString()));
        if( number > 9999.0 * 10000 ){
            return formatLargeAmt.format(number/(10000*10000) * symbol)+"亿";
        }else if(number > 9999.0){
            return formatLargeAmt.format(number/10000 * symbol)+"万";
        }else{
            return formatAmt.format(number* symbol);
        }
    }

    public static String formatHDNumer(Object num){
        Integer symbol = 1;
        if(isEmpty(num)){
            return "";
        }
        if(Double.valueOf(num.toString()) < 0){
            symbol = -1;
        }
        Double number = Math.abs(Double.valueOf(num.toString()));
        if( number > 9999.0 * 10000 ){
            return formatLargeAmt.format(number/(10000) * symbol)+"万";
        } else{
            return formatAmt.format(number* symbol);
        }
    }


    public static String formatVolume(Object num){
        if(isEmpty(num)){
            return "";
        }
        Double number = Math.abs(Double.valueOf(num.toString()));
        if( number >= 10000){
            return formatLargeVol.format(number/10000)+"万";
        }else{
            return formatVol.format(number);
        }
    }

    public static String formatNumer(Object num){
        return format2.format(num);
    }

    public static boolean floatNumEqual(Double num1, Double num2){
        Double delta = num1 - num2;
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

    /**
     * 不考虑null
     * @param obj1
     * @param obj2
     * @return
     */
    public static int compareDate(Object obj1, Object obj2){
        Date date1=null, date2=null;
        if(obj1 instanceof Date){
            date1 = (Date)obj1;
        }else{
            date1 = parseDateString((String)obj1);
        }
        if(obj2 instanceof Date){
            date2 = (Date)obj2;
        }else{
            date2 = parseDateString((String)obj2);
        }

        if(isEmpty(date1) && !isEmpty(date2)){
            return -1;
        }
        if(!isEmpty(date1) && isEmpty(date2)){
            return 1;
        }
        if(isEmpty(date1) && isEmpty(date2)){
            return 0;
        }

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

        return fmt.format(date1).compareTo(fmt.format(date2));
    }


    public static OkHttpClientManager.Param[] map2PostParams(Map<String, String> map){
        Integer count =  map.keySet().size();
        OkHttpClientManager.Param[] list = new OkHttpClientManager.Param[count];
        Integer index = 0;
        for(Map.Entry<String, String> en : map.entrySet()){
            list[index++] = new OkHttpClientManager.Param(en.getKey(), en.getValue());
        }
        return list;
    }
}
