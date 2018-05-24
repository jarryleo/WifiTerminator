package cn.leo.wifiterminator.db;

/**
 * Created by JarryLeo on 2017/2/26.
 */

public interface WifiDbConfig {
    String DB_NAME = "wifi.db";
    int VERSION = 1;
    String TABLE_NAME = "wifiInfo";
    String COLUMN_ID = "_id";
    String COLUMN_SSID = "ssid";
    String COLUMN_BSSID = "bssid";
    String COLUMN_PSW = "password";
    String COLUMN_UPDATE = "last_update";
    String COLUMN_SUCCESS_COUNT = "success_count";

    String CREATE_SQL = "create table " + TABLE_NAME +
            " (" + COLUMN_ID + " integer primary key autoincrement," +
            COLUMN_SSID + " varchar(30)," +
            COLUMN_BSSID + " varchar(30)," +
            COLUMN_PSW + " varchar(50)," +
            COLUMN_UPDATE + " varchar(30)," +
            COLUMN_SUCCESS_COUNT + " integer" + ");";

}
