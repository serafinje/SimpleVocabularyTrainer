package com.sera.android.simplevocabularytrainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sera.android.simplevocabularytrainer.DAO.VocabularyDAO;


public class StatsActivity extends Activity
    implements View.OnClickListener
{
    TextView tvWordsLearnt,tvTotalWords,tvQuestionsAnswered,tvQuestionsAttempted;
    ProgressBar pbProgress, pbSuccessRate;
    Button btnReset,btnBack;
    String currentVocabularyName;
    VocabularyDAO currentDB;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Intent intent = getIntent();
        currentVocabularyName = intent.getStringExtra("vocabulary_name");
        currentDB = new VocabularyDAO(this,currentVocabularyName);

        tvWordsLearnt = (TextView) findViewById(R.id.tvWordsLearnt);
        tvTotalWords = (TextView) findViewById(R.id.tvTotalWords);
        tvQuestionsAnswered = (TextView) findViewById(R.id.tvQuestionsAnswered);
        tvQuestionsAttempted = (TextView) findViewById(R.id.tvQuestionsAttempted);

        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        pbSuccessRate = (ProgressBar) findViewById(R.id.pbSuccessRate);

        btnReset = (Button)findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        tvWordsLearnt.setText(""+currentDB.countLearnedWords());
        tvTotalWords.setText(""+currentDB.countTotalWords());
        tvQuestionsAnswered.setText(""+currentDB.countQuestionsSuccess());
        tvQuestionsAttempted.setText(""+currentDB.countQuestionsAttempted());

        pbProgress.setMax((int)currentDB.countTotalWords());
        pbProgress.setProgress((int)currentDB.countLearnedWords());

        pbSuccessRate.setMax((int) currentDB.countQuestionsAttempted());
        pbSuccessRate.setProgress((int)currentDB.countQuestionsSuccess());

        // Buscar AdView como recurso y cargar una solicitud.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
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
        switch (componente.getId()) {
            case R.id.btnReset:
                currentDB = new VocabularyDAO(this,currentVocabularyName);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(this.getResources().getText(R.string.str_confirm_reset));
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        currentDB.resetStatistics();
                        finish();
                        startActivity(getIntent());
                    }
                });
                builder.setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.btnBack:
                finish();
                break;
        }
    }

}
