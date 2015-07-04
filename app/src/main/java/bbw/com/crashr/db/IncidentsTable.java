package bbw.com.crashr.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by henry on 4/07/15.
 */
public class IncidentsTable {

    public static final String TABLE_INCIDENTS = "incidents";

    private static final String TABLE_CREATE = "CREATE TABLE "
            + TABLE_INCIDENTS
            + "("
            + "_id integer primary key autoincrement, "
            + "TLA_NAME text, "
            + "CRASH_ROAD text, "
            + "CRASH_DIST integer, "
            + "CRASH_DIRN text, "
            + "INTSN integer, "
            + "SIDE_ROAD text, "
            + "CRASH_ID integer, "
            + "CRASH_DATE text, "
            + "CRASH_DOW text, "
            + "CRASH_TIME integer, "
            + "MVMT text, "
            + "CAUSES text, "
            + "OBJECTS_STRUCK text, "
            + "ROAD_CURVE text, "
            + "ROAD_WET text, "
            + "LIGHT text, "
            + "WTHRA text, "
            + "JUNC_TYPE text, "
            + "TRAF_CTRL text, "
            + "ROAD_MARK text, "
            + "SPD_LIM integer, "
            + "CRASH_FATAL_CNT integer, "
            + "CRASH_SEV_CNT integer, "
            + "CRASH_MIN_CNT integer, "
            + "PERS_AGE1 integer, "
            + "PERS_AGE2 integer, "
            + "LAT integer, "
            + "LNG integer"
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENTS);
    }

}
