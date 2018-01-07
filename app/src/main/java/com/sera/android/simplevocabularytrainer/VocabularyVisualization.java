package com.sera.android.simplevocabularytrainer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.formats.NativeAd;
import com.sera.android.simplevocabularytrainer.DAO.VocabularyDAO;
import com.sera.android.simplevocabularytrainer.DAO.Word;
import com.sera.android.simplevocabularytrainer.DAO.WordsSet;

import java.util.ArrayList;
import java.util.List;


public class VocabularyVisualization extends Activity
        implements View.OnClickListener
{
    public WordListAdapter adapter;
    public WordsSet currentVocabulary;
    public Word currentWord;
    public String currentVocabularyName;
    VocabularyDAO currentDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_visualization);

        Intent intent = getIntent();
        // 1) Sacamos informacion de la BD a partir del nombre de vocabulario
        String vocabularyTitle = intent.getStringExtra("vocabulary_name");
        setTitle(getResources().getString(R.string.app_name) +": " + vocabularyTitle);
        TextView tvTitle = (TextView)findViewById(R.id.tvTitle);
        //tvTitle.setText(getResources().getString(R.string.app_name) +": " + vocabularyTitle);
        tvTitle.setText(vocabularyTitle);
        tvTitle.setOnClickListener(this);
        tvTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ImageButton ib1 = (ImageButton) findViewById(R.id.ibTitleUpdate);
                ImageButton ib2 = (ImageButton) findViewById(R.id.ibTitleCancelUpdate);
                if (hasFocus) {
                    if (ib1!=null)
                        ib1.setVisibility(ImageButton.VISIBLE);
                    if (ib2!=null)
                        ib2.setVisibility(ImageButton.VISIBLE);
                } else {
                    if (ib1!=null)
                        ib1.setVisibility(ImageButton.GONE);
                    if (ib2!=null)
                        ib2.setVisibility(ImageButton.GONE);
                }
            }
        });
        currentVocabularyName = vocabularyTitle;

        currentDB = new VocabularyDAO(this,vocabularyTitle);
        currentVocabulary = currentDB.readWords();

        // 2) Creación de un adapter con la informacion de palabras
        adapter = new WordListAdapter(this);

        // Inicializamos el ListView con nuestro adapter
        ListView lv = (ListView)findViewById(R.id.lvWordsList);
        lv.setAdapter(adapter);

        long last_word = intent.getLongExtra("word_id", 0);
        Log.d(this.getClass().getName(), "Vocabulario, palabra #" + last_word);
        // Buscamos la palabra
        int currentPos=adapter.getItemPos(last_word);
        Log.d(this.getClass().getName(), "Scroll a "+currentPos);
        lv.setSelection(currentPos);
        lv.smoothScrollToPosition(currentPos);

        // 3) Inicialización del resto de componentes
        ImageButton imageButton = (ImageButton)findViewById(R.id.ibTitleCancelUpdate);
        imageButton.setOnClickListener(this);
        imageButton.setVisibility(ImageButton.INVISIBLE);

        ImageButton ib = (ImageButton)findViewById(R.id.ibTitleUpdate);
        ib.setOnClickListener(this);
        imageButton.setVisibility(ImageButton.INVISIBLE);


        Button btnChangeVocabulary = (Button) findViewById(R.id.btnChangeVocabulary);
        btnChangeVocabulary.setOnClickListener(this);

        Button btnAddWord = (Button) findViewById(R.id.btnAddWord);
        btnAddWord.setOnClickListener(this);

        Button btnStats = (Button) findViewById(R.id.btnStats);
        btnStats.setOnClickListener(this);

        Button btnTestNative = (Button) findViewById(R.id.btnTestNative);
        btnTestNative.setOnClickListener(this);

        Button btnTestForeign = (Button) findViewById(R.id.btnTestForeign);
        btnTestForeign.setOnClickListener(this);

        // Para elegir juego de caracteres
        Spinner spnEncoding = (Spinner) findViewById(R.id.spnEncoding);
        ArrayList<String> encodings = new ArrayList<String>();
        encodings.add("ISO-8859-1");
        encodings.add("UTF-8");
        final ArrayAdapter<String> adapterEncodings = new ArrayAdapter<String>(this,R.layout.spinner_item,encodings);
        spnEncoding.setAdapter(adapterEncodings);
        spnEncoding.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        //************  Text changed, modifica filtro  ****************
        TextView tvFiltro = (TextView)findViewById(R.id.tvFiltro);
        tvFiltro.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Esconder teclado
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Buscar AdView como recurso y cargar una solicitud.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    @Override
    public void onStart() {
        super.onStart();
        this.onCreate(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vocabulary_visualization, menu);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, VocabularySelection.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra("vocabulary_name", this.currentVocabularyName);
        Log.d(this.getClass().getName(), "Volvemos a seleccion de vocabulario");
        startActivity(intent);
    }



    @Override
    public void onClick(View componente) {
        Intent intent;
        switch (componente.getId()) {
            // Cambiar nombre del vocabulario
            /* Esto no funciona, se gestiona con el onFocusChange
            case R.id.tvTitle:
                ImageButton ib1 = (ImageButton)findViewById(R.id.ibTitleUpdate);
                ib1.setVisibility(ImageButton.VISIBLE);
                ib1 = (ImageButton)findViewById(R.id.ibTitleCancelUpdate);
                ib1.setVisibility(ImageButton.VISIBLE);
                break;
                */

            // Cancelar cambio de nombre
            case R.id.ibTitleCancelUpdate:
                ImageButton ib2 = (ImageButton)findViewById(R.id.ibTitleUpdate);
                ib2.setVisibility(ImageButton.GONE);
                ib2 = (ImageButton)findViewById(R.id.ibTitleCancelUpdate);
                ib2.setVisibility(ImageButton.GONE);
                TextView tvTitle1 = (TextView)findViewById(R.id.tvTitle);
                tvTitle1.setText(currentVocabularyName);
                tvTitle1.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tvTitle1.getWindowToken(), 0);
                break;

            // Confirmar cambio de nombre
            case R.id.ibTitleUpdate:
                ImageButton ib3 = (ImageButton)findViewById(R.id.ibTitleUpdate);
                ib3.setVisibility(ImageButton.GONE);
                ib3 = (ImageButton)findViewById(R.id.ibTitleCancelUpdate);
                ib3.setVisibility(ImageButton.GONE);

                TextView tvTitle = (TextView)findViewById(R.id.tvTitle);
                String tmp = tvTitle.getText().toString();
                if (!tmp.equals("")) {
                    if (currentDB.renameDatabase(tmp)) {
                        currentVocabularyName = tmp;
                    }
                } else {
                    tvTitle.setText(currentVocabularyName);
                }
                tvTitle.clearFocus();
                InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm2.hideSoftInputFromWindow(tvTitle.getWindowToken(), 0);
                break;

            // Cambiar vocabulario
            case R.id.btnChangeVocabulary:
                intent = new Intent(this,VocabularySelection.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra("vocabulary_name", this.currentVocabularyName);
                startActivity(intent);
                break;

            case R.id.btnAddWord:
                intent = new Intent(this,WordEdition.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                //intent.putExtra("word_id","0");
                intent.putExtra("vocabulary_name",this.currentVocabularyName);
                startActivity(intent);
                break;
            case R.id.btnTestForeign:
                intent = new Intent(this,VocabularyTestActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra("vocabulary_name", this.currentVocabularyName);
                intent.putExtra("test_type","foreign");
                startActivity(intent);
                break;
            case R.id.btnTestNative:
                intent = new Intent(this,VocabularyTestActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra("vocabulary_name", this.currentVocabularyName);
                intent.putExtra("test_type","native");
                startActivity(intent);
                break;
            case R.id.btnStats:
                intent = new Intent(this,StatsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra("vocabulary_name", this.currentVocabularyName);
                startActivity(intent);
                break;
        }
    }


    public void removeItem(Word w)
    {
        //VocabularyDAO currentDB = new VocabularyDAO(this,currentVocabularyName);
        currentDB.deleteWord(w);
        finish();
        startActivity(getIntent());
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.str_removed_word)+": "+w.strNative, Toast.LENGTH_SHORT).show();
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