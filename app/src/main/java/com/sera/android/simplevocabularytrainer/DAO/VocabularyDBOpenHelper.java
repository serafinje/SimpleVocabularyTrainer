package com.sera.android.simplevocabularytrainer.DAO;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Sera on 25/04/2015.
 */
public class VocabularyDBOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    protected String dbName;
    protected Context context;

    public VocabularyDBOpenHelper(Context context, String name ) {
        super(context, name, null, DATABASE_VERSION);
        this.dbName = name;
        this.context = context;
        Log.d(this.getClass().getName(),"Inicializando helper para BD: "+dbName);

    }

    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        if (!db.isOpen()) {
            Log.d(this.getClass().getName(),"Abriendo BD: "+dbName);
            db = context.openOrCreateDatabase(
                    dbName,
                    SQLiteDatabase.OPEN_READWRITE, null);
        }
        //Log.d(this.getClass().getName(),"Devolviendo BD: "+dbName);
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE words ("
                + "_id INTEGER PRIMARY KEY,"
                + "word_native TEXT,"
                + "word_foreign TEXT,"
                + "part_of_speech TEXT,"
                + "notes TEXT,"
                + "times_asked INTEGER,"
                + "times_success INTEGER,"
                + "times_failed INTEGER,"
                + "learned INTEGER"
                + ");";
        Log.d(this.getClass().getName(),"Creando tabla en BD ["+dbName+"] "+sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
