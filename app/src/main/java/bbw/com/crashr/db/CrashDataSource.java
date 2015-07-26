package bbw.com.crashr.db;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by henry on 4/07/15.
 */
public class CrashDataSource {

    private SQLiteDatabase database;
    private CrashDBHelper dbHelper;
    private SimpleDateFormat dateFormat;

    public CrashDataSource(Context context) throws SQLException {
        dbHelper = new CrashDBHelper(context);
        dateFormat = new SimpleDateFormat("dd/MM/yy HHmm");
    }

    public void open() {
        try {
            dbHelper.createDataBase();
            dbHelper.openDataBase();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        dbHelper.close();
        database = dbHelper.getWritableDatabase();

        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        c.moveToFirst();

        while(!c.isAfterLast()) {
            Log.w("DB", c.getString(0));
            c.moveToNext();
        }
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
            i.date = dateFormat.parse(cursor.getString(1) + " " + cursor.getString(2));
        }
        catch(Exception e) {
            i.date = new Date();
        }

        String[] tmp = cursor.getString(3).split(" ");
        int numBlank = 0;

        for(int j = 0; j < tmp.length; j++)
        {
            if(tmp[j] == "")
            {
                numBlank++;
            }
        }

        i.causes = new String[tmp.length - numBlank];

        int k = 0;

        for(int j = 0; j < tmp.length; j++)
        {
            if(tmp[j] != "")
            {
                i.causes[k] = tmp[j];
                k++;
            }
        }

        i.objectsStruck = cursor.getString(4);
        i.roadWet = cursor.getString(5);
        i.weather = cursor.getString(6);
        i.speedLimit = cursor.getLong(7);
        i.fatalCount = cursor.getLong(8);
        i.severeCount = cursor.getLong(9);
        i.minorCount = cursor.getLong(10);
        i.latitude = cursor.getDouble(11);
        i.longitude = cursor.getDouble(12);

        return i;
    }

}
