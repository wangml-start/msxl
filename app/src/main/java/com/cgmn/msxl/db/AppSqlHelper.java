package com.cgmn.msxl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppSqlHelper extends SQLiteOpenHelper {

    public AppSqlHelper(Context context) {
        super(context, "app.db", null, 1);
    }

    @Override
    //数据库第一次创建时被调用
    public void onCreate(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE users");
        sql.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append("account VARCHAR(32)");
        sql.append("user_name VARCHAR(64)");
        sql.append("password VARCHAR(128)");
        sql.append("token VARCHAR(64)");
        sql.append("last_active int");
        sql.append(")");
        db.execSQL(sql.toString());

    }
    //软件版本号发生改变时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("ALTER TABLE person ADD phone VARCHAR(12) NULL");
    }
}