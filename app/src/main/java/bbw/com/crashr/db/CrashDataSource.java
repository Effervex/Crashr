package bbw.com.crashr.db;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by henry on 4/07/15.
 */
public class CrashDataSource {

    private SQLiteDatabase database;
    private CrashDBHelper dbHelper;
    private SimpleDateFormat dateFormat;

    public CrashDataSource(Context context) {
        dbHelper = new CrashDBHelper(context);
        dateFormat = new SimpleDateFormat("dd/MM/yyHHmm");
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<Incident> getLocalisedIncidents(double minLat, double maxLat, double minLong, double maxLong) {

        List<Incident> incidents = new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT * FROM " + IncidentsTable.TABLE_INCIDENTS + " WHERE LAT>=" + minLat + " AND LAT<=" + maxLat + " AND LNG>=" + minLong + " AND LNG<=" + maxLong, null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            Incident i = cursorToIncident(cursor);
            incidents.add(i);
            cursor.moveToNext();
        }

        cursor.close();

        return incidents;
    }

    private Incident cursorToIncident(Cursor cursor) {

        Incident i = new Incident();

        i._id = cursor.getLong(0);

        try {
            i.date = dateFormat.parse(cursor.getString(1));
        }
        catch(Exception e) {
            i.date = new Date();
        }

        i.causes = cursor.getString(2).split(" ");
        i.objectsStruck = cursor.getString(3);
        i.roadWet = cursor.getString(4);
        i.speedLimit = cursor.getLong(5);
        i.fatalCount = cursor.getLong(6);
        i.severeCount = cursor.getLong(7);
        i.minorCount = cursor.getLong(8);
        i.latitude = cursor.getDouble(9);
        i.longitude = cursor.getDouble(10);

        return i;
    }

}
