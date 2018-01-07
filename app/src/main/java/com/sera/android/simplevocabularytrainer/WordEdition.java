package com.sera.android.simplevocabularytrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sera.android.simplevocabularytrainer.DAO.VocabularyDAO;
import com.sera.android.simplevocabularytrainer.DAO.Word;


public class WordEdition extends Activity
        implements View.OnClickListener
{
    EditText newWordNative;
    EditText newWordPartOfSpeech;
    EditText newWordNotes;
    EditText newWordForeign;
    Word currentWord;
    String vocabularyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_edition);
        TextView tvTitle = (TextView)findViewById(R.id.tvTitle);

        Intent intent = getIntent();
        // 1) Sacamos informacion de la BD a partir del id de palabra
        vocabularyName = intent.getStringExtra("vocabulary_name");
        String strWordId = intent.getStringExtra("word_id");
        if (strWordId!=null && vocabularyName != null) {
            VocabularyDAO currentDB =  new VocabularyDAO(this,vocabularyName);
            currentWord = currentDB.getWord(Long.parseLong(strWordId));
            tvTitle.setText(R.string.str_edit_word);
            Log.d(this.getClass().getName(),"Editando palabra "+currentWord.toString());
        } else {
            tvTitle.setText(R.string.str_add_word);
        }

        Button btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        // Buscar AdView como recurso y cargar una solicitud.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    public void onStart() {
        super.onStart();
        newWordNative = (EditText) findViewById(R.id.tvNative);
        newWordPartOfSpeech = (EditText) findViewById(R.id.tvPartOfSpeech);
        newWordNotes = (EditText) findViewById(R.id.tvNotes);
        newWordForeign = (EditText) findViewById(R.id.tvForeign);

        if (currentWord!=null) {
            newWordNative.setText(currentWord.strNative);
            newWordPartOfSpeech.setText(currentWord.partOfSpeech);
            newWordNotes.setText(currentWord.notes);
            newWordForeign.setText(currentWord.strForeign);
        } else {
            newWordNative.setText("");
            newWordPartOfSpeech.setText("");
            newWordNotes.setText("");
            newWordForeign.setText("");
        }
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //((InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editProductName, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_word_edition, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /* ***************** EVENTOS ******************** */

    /**
     * Boton->onClick
     */
    public void onClick(View btn) {
        switch (btn.getId()) {
            case R.id.btnOK:
                if (currentWord==null) {
                    currentWord = new Word(newWordNative.getText().toString(), newWordForeign.getText().toString(), newWordPartOfSpeech.getText().toString(), newWordNotes.getText().toString());
                    VocabularyDAO currentDB = new VocabularyDAO(this, vocabularyName);
                    currentDB.addWord(currentWord);
                } else {
                    currentWord.strNative = newWordNative.getText().toString();
                    currentWord.strForeign = newWordForeign.getText().toString();
                    currentWord.partOfSpeech = newWordPartOfSpeech.getText().toString();
                    currentWord.notes = newWordNotes.getText().toString();
                    currentWord.timesAsked=0;
                    currentWord.timesSuccess=0;
                    currentWord.timesFailed=0;
                    VocabularyDAO currentDB = new VocabularyDAO(this, vocabularyName);
                    currentDB.updateWord(currentWord);
                }
                finish();
                break;
            case R.id.btnCancel:
                finish();
                break;
        }
    }

}
