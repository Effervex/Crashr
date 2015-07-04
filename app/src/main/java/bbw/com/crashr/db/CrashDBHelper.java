package bbw.com.crashr.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by henry on 4/07/15.
 */
public class CrashDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "crash.db";
    private static final int DATABASE_VERSION = 1;

    public CrashDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        IncidentsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(CrashDBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ".");
        IncidentsTable.onUpgrade(db);
    }

}
