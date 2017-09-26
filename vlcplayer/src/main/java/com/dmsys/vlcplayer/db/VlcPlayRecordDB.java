package com.dmsys.vlcplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/8/29.
 */

public class VlcPlayRecordDB extends SQLiteOpenHelper {

    private final static String DB_NAME = "VlcPlayRecordDB.db";
    private final static int VERSION = 2;
    private final String TABLE_NAME = "playrecord";
    private final String _path = "path";
    private final String _subtitle_switch = "subtitle_switch";
    private final String _subtitle_path = "subtitle_path";


    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + _path + " TEXT UNIQUE ON CONFLICT REPLACE,"
            + _subtitle_switch + " INTEGER,"
            + _subtitle_path + " TEXT );";


    public VlcPlayRecordDB(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    private static volatile VlcPlayRecordDB mInstance;

    public static VlcPlayRecordDB getInstance(Context context) {

        if (null == mInstance) {
            synchronized (VlcPlayRecordDB.class) {
                if (null == mInstance) {
                    mInstance = new VlcPlayRecordDB(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }


    public synchronized PlayRecordBean getRecordByUrl(String url) {
        PlayRecordBean ret = null;

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + _path + " =? ";

        Cursor c = null;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            c = db.rawQuery(sql, new String[]{url});

            if (c.getCount() > 1) {
                System.out.println("DMDeviceDB 数据库损坏了！");
            }

            if (c.moveToFirst()) {
                String path = c.getString(c.getColumnIndex(_path));
                int onOff = c.getInt(c.getColumnIndex(_subtitle_switch));
                String subtitle_path = c.getString(c.getColumnIndex(_subtitle_path));

                ret = new PlayRecordBean(onOff == 0, path,subtitle_path);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (null != db && db.isOpen()) {
                db.close();
            }
        }
        return ret;
    }


    public synchronized boolean addRecord(PlayRecordBean mPlayRecordBean) {
        SQLiteDatabase db = null;
        boolean ret = false;
        try {
            db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(_path, mPlayRecordBean.uri);
            values.put(_subtitle_switch, mPlayRecordBean.subtitle_onoff ? 0:1);
            values.put(_subtitle_path, mPlayRecordBean.subtitle_path);


            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            ret = true;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ret = false;
        } finally {
            if (null != db && db.isOpen()) {
                db.close();
            }
        }
        return ret;
    }


    public synchronized boolean deleteRecord(String path) {
        SQLiteDatabase db = null;
        boolean ret = false;
        try {
            db = getWritableDatabase();
            int affected = db.delete(TABLE_NAME, _path + "=? ", new String[]{path});
            if (affected >= 0) {
                ret = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != db && db.isOpen()) {
                db.close();
            }
        }
        return ret;
    }





}
