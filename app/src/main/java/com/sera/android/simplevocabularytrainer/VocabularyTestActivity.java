package com.sera.android.simplevocabularytrainer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sera.android.simplevocabularytrainer.DAO.VocabularyDAO;
import com.sera.android.simplevocabularytrainer.DAO.Word;
import com.sera.android.simplevocabularytrainer.DAO.WordsSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


public class VocabularyTestActivity extends Activity
        implements View.OnClickListener
{
    // Base de datos
    String currentVocabularyName;
    VocabularyDAO currentDB;
    static ArrayList<String> wordsArray;

    // Componentes
    TextView lblQuestion;
    TextView tvPartOfSpeech;
    //EditText etAnswer;
    AutoCompleteTextView etAnswer;
    TextView lblCorrectAnswer,tvCorrectAnswer;
    Button btnNext,btnEnd,btnOK,btnEdit;
    TextView tvStatus;
    Word currentWord;
    boolean foreign;

    // Sonidos
    MediaPlayer mediaPlayerSuccess;
    MediaPlayer mediaPlayerFail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_test);

        // Guardamos componentes para usarlos luego
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        lblQuestion = (TextView)findViewById(R.id.lblQuestion);
        //etAnswer = (EditText) findViewById(R.id.etAnswer);
        etAnswer = (AutoCompleteTextView) findViewById(R.id.etAnswer);
        tvPartOfSpeech = (TextView) findViewById(R.id.tvPartOfSpeech);
        btnOK = (Button) findViewById(R.id.btnOK);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnEnd = (Button) findViewById(R.id.btnEnd);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        lblCorrectAnswer = (TextView) findViewById(R.id.lblCorrectAnswer);
        tvCorrectAnswer = (TextView) findViewById(R.id.tvCorrectAnswer);

        // Enlazamos listeners
        btnOK.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnEnd.setOnClickListener(this);
        btnEdit.setOnClickListener(this);

        // Sacamos parametros
        Intent intent = getIntent();
        currentVocabularyName = intent.getStringExtra("vocabulary_name");
        foreign = intent.getStringExtra("test_type").equals("foreign");

        // Leemos base de datos
        currentDB = new VocabularyDAO(this,currentVocabularyName);

        // Ponemos titulo
        setTitle(currentVocabularyName);

        // Configuramos pantalla
        reloadWordsList();
        setupAskWord();

        mediaPlayerSuccess = MediaPlayer.create(this, R.raw.clap);
        mediaPlayerFail = MediaPlayer.create(this, R.raw.pow);

        // Buscar AdView como recurso y cargar una solicitud.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    private void ocultarComponentesRespuesta() {
        etAnswer.setText("");
        lblCorrectAnswer.setVisibility(View.INVISIBLE);
        tvCorrectAnswer.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
        btnEdit.setVisibility(View.INVISIBLE);
        btnEnd.setVisibility(View.VISIBLE);
        btnOK.setVisibility(View.VISIBLE);
    }


    private void mostrarComponentesRespuesta() {
        lblCorrectAnswer.setVisibility(View.VISIBLE);
        tvCorrectAnswer.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.VISIBLE);
        btnEnd.setVisibility(View.VISIBLE);
        btnOK.setVisibility(View.INVISIBLE);
    }


    private void mostrarComponentesFinTest() {
        lblQuestion.setText(R.string.str_vocabulary_complete);
        lblCorrectAnswer.setVisibility(View.INVISIBLE);
        tvCorrectAnswer.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
        btnEdit.setVisibility(View.INVISIBLE);
        btnEnd.setVisibility(View.VISIBLE);
        btnOK.setVisibility(View.INVISIBLE);
        etAnswer.setVisibility(View.INVISIBLE);
    }


    private void reloadWordsList() {
        // Actualizamos estado
        tvStatus.setText(currentVocabularyName+" ("+currentDB.countLearnedWords()+"/"+currentDB.countTotalWords()+": " + (100*currentDB.countLearnedWords()/currentDB.countTotalWords()) +  "%)");

        // Y configuramos el autocompletar con todas las palabras
        WordsSet words = currentDB.readWords();
        Iterator<Word> it = words.iterator();
        wordsArray= new ArrayList<String>();
        while (it.hasNext()) {
            Word w = it.next();
            String currentStr="";
            if (foreign) {
                currentStr = w.strForeign;
            } else {
                currentStr = w.strNative;
            }

            String [] strArray = currentStr.split(",");
            for (int i=0; i<strArray.length; i++) {
                String str = strArray[i];
                if (str.indexOf('(')>-1 && str.indexOf(')')>-1 && str.indexOf('(')<str.indexOf(')')) {
                    str = str.substring(0,str.indexOf('(')) + " " + str.substring(str.indexOf(')')+1);
                }
                wordsArray.add(str.trim());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, wordsArray);
        etAnswer.setThreshold(1);
        etAnswer.setAdapter(adapter);
    }

    private void setupAskWord() {
        currentWord = currentDB.getWordToLearn();
        if (currentWord!=null) {
            if (foreign) {
                lblQuestion.setText(currentWord.strNative);
                tvCorrectAnswer.setText(currentWord.strForeign);
                //String [] strArray = currentWord.strForeign.split(",");
                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, strArray);
                //etAnswer.setThreshold(1);
                //etAnswer.setAdapter(adapter);
            } else {
                lblQuestion.setText(currentWord.strForeign);
                tvCorrectAnswer.setText(currentWord.strNative);
                //String [] strArray = currentWord.strNative.split(",");
                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, strArray);
                //etAnswer.setThreshold(1);
                //etAnswer.setAdapter(adapter);
            }
            if (currentWord.partOfSpeech!=null && !currentWord.partOfSpeech.equals("")) {
                tvPartOfSpeech.setText(currentWord.partOfSpeech);
            } else {
                TextView lblPartOfSpeech = (TextView)findViewById(R.id.lblPartOfSpeech);
                lblPartOfSpeech.setVisibility(View.INVISIBLE);
                tvPartOfSpeech.setVisibility(View.INVISIBLE);
            }

            ocultarComponentesRespuesta();

        } else {
            mostrarComponentesFinTest();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vocabulary_test, menu);
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

    public void onClick(View componente) {
        // Cambiar vocabulario
        Intent intent;
        switch(componente.getId()){
            case R.id.btnOK:
                currentWord.timesAsked++;
                String respuesta = etAnswer.getText().toString();
                String correcta = tvCorrectAnswer.getText().toString();
                if (checkRespuesta(respuesta,correcta)) {
                    currentWord.timesSuccess++;

                    correcta = correcta + " -> "+getResources().getString(R.string.str_correct);
                    tvCorrectAnswer.setText(correcta);
                    tvCorrectAnswer.setTextColor(Color.GREEN);
                    mediaPlayerSuccess.start();
                } else {
                    currentWord.timesFailed++;
                    correcta = correcta + " -> "+getResources().getString(R.string.str_fail);
                    tvCorrectAnswer.setText(correcta);
                    tvCorrectAnswer.setTextColor(Color.parseColor("#FFA70900"));
                    mediaPlayerFail.start();
                }

                VocabularyDAO currentDB = new VocabularyDAO(this,currentVocabularyName);
                currentDB.updateWord(currentWord);
                tvStatus.setText(currentVocabularyName+" ("+currentDB.countLearnedWords()+"/"+currentDB.countTotalWords()+": " + (100*currentDB.countLearnedWords()/currentDB.countTotalWords()) +  "%)");

                mostrarComponentesRespuesta();
            break;
            case R.id.btnNext:
                setupAskWord();
                break;

            case R.id.btnEdit:
                editItem(currentWord);
                reloadWordsList();
                //finish();
                //startActivity(getIntent());
                break;

            case R.id.btnEnd:
                intent = new Intent(this, VocabularyVisualization.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra("vocabulary_name", this.currentVocabularyName);
                if (currentWord!=null) {
                    intent.putExtra("word_id", currentWord.id);
                }
                startActivity(intent);
                break;
        }
    }

    private boolean checkRespuesta(String respuesta,String correcta) {
        boolean ret=false;

        if (respuesta!=null && !respuesta.equals("")) {
            respuesta = respuesta.trim().toLowerCase();
            StringTokenizer st = new StringTokenizer(correcta,";,()");
            while (st.hasMoreTokens() && !ret) {
                String next = st.nextToken().trim().toLowerCase();
                int abrePar = next.indexOf("(");
                int cierraPar = next.indexOf(")");
                if (abrePar>-1 && cierraPar>-1 && cierraPar>abrePar) {
                    next = next.replaceAll("\\(.*\\)", "").trim();
                }
                ret = (respuesta.equals(next));
            }
        }

        return ret;
    }

    public void editItem(Word w)
    {
        Intent intent = new Intent(this,WordEdition.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra("word_id",""+ w.id);
        intent.putExtra("vocabulary_name", this.currentVocabularyName);
        startActivity(intent);
    }
}
