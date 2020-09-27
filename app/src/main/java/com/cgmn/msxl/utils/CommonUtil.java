package com.cgmn.msxl.utils;

import android.net.Uri;
import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommonUtil {

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
        String paramStr = buildParamters(res.toString(), params);
        res.append("?");
        res.append(paramStr);
        return res.toString();
    }

    public static String buildParamters(String url, Map<String, String> params){
        Uri uri = Uri.parse(url);
        Uri.Builder builder = uri.buildUpon();
        for(Map.Entry<String, String> entry:params.entrySet()){
            builder.appendQueryParameter(entry.getKey(), (String) entry.getValue());
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

    public static String encodePassword(String pass, Integer times){
        int index = 3;
        if(times != null){
            index = times;
        }
        String trans = pass;
        while(index-- > 0){
            trans = Base64.encodeToString(trans.getBytes(), Base64.DEFAULT);
        }

        return trans;
    }

    public static String decodePassword(String pass, Integer times){
        int index = 3;
        if(times != null){
            index = times;
        }
        String trans = pass;
        while(index-- > 0){
            trans = new String(Base64.decode(trans.getBytes(), Base64.DEFAULT));
        }

        return trans;
    }

}
