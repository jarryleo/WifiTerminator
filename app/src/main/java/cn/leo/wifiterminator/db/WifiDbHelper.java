package cn.leo.wifiterminator.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by JarryLeo on 2017/2/26.
 */

public class WifiDbHelper extends SQLiteOpenHelper {
    public WifiDbHelper(Context context) {
        super(context, WifiDbConfig.DB_NAME, null, WifiDbConfig.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WifiDbConfig.CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
