package com.cgmn.msxl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.*;

public class AppSqlHelper extends SQLiteOpenHelper {
    private static final int VERSION = 5;
    public final static String DB_NAME = "app.db";

    public AppSqlHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    //数据库第一次创建时被调用
    public void onCreate(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE users(");
        sql.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append("phone VARCHAR(32),");
        sql.append("user_name VARCHAR(64),");
        sql.append("password VARCHAR(128),");
        sql.append("token VARCHAR(64),");
        sql.append("last_active INTEGER");
        sql.append(");");

        sql.append("CREATE TABLE temp_data_save(");
        sql.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append("content text,");
        sql.append("data_type VARCHAR(64)");
        sql.append(");");
        db.execSQL(sql.toString());
    }

    //软件版本号发生改变时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }

    public void upsert(String tableName, ContentValues values, String key) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            String sql = String.format("SELECT id FROM %s WHERE %s = ?", tableName, key);
            Cursor cursor = db.rawQuery(sql, new String[]{(String) values.get(key)});
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
            db = getWritableDatabase();
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
            db = getWritableDatabase();
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
            db = getWritableDatabase();
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
            db = getReadableDatabase();
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
            db = getReadableDatabase();
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
        String[] fields = new String[]{"user_name", "phone", "password", "token"};
        List<Map<String, Object>> list = query(sql, params, fields);
        if(CommonUtil.isEmpty(list) || list.size() == 0){
            return null;
        }
        return list.get(0);
    }

    public String getKlinJsonStr(){
        String sql = "SELECT * FROM temp_data_save LIMIT 1";
        String[] params = new String[]{};
        String[] fields = new String[]{"content"};
        List<Map<String, Object>> list = query(sql, params, fields);
        if(CommonUtil.isEmpty(list) || list.size() == 0){
            return null;
        }
        return (String) list.get(0).get("content");
    }
}