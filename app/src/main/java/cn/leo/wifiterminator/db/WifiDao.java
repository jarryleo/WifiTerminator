package cn.leo.wifiterminator.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cn.leo.wifiterminator.bean.WifiBean;

/**
 * Created by JarryLeo on 2017/2/26.
 */

public class WifiDao {
    private static WifiDao mDao;
    private static WifiDbHelper dbHelper;
    private String psw;

    private WifiDao() {
    }

    /**
     * 数据库操作类最好用单例设计模式
     *
     * @param context
     * @return
     */
    public static WifiDao getDao(Context context) {
        if (mDao == null) {
            dbHelper = new WifiDbHelper(context);
            mDao = new WifiDao();
        }
        return mDao;
    }

    /**
     * 储存数据
     *
     * @param bean
     * @return
     */
    public synchronized boolean addWifi(WifiBean bean) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WifiDbConfig.COLUMN_SSID, bean.SSID);
        values.put(WifiDbConfig.COLUMN_BSSID, bean.BSSID);
        values.put(WifiDbConfig.COLUMN_PSW, bean.password);
        values.put(WifiDbConfig.COLUMN_UPDATE, System.currentTimeMillis());
        values.put(WifiDbConfig.COLUMN_SUCCESS_COUNT, 0);
        long insert = db.insert(WifiDbConfig.TABLE_NAME, null, values);
        db.close();
        return insert != 0;
    }

    /**
     * 查询数据库存在的密码
     *
     * @param bean
     * @return
     */
    public synchronized String getPsw(WifiBean bean) {
        String psw = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{WifiDbConfig.COLUMN_PSW};
        String selection = WifiDbConfig.COLUMN_SSID + "=? and "
                + WifiDbConfig.COLUMN_BSSID + "=?";
        String[] args = new String[]{bean.SSID, bean.BSSID};
        Cursor cursor = db.query(WifiDbConfig.TABLE_NAME, columns, selection, args, null, null, null);
        if (cursor.moveToNext()) {
            psw = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return psw;
    }

    /**
     * 查询密码最后更新时间
     *
     * @param bean
     * @return
     */
    public synchronized long getLastUpdate(WifiBean bean) {
        long update = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{WifiDbConfig.COLUMN_UPDATE};
        String selection = WifiDbConfig.COLUMN_SSID + "=? and "
                + WifiDbConfig.COLUMN_BSSID + "=?";
        String[] args = new String[]{bean.SSID, bean.BSSID};
        Cursor cursor = db.query(WifiDbConfig.TABLE_NAME, columns, selection, args, null, null, null);
        if (cursor.moveToNext()) {
            update = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        return update;
    }

    /**
     * 获取该密码成功次数
     *
     * @param bean
     * @return
     */
    public synchronized int getSuccessCount(WifiBean bean) {
        int count = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{WifiDbConfig.COLUMN_SUCCESS_COUNT};
        String selection = WifiDbConfig.COLUMN_SSID + "=? and "
                + WifiDbConfig.COLUMN_BSSID + "=?";
        String[] args = new String[]{bean.SSID, bean.BSSID};
        Cursor cursor = db.query(WifiDbConfig.TABLE_NAME, columns, selection, args, null, null, null);
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /**
     * 更新密码库
     *
     * @param bean
     * @return
     */
    public synchronized boolean updateWifi(WifiBean bean) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WifiDbConfig.COLUMN_PSW, bean.password);
        values.put(WifiDbConfig.COLUMN_UPDATE, System.currentTimeMillis());
        values.put(WifiDbConfig.COLUMN_SUCCESS_COUNT, 0);
        String whereClause = WifiDbConfig.COLUMN_SSID + "=? and "
                + WifiDbConfig.COLUMN_BSSID + "=?";
        String[] whereArgs = new String[]{bean.SSID, bean.BSSID};
        int update = db.update(WifiDbConfig.TABLE_NAME, values, whereClause, whereArgs);
        db.close();
        return update > 0;
    }

    /**
     * 成功次数+1
     *
     * @param bean
     */
    public synchronized void successPlus(WifiBean bean) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update " + WifiDbConfig.TABLE_NAME + " set " +
                WifiDbConfig.COLUMN_SUCCESS_COUNT + "=" +
                WifiDbConfig.COLUMN_SUCCESS_COUNT + "+1 where " +
                WifiDbConfig.COLUMN_SSID + "='" + bean.SSID + "' and " +
                WifiDbConfig.COLUMN_BSSID + "='" + bean.BSSID + "'";
        db.execSQL(sql);
        db.close();
    }
}
