package com.sera.android.simplevocabularytrainer.DAO;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by Sera on 24/04/2015.
 */
public class Word implements Comparable<Word>, Serializable
{
    public long id;
    public String strNative;
    public String strForeign;
    public String partOfSpeech;
    public String notes;
    public int timesAsked;
    public int timesSuccess;
    public int timesFailed;
    public boolean learned() { return (timesAsked>0 && timesSuccess>timesFailed);};

    public Word() {
    }

    public Word(long id, String strNative, String strForeign, String partOfSpeech, String notes, int timesAsked,int timesSuccess, int timesFailed, boolean learned) {
        this.id = id;
        this.strNative = strNative;
        this.strForeign = strForeign;
        this.partOfSpeech = partOfSpeech;
        this.notes = notes;
        this.timesAsked = timesAsked;
        this.timesSuccess = timesSuccess;
        this.timesFailed= timesFailed;
        //this.learned = learned;

        //this.learned = false;
        //if (timesAsked>0 && timesSuccess>timesFailed) this.learned=true;
    }

    public Word(String strNative, String strForeign, String partOfSpeech, String notes) {
        this.strNative = strNative;
        this.strForeign = strForeign;
        this.partOfSpeech = partOfSpeech;
        this.notes = notes;
        this.timesAsked = 0;
        this.timesSuccess = 0;
        this.timesFailed= 0;
        //this.learned = false;
    }

    public int compareTo(Word arg0)
    {
        return strNative.compareToIgnoreCase(arg0.strNative);
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", strNative='" + strNative + '\'' +
                ", strForeign='" + strForeign + '\'' +
                ", partOfSpeech='" + partOfSpeech + '\'' +
                ", notes='" + notes + '\'' +
                ", timesAsked=" + timesAsked +
                ", timesSuccess=" + timesSuccess +
                ", timesFailed=" + timesFailed +
                ", learned=" + learned() +
                '}';
    }
}
