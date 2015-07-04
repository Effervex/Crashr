package bbw.com.crashr.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import java.io.*;
import java.sql.SQLException;

/**
 * Created by henry on 4/07/15.
 */
public class CrashDBHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/bbw.com.crashr/databases/";
    private static String DB_NAME = "crash.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;

    public CrashDBHelper (Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    public void crateDatabase() throws IOException {
        boolean vtVarMi = isDatabaseExist();

        if (!vtVarMi) {
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private boolean isDatabaseExist() {
        SQLiteDatabase kontrol = null;

        try {
            String myPath = DB_PATH + DB_NAME;
            kontrol = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            kontrol = null;
        }

        if (kontrol != null) {
            kontrol.close();
        }
        return kontrol != null ? true : false;
    }

    public void openDataBase() throws SQLException {

        // Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

    }

    public Cursor Sample_use_of_helper() {

        return myDataBase.query("TABLE_NAME", null, null, null, null, null, null);
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
