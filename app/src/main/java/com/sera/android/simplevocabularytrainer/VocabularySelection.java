package com.sera.android.simplevocabularytrainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sera.android.simplevocabularytrainer.DAO.VocabularyDAO;
import com.sera.android.simplevocabularytrainer.DAO.VocabularyDBOpenHelper;
import com.sera.android.simplevocabularytrainer.DAO.Word;
import com.sera.android.simplevocabularytrainer.DAO.WordsSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


import static android.content.Intent.ACTION_CREATE_DOCUMENT;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.widget.Toast.*;


public class VocabularySelection extends Activity
    implements View.OnClickListener
{
    public Activity currentActivity=this;
    public String currentVocabularyName;
    SortedMap<String, Integer> predefinedVocabularies;
    public RadioGroup rbListaVocabularios;
    public int currentVocabularyPos;

    private ProgressDialog pDialog;

    static final int SELECCION_FICHERO_IMPORT=0;
    static final int SELECCION_DIRECTORIO_EXPORT=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_selection);
        setTitle(R.string.app_name);

        Button btnOpenVocabulary = (Button) findViewById(R.id.btnOpenVocabulary);
        btnOpenVocabulary.setOnClickListener(this);
        Button btnDeleteVocabulary = (Button) findViewById(R.id.btnDeleteVocabulary);
        btnDeleteVocabulary.setOnClickListener(this);
        Button btnCreateVocabulary = (Button) findViewById(R.id.btnCreateVocabulary);
        btnCreateVocabulary.setOnClickListener(this);
        Button btnImportVocabulary = (Button) findViewById(R.id.btnImportVocabulary);
        btnImportVocabulary.setOnClickListener(this);
        Button btnExportVocabulary = (Button) findViewById(R.id.btnExportVocabulary);
        btnExportVocabulary.setOnClickListener(this);

        predefinedVocabularies = new TreeMap<String, Integer>();
        predefinedVocabularies.put("Español - Ruso - 1000 palabras", R.raw.espanol_ruso_1000_palabras);
        //predefinedVocabularies.put("Español - Ruso Mío",R.raw.espanol_ruso);
        predefinedVocabularies.put("Danish - English", R.raw.danish_english);
        predefinedVocabularies.put("Dutch - English", R.raw.dutch_english);
        predefinedVocabularies.put("Finnish - English", R.raw.finnish_english);
        predefinedVocabularies.put("French - English", R.raw.french_english);
        predefinedVocabularies.put("German - English", R.raw.german_english);
        predefinedVocabularies.put("Greek - English", R.raw.greek_english);
        predefinedVocabularies.put("Hungarian - English", R.raw.hungarian_english);
        predefinedVocabularies.put("Italian - English", R.raw.italian_english);
        predefinedVocabularies.put("Norwegian - English", R.raw.norwegian_english);
        predefinedVocabularies.put("Polish - English", R.raw.polish_english);
        predefinedVocabularies.put("Portuguese - English", R.raw.portuguese_english);
        predefinedVocabularies.put("Romanian - English", R.raw.romanian_english);
        predefinedVocabularies.put("Russian - English", R.raw.russian_english);
        predefinedVocabularies.put("Spanish - English", R.raw.spanish_english);
        predefinedVocabularies.put("Swedish - English", R.raw.swedish_english);
        predefinedVocabularies.put("Turkish - English", R.raw.turkish_english);

        // Rellenamos lista de vocabularios
        rbListaVocabularios = (RadioGroup) this.findViewById(R.id.rbVocabularySelection);
        fillVocabularyListRB();

        // Buscar AdView como recurso y cargar una solicitud.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("691F131B10E8003A643716BACA1D6C0B")      // Teclast X98
                .build();
        adView.loadAd(adRequest);

    }

    private void fillVocabularyListRB() {
        rbListaVocabularios.removeAllViews();
        rbListaVocabularios.clearCheck();
        String[] vocabularyList = getApplicationContext().databaseList();
        Set<String> mySet = new HashSet<String>(Arrays.asList(vocabularyList));

        currentVocabularyName = getIntent().getStringExtra("vocabulary_name");
        if (currentVocabularyName == null) {
            currentVocabularyName = "";
        }
        int i = 0;
        if (vocabularyList.length > 0) {
            for (i = 0; i < vocabularyList.length; i++) {
                final String vocabularyName = vocabularyList[i];
                if (!vocabularyName.endsWith("-journal") && !vocabularyName.endsWith(".db") && !vocabularyName.startsWith("webview")) {
                    addVocabularioToRadioGroup(rbListaVocabularios, i, vocabularyName);
                }
            }
        }
        Iterator<String> it = predefinedVocabularies.keySet().iterator();
        while (it.hasNext()) {
            String voc = it.next();
            if (!mySet.contains(voc)) {
                addVocabularioToRadioGroup(rbListaVocabularios, i++, voc + " [" + getResources().getString(R.string.str_predefined) + "]");
            }
        }
        if (rbListaVocabularios.getCheckedRadioButtonId() == -1) {
            RadioButton rb = (RadioButton) rbListaVocabularios.getChildAt(0);
            rb.setChecked(true);
            currentVocabularyName = rb.getText().toString();
            currentVocabularyPos = 0;
        }
    }

    private void addVocabularioToRadioGroup(RadioGroup rbListaVocabularios, final int pos, final String vocabularyName) {
        RadioButton rbItem = new RadioButton(this);
        rbItem.setText(vocabularyName);
        //rbItem.setTextColor(getResources().getColor(R.color.mycolors_text));
        rbItem.setTextColor(ContextCompat.getColor(this,R.color.mycolors_text));
        rbItem.setTextSize(getResources().getDimension(R.dimen.lista_diccionarios));
        rbItem.setId(pos);
        rbItem.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        currentVocabularyName = vocabularyName;
                        currentVocabularyPos = pos;
                    }
                }
        );
        if (currentVocabularyName.equals(vocabularyName)) {
            rbItem.setChecked(true);
            currentVocabularyName = vocabularyName;
            currentVocabularyPos = pos;
        }
        rbListaVocabularios.addView(rbItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vocabulary_selection, menu);
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
        Intent intent;
        switch (componente.getId()) {
            case R.id.btnOpenVocabulary:
                if (currentVocabularyName != null) {
                    currentVocabularyName = cleanDatabaseName(currentVocabularyName);
                    Log.d(this.getClass().getName(), "Abriendo DB " + currentVocabularyName);
                    // Sacamos la lista de BD reales que ya tengo
                    String[] vocabularyList = getApplicationContext().databaseList();
                    Set<String> mySet = new HashSet<String>(Arrays.asList(vocabularyList));

                    // Si el vocabulario elegido no está en la lista, es un predefinido y lo importamos
                    if (!mySet.contains(currentVocabularyName)) {
                        int idRecurso = predefinedVocabularies.get(currentVocabularyName);
                        ResourceImportAsyncTask riat = new ResourceImportAsyncTask();
                        riat.execute("" + idRecurso, currentVocabularyName);
                    } else {
                        intent = new Intent(currentActivity, VocabularyVisualization.class);
                        intent.putExtra("vocabulary_name", currentVocabularyName);
                        startActivity(intent);
                    }
                }
                break;

            case R.id.btnCreateVocabulary:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle(R.string.create_vocabulary);
                alert.setMessage(R.string.str_enter_vocabulary_name);

                final EditText input = new EditText(this);
                alert.setView(input);

                alert.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String dbname = input.getText().toString();
                        VocabularyDBOpenHelper currentDB = new VocabularyDBOpenHelper(currentActivity, dbname);
                        SQLiteDatabase wordsDB = currentDB.getWritableDatabase();
                        wordsDB.close();

                        finish();
                        startActivity(getIntent());
                    }
                });

                alert.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
                break;

            case R.id.btnImportVocabulary:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/plain");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, getResources().getString(R.string.str_select_file)),
                            SELECCION_FICHERO_IMPORT);
                } catch (android.content.ActivityNotFoundException ex) {
                    // Potentially direct the user to the Market with a Dialog
                    //makeText(this, getResources().getString(R.string.str_install_filemanager),LENGTH_SHORT).show();
                    mensaje(getResources().getString(R.string.app_name),getResources().getString(R.string.str_install_filemanager));
                }
                break;
            case R.id.btnExportVocabulary:
                // Quitamos porqueria del nombre
                currentVocabularyName = cleanDatabaseName(currentVocabularyName);


                Log.d(this.getClass().getName(), "Exportando");
                ExportAsyncTask eat = new ExportAsyncTask();
                eat.execute(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                break;

            case R.id.btnDeleteVocabulary:
                AlertDialog.Builder alertDelete = new AlertDialog.Builder(this);

                alertDelete.setTitle(R.string.str_delete_vocabulary);
                alertDelete.setMessage(getResources().getString(R.string.str_confirm_delete_vocabulary) + " " + currentVocabularyName + "?");

                alertDelete.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getApplicationContext().deleteDatabase(currentVocabularyName);
                        fillVocabularyListRB();
                    }
                });

                alertDelete.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alertDelete.show();
                break;
        }
    }


    /**
     * Función que se invoca tras volver del diálogo para elegir fichero (import) o directorio (export)
     * @param requestCode Código que se envió (indicando exportar o importar)
     * @param resultCode Código de resultado (debería ser RESULT_OK)
     * @param data Directorio/fichero elegido
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(this.getClass().getName(), "Tras seleccion de archivo");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECCION_FICHERO_IMPORT:
                    Log.d(this.getClass().getName(), "Importando "+data.getData().getPath());

                    ImportAsyncTask iat = new ImportAsyncTask();
                    Uri uri = data.getData();

                    Log.d(this.getClass().getName(), "Scheme: "+uri.getScheme());
                    Log.d(this.getClass().getName(), "Authority: "+uri.getAuthority());

                    /*String filePath ="";
                    if ("file".equalsIgnoreCase(uri.getScheme())) {
                        filePath = uri.getPath();
                    }
                    else if (DocumentsContract.isDocumentUri(this, uri)) {
                        if (isExternalStorageDocument(uri)) {
                            final String docId = DocumentsContract.getDocumentId(uri);
                            final String[] split = docId.split(":");
                            final String type = split[0];

                            Log.d(this.getClass().getName(), "ExternalStorage: "+Environment.getExternalStorageDirectory());
                            File[] dirs = this.getExternalFilesDirs(null);
                            for (File dir : dirs) {
                                Log.d(this.getClass().getName(), "ExternalFile: "+dir.getAbsolutePath());
                            }

                            //if ("primary".equalsIgnoreCase(type)) {
                                filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                            //}

                            // TODO handle non-primary volumes
                        }
                        // DownloadsProvider
                        else if (isDownloadsDocument(uri)) {
                            final String id = DocumentsContract.getDocumentId(uri);
                            final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                            filePath= getDataColumn(this, contentUri, null, null);
                        }
                    }
                    Log.d(this.getClass().getName(), "Importando2: "+filePath);
                    iat.execute(filePath);
                    */
                    try {
                        if ("file".equalsIgnoreCase(uri.getScheme())) {
                            Log.d(this.getClass().getName(), "Fichero: Abriendo "+uri.getPath());
                            iat.fileName = (new File(uri.getPath())).getName();
                            FileInputStream fis = new FileInputStream(uri.getPath());
                            iat.execute(fis);
                            //fis.close();
                        }
                        else if (DocumentsContract.isDocumentUri(this, uri)) {
                            Log.d(this.getClass().getName(), "Stream: DocumentID: "+DocumentsContract.getDocumentId(uri));

                            String docId = DocumentsContract.getDocumentId(uri);
                            String[] split = docId.split(":");
                            // Si hay barras, cogemos lo del final
                            iat.fileName = split[1];
                            String[] split2 = iat.fileName.split("/");
                            iat.fileName = split2[split2.length - 1];
                                iat.execute(getContentResolver().openInputStream(uri));
                        } else {
                            Log.d(this.getClass().getName(), "***Ni fichero ni stream***");
                        }
                    } catch (IOException fne) {
                        mensaje("KKKK", "Fichero no encontrado " + uri.getPath());
                    }

                    break;
                /* Esto no funciona. En android no se puede seleccionar una carpeta.
                case SELECCION_DIRECTORIO_EXPORT:
                    // Quitamos porqueria del nombre
                    //currentVocabularyName = cleanDatabaseName(currentVocabularyName);
                    File fout = new File(data.getData().getPath());
                    Log.d(this.getClass().getName(), "Exportando "+fout.getAbsolutePath());
                    ExportAsyncTask eat = new ExportAsyncTask();
                    eat.execute(fout);
                    break;
                    */
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /********************************** AsyncTasks **************************************/
    private class ResourceImportAsyncTask extends AsyncTask<String,String,Boolean>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(this.getClass().getName(), "***************** Antes de importar recurso ***************");
            pDialog = new ProgressDialog(currentActivity);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... parametros)
        {
            boolean ret;
            int idRecurso = Integer.parseInt(parametros[0]);
            String vocName =parametros[1];
            pDialog.setMessage(currentActivity.getResources().getString(R.string.str_expanding)+" "+vocName);
            try {
                importResourceVocabulary(idRecurso, vocName);
                ret=true;
            } catch (Exception e) {
                Log.e(this.getClass().getName(), "Error leyendo palabras", e);
                ret=false;
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            Log.d(this.getClass().getName(), "********************* Despues de importar: Resultado="+result+" ***********************");
            if (result) {
                Intent intent = new Intent(currentActivity, VocabularyVisualization.class);
                intent.putExtra("vocabulary_name", currentVocabularyName);
                startActivity(intent);
            } else {
                //makeText(currentActivity, currentActivity.getResources().getString(R.string.str_error_importing)+" "+currentVocabularyName,LENGTH_LONG).show();
                mensaje(currentActivity.getResources().getString(R.string.app_name),currentActivity.getResources().getString(R.string.str_error_importing));
            }
        }
    }



    private class ImportAsyncTask extends AsyncTask<InputStream,String,Boolean>
    {
        public String fileName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(this.getClass().getName(), "***************** Antes de importar fichero ***************");
            pDialog = new ProgressDialog(currentActivity);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(InputStream... parametros)
        {
            boolean ret;
            //File input = new File(parametros[0]);
            //fileName=input.getName();

            InputStream input=parametros[0];
            pDialog.setMessage(currentActivity.getResources().getString(R.string.str_progress_importing)+" "+fileName);
            try {
                importData(fileName,input);
                ret=true;
                input.close();
            } catch (Exception e) {
                Log.e(this.getClass().getName(), "Error leyendo palabras", e);
                ret=false;
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            Log.d(this.getClass().getName(), "********************* Despues de importar: Resultado="+result+" ***********************");
            if (result) {
                finish();
                startActivity(getIntent());
            } else {
                //makeText(currentActivity, currentActivity.getResources().getString(R.string.str_error_importing)+" "+currentVocabularyName,LENGTH_LONG).show();
                mensaje(currentActivity.getResources().getString(R.string.app_name),currentActivity.getResources().getString(R.string.str_error_importing));
            }
        }
    }



    private class ExportAsyncTask extends AsyncTask<File,String,Boolean>
    {
        private boolean reloadScreen;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(this.getClass().getName(), "***************** Antes de exportar fichero ***************");
            pDialog = new ProgressDialog(currentActivity);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(File... parametros)
        {
            boolean ret;
            File output = parametros[0];
            pDialog.setMessage(currentActivity.getResources().getString(R.string.str_exporting) + " "+output.getAbsolutePath());

            try {
                // Primero miramos si es un predefinido. En tal caso, hay que crearlo para exportarlo.
                String[] vocabularyList = getApplicationContext().databaseList();
                Set<String> mySet = new HashSet<String>(Arrays.asList(vocabularyList));
                // Si el vocabulario elegido no está en la lista, es un predefinido y lo importamos
                if (!mySet.contains(currentVocabularyName)) {
                    int idRecurso = predefinedVocabularies.get(currentVocabularyName);
                    importResourceVocabulary(idRecurso, currentVocabularyName);
                    reloadScreen=true;
                }

                exportData(output,currentVocabularyName);
                ret = true;
            } catch (Exception e) {
                Log.e(this.getClass().getName(), "Error exportando palabras", e);
                ret=false;
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            Log.d(this.getClass().getName(), "********************* Despues de exportar: Resultado=" + result + " ***********************");
            if (result) {
                //makeText(currentActivity, currentActivity.getResources().getString(R.string.str_finished_exporting)+" "+currentVocabularyName,LENGTH_LONG).show();
                mensaje(currentActivity.getResources().getString(R.string.app_name),currentActivity.getResources().getString(R.string.str_finished_exporting));
                if (reloadScreen) {
                    finish();
                    startActivity(getIntent());
                }
            } else {
                //makeText(currentActivity, currentActivity.getResources().getString(R.string.str_error_exporting)+" "+currentVocabularyName,LENGTH_LONG).show();
                mensaje(currentActivity.getResources().getString(R.string.app_name),currentActivity.getResources().getString(R.string.str_error_exporting));
            }
        }
    }



    /**************************************** Metodos de import/export *********************************/

    /*
     * Importa un fichero de los recursos
     * Ejemplo: importResourceVocabulary(R.raw.espanol_ruso_1000_palabras,"Español - Ruso - 1000 palabras")
     */
    private void importResourceVocabulary(int vocabularyId, String name)
            throws IOException
    {
        // Leemos los datos de Interlex
        InputStream databaseInputStream = getResources().openRawResource(vocabularyId);
        currentVocabularyName = name;
        VocabularyDAO currentDB = new VocabularyDAO(currentActivity, currentVocabularyName);
        String linea;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(databaseInputStream));
        while ((linea = reader.readLine()) != null) {
            String[] tokens = linea.split("\t", -1);
            String strNative = tokens[0];
            String partOfSpeech = tokens[1];
            String notes = tokens[2];
            String translation = tokens[3];

            Word w = new Word(strNative, translation, partOfSpeech, notes);
            currentDB.addWord(w);
            //publishProgress(w.strNative);
            //pDialog.setMessage(getResources().getString(R.string.str_progress_importing) + " " + w.strNative);
            pDialog.incrementProgressBy(1);
            Log.d(this.getClass().getName(), getResources().getString(R.string.str_progress_importing) + " " + w.strNative);
        }

        reader.close();
    }


    /*
     * Lee datos de un fichero separado por tabs
     * Formato: (Native\tPartOfSpeech\tNotes\tTranslation)
     */
    private void importData(File origen)
        throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(origen));

        currentVocabularyName = origen.getName();
        currentVocabularyName = cleanDatabaseName(currentVocabularyName);

        String[] vocabularyList = getApplicationContext().databaseList();
        Set<String> mySet = new HashSet<String>(Arrays.asList(vocabularyList));
        if (mySet.contains(currentVocabularyName)) {
            int i = 0;
            while (mySet.contains(currentVocabularyName + " (" + i + ")")) {
                i++;
            }
            currentVocabularyName = currentVocabularyName + " (" + i + ")";
        }

        VocabularyDAO currentDB = new VocabularyDAO(currentActivity, currentVocabularyName);
        String linea;

        while ((linea = reader.readLine()) != null) {
            String[] tokens = linea.split("\t", -1);
            String strNative = tokens[0];
            String partOfSpeech = tokens[1];
            String notes = tokens[2];
            String translation = tokens[3];

            Word w = new Word(strNative, translation, partOfSpeech, notes);
            currentDB.addWord(w);
        }
        reader.close();
    }

    private void importData(String fileName, InputStream origen)
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(origen));

        currentVocabularyName = cleanDatabaseName(fileName);

        String[] vocabularyList = getApplicationContext().databaseList();
        Set<String> mySet = new HashSet<String>(Arrays.asList(vocabularyList));
        if (mySet.contains(currentVocabularyName)) {
            int i = 0;
            while (mySet.contains(currentVocabularyName + " (" + i + ")")) {
                i++;
            }
            currentVocabularyName = currentVocabularyName + " (" + i + ")";
        }

        VocabularyDAO currentDB = new VocabularyDAO(currentActivity, currentVocabularyName);
        String linea;

        while ((linea = reader.readLine()) != null) {
            String[] tokens = linea.split("\t", -1);
            String strNative = tokens[0];
            String partOfSpeech = tokens[1];
            String notes = tokens[2];
            String translation = tokens[3];

            Word w = new Word(strNative, translation, partOfSpeech, notes);
            currentDB.addWord(w);
        }
        reader.close();
    }


    /*
     * Exporta datos a un fichero separado por tabs
     * Formato: (Native\tPartOfSpeech\tNotes\tTranslation)
     */
    private void exportData(File destino,String vocName)
        throws IOException
    {
        // Escribimos los datos en un fichero separado por tabs, con los campos: nativo - part - notas - traduccion
        File file = new File(destino, vocName + ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            VocabularyDAO currentDB = new VocabularyDAO(currentActivity, vocName);
            String linea;
            WordsSet ws = currentDB.readWords();
            Iterator<Word> it = ws.iterator();
            while (it.hasNext()) {
                Word w = it.next();
                linea = w.strNative + "\t" + w.partOfSpeech + "\t" + w.notes + "\t" + w.strForeign + "\n";
                writer.write(linea);
            }
            writer.close();
    }


    private String cleanDatabaseName(String dbname) {
        String importStr = " [" + getResources().getString(R.string.str_predefined) + "]";
        if (dbname.endsWith(importStr)) {
            dbname = dbname.substring(0, dbname.length() - importStr.length());
        }
        String txtStr = ".txt";
        if (dbname.endsWith(txtStr)) {
            dbname = dbname.substring(0, dbname.length() - txtStr.length());
        }

        return dbname;
    }

    /*==================== Notificaciones al usuario ============*/
    /**
     * Genera una notificación en la barra de tareas.
     * @param titulo Normalmente será el nombre de la app
     * @param msg El mensaje a mostrar debajo
     */
    private void mensaje(String titulo,String msg)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(currentActivity)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(titulo)
                        .setContentText(msg);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }


    /*==================== ContentProviders (para import/export) ============*/
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}