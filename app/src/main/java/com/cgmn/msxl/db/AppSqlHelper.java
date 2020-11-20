package com.cgmn.msxl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.*;

public class AppSqlHelper extends SQLiteOpenHelper {
    private static final int VERSION =12;
    public final static String DB_NAME = "app.db";

    public AppSqlHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    //数据库第一次创建时被调用
    public void onCreate(SQLiteDatabase db) {
        StringBuffer users = new StringBuffer();
        users.append("CREATE TABLE users(");
        users.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        users.append("phone VARCHAR(32),");
        users.append("user_name VARCHAR(64),");
        users.append("password VARCHAR(128),");
        users.append("token VARCHAR(64),");
        users.append("last_active INTEGER");
        users.append(");");
        db.execSQL(users.toString());
        db.execSQL("ALTER TABLE users ADD gender INTEGER;");
        db.execSQL("ALTER TABLE users ADD signature VARCHAR(225);");
        db.execSQL("ALTER TABLE users ADD image_cut text;");
        db.execSQL("ALTER TABLE users ADD user_id INTEGER;");

        StringBuffer mode = new StringBuffer();
        mode.append("CREATE TABLE user_modes(");
        mode.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        mode.append("user_id INTEGER,");
        mode.append("mode_type INTEGER,");
        mode.append("model_status INTEGER");
        mode.append(");");
        db.execSQL(mode.toString());

    }

    //软件版本号发生改变时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE users ADD user_id INTEGER;");
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

    public void upsert(String tableName, ContentValues values, String key, String condition) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            String sql = String.format("SELECT id FROM %s WHERE %s = ? %s", tableName, key, condition);
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

    public void excuteSql(String sql) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.execSQL(sql);
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
        String[] fields = {"id","user_name", "phone", "password", "token","gender","signature", "image_cut", "user_id"};
        List<Map<String, Object>> list = query(sql, params, fields);
        if(CommonUtil.isEmpty(list) || list.size() == 0){
            return null;
        }
        return list.get(0);
    }

    public List<Map<String, Object>> getAccountList(){
        String sql = "SELECT * FROM users";
        String[] params = new String[]{};
        String[] fields = {"phone", "password"};
        List<Map<String, Object>> list = query(sql, params, fields);
        return list;
    }

    public Map<String, String> getUserModelSettings(String userId){
        String sql = "SELECT * FROM user_modes where user_id=" + userId;
        String[] params = new String[]{};
        String[] fields = new String[]{"mode_type","model_status"};
        List<Map<String, Object>> list = query(sql, params, fields);
        Map<String, String> hash = new HashMap<>();
        if(!CommonUtil.isEmpty(list)){
            for(Map<String, Object> item : list){
                String modType = (String) item.get("mode_type");
                String modelStatus = (String) item.get("model_status");
                hash.put(modType, modelStatus);
            }
        }

        return hash;
    }
}