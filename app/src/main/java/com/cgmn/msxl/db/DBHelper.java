package com.cgmn.msxl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DBHelper {

    protected AppSqlHelper sqlHelper;

    protected Context context;

    public DBHelper(Context conetxt) {
        this.context = context;
        sqlHelper = new AppSqlHelper(conetxt);
    }

    public void upsert(String tableName, ContentValues values, String key) {
        SQLiteDatabase db = null;
        try {
            db = sqlHelper.getWritableDatabase();
            String sql = "SELECT id FROM '?' WHERE ? ='?'";
            Cursor cursor = db.rawQuery(sql, new String[]{tableName, key, (String) values.get(key)});
            if(cursor.moveToFirst()){
                Integer id = cursor.getInt(cursor.getColumnIndex("id"));
                update(tableName, values, String.format("id=%s", id));
            }else{
                insert(tableName, values);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    public void insert(String tableName, ContentValues values) {
        SQLiteDatabase db = null;
        try {
            db = sqlHelper.getWritableDatabase();
            db.insert(tableName, null, values);
        } catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    public void update(String tableName, ContentValues values, String condition) {
        SQLiteDatabase db = null;
        try {
            db = sqlHelper.getWritableDatabase();
            db.update(tableName, values, condition, null);
        } catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    public void delete(String tableName, String condition) {
        SQLiteDatabase db = null;
        try {
            db = sqlHelper.getWritableDatabase();
            db.delete(tableName, condition, null);
        } catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    public List<Map<String, Object>> query(String sql, String[] params, String[] queries) {
        SQLiteDatabase db = null;
        try {
            db = sqlHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, params);
            List<Map<String, Object>> list = new ArrayList<>();
            while(cursor.moveToNext()){
                Map<String,Object> map = new HashMap<>();
                for(String field : queries){
                    String value = cursor.getString(cursor.getColumnIndex(field));
                    map.put(field, value);
                }
                list.add(map);
            }
            cursor.close();
            return list;
        }catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    public String getToken(){
        SQLiteDatabase db = null;
        try {
            String query = "SELECT token FROM users WHERE last_active=1 LIMIT 1";
            db = sqlHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, new String[]{});
            List<Map<String, Object>> list = new ArrayList<>();
            String value = null;
            if(cursor.moveToFirst()){
                value = cursor.getString(cursor.getColumnIndex("token"));
            }
            cursor.close();
            return value;
        }catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    public Map<String, Object> getActiveUser(){
        String sql = "SELECT * FROM users WHERE last_active=1 LIMIT 1";
        String[] params = new String[]{};
        String[] fields = new String[]{"user_name", "account", "password", "token"};
        List<Map<String, Object>> list = query(sql, params, fields);
        return list.get(0);
    }
}
