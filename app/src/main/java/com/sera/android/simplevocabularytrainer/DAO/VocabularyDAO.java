package com.sera.android.simplevocabularytrainer.DAO;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.nio.charset.Charset;


/**
 * Created by Sera on 25/04/2015.
 */
public class VocabularyDAO {
    private VocabularyDBOpenHelper helper;

    public VocabularyDAO(Activity parent,String name) {
         this.helper = new VocabularyDBOpenHelper(parent,name);
    }

    public boolean renameDatabase(String newName) {
        File databaseFile = helper.context.getDatabasePath(helper.dbName);
        File newDatabaseFile = new File(databaseFile.getParentFile(), newName);
        boolean ret = databaseFile.renameTo(newDatabaseFile);
        if (ret) {
            helper.dbName = newName;
        }
        return ret;
    }


    public WordsSet readWords()
    {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        WordsSet items = new WordsSet();

        String sql = "SELECT * from words order by word_native";
        Log.d(this.getClass().getName(), sql);
        Cursor c = wordsDB.rawQuery(sql,null);
        c.moveToFirst();

        Word w;
        while (!c.isAfterLast()) {
            w = readWord(c);
            //Log.d(this.getClass().getName(), "Leida palabra: ["+w.toString()+"]");
            items.add(w);

            c.moveToNext();
        }
        c.close();
        wordsDB.close();

        return items;
    }


    public Word getWord(long id)
    {
        Word ret = null;

        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        String query = "SELECT * from words where _id="+id;
        Log.d(this.getClass().getName(), query);

        Cursor c = wordsDB.rawQuery(query,null);
        c.moveToFirst();
        if (!c.isAfterLast()) {
            ret = readWord(c) ;
        } else {
            Log.d(this.getClass().getName(),"Palabra no encontrada! ["+id+"]");
        }
        wordsDB.close();

        return ret;
    }

    public Word getWordToLearn()
    {
        Word ret = null;

        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        String query = "SELECT * from words where learned=0 order by random() limit 1";
        Log.d(this.getClass().getName(), query);

        Cursor c = wordsDB.rawQuery(query,null);
        c.moveToFirst();
        if (!c.isAfterLast()) {
            ret = readWord(c) ;
            Log.d(this.getClass().getName(),"Palabra leida: "+ret.toString());
        } else {
            Log.d(this.getClass().getName(),"Palabra no encontrada!");
        }
        wordsDB.close();

        return ret;
    }

    private Word readWord(Cursor c)
    {
        long id = c.getLong(c.getColumnIndex("_id"));
        String strNative = new String(c.getString(c.getColumnIndex("word_native")).getBytes(Charset.forName("ISO-8859-1")));

        String foreign = c.getString(c.getColumnIndex("word_foreign"));
        foreign = new String(foreign.getBytes(Charset.forName("UTF-8")));

        String partOfSpeech = c.getString(c.getColumnIndex("part_of_speech"));
        String notes = c.getString(c.getColumnIndex("notes"));
        int timesAsked = c.getInt(c.getColumnIndex("times_asked"));
        int timesSuccess = c.getInt(c.getColumnIndex("times_success"));
        int timesFailed = c.getInt(c.getColumnIndex("times_failed"));
        boolean learned = (c.getInt(c.getColumnIndex("learned"))==1);

        return new Word(id,strNative,foreign,partOfSpeech,notes,timesAsked,timesSuccess,timesFailed,learned);
    }


    public long addWord(Word w)
    {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        ContentValues wValues = new ContentValues();
        wValues.put("word_native",w.strNative);
        wValues.put("word_foreign",w.strForeign);
        wValues.put("part_of_speech",w.partOfSpeech);
        wValues.put("notes",w.notes);
        wValues.put("times_asked",w.timesAsked);
        wValues.put("times_success",w.timesSuccess);
        wValues.put("times_failed",w.timesFailed);
        wValues.put("learned", w.learned() ? 1 : 0);
        //Log.d(this.getClass().getName(), "Añadiendo palabra: " + wValues.valueSet().toString());

        long wId = wordsDB.insert("words", null, wValues);
        wordsDB.close();

        return wId;
    }

    public void updateWord(Word w)
    {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        ContentValues wValues = new ContentValues();
        wValues.put("word_native",w.strNative);
        wValues.put("word_foreign",w.strForeign);
        wValues.put("part_of_speech",w.partOfSpeech);
        wValues.put("notes",w.notes);
        wValues.put("times_asked",w.timesAsked);
        wValues.put("times_success",w.timesSuccess);
        wValues.put("times_failed",w.timesFailed);
        wValues.put("learned",w.learned()?1:0);

        Log.d(this.getClass().getName(),"Actualizando palabra: "+w.toString());
        String whereClause = "_id=?";
        String[] whereArgs = {""+w.id};
        long pId = wordsDB.update("words", wValues, whereClause, whereArgs);
        wordsDB.close();
    }

    public void deleteWord(Word w)
    {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();

        Log.d(this.getClass().getName(),"Borrando palabra: "+w.strNative);
        String whereClause = "_id=?";
        String[] whereArgs = {""+w.id};
        long pId = wordsDB.delete("words", whereClause, whereArgs);
        wordsDB.close();
    }

    public long countTotalWords()
    {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        String sql = "SELECT count(*) from words";
        Cursor c = wordsDB.rawQuery(sql,null);
        c.moveToFirst();
        long ret = c.getLong(0);
        Log.d(this.getClass().getName(), sql+" -> " + ret);
        wordsDB.close();
        return ret;
    }

    public long countLearnedWords()
    {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        //String sql = "SELECT count(*) from words where learned=1";
        String sql = "SELECT count(*) from words where learned=1";
        Cursor c = wordsDB.rawQuery(sql,null);
        c.moveToFirst();
        long ret = c.getLong(0);
        Log.d(this.getClass().getName(), sql+" -> " + ret);
        wordsDB.close();
        return ret;
    }

    public long countQuestionsAttempted()
    {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        String sql = "SELECT sum(times_asked) from words";
        Cursor c = wordsDB.rawQuery(sql,null);
        c.moveToFirst();
        long ret = c.getLong(0);
        Log.d(this.getClass().getName(), sql+" -> " + ret);
        wordsDB.close();
        return ret;
    }

    public long countQuestionsSuccess() {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        String sql = "SELECT sum(times_success) from words";
        Cursor c = wordsDB.rawQuery(sql,null);
        c.moveToFirst();
        long ret = c.getLong(0);
        Log.d(this.getClass().getName(), sql+" -> " + ret);
        wordsDB.close();
        return ret;
    }

    public long countQuestionsFailed() {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        String sql = "SELECT sum(times_failed) from words";
        Cursor c = wordsDB.rawQuery(sql,null);
        c.moveToFirst();
        long ret = c.getLong(0);
        Log.d(this.getClass().getName(), sql+" -> " + ret);
        wordsDB.close();
        return ret;
    }

    public void resetStatistics() {
        SQLiteDatabase wordsDB = this.helper.getWritableDatabase();
        ContentValues wValues = new ContentValues();
        wValues.put("times_asked",0);
        wValues.put("times_success",0);
        wValues.put("times_failed",0);
        wValues.put("learned",0);

        Log.d(this.getClass().getName(),"Reseteando estadisticas: "+wValues.toString());
        long pId = wordsDB.update("words", wValues, null, null);
        wordsDB.close();
    }


}
