package com.sera.android.simplevocabularytrainer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;

import com.sera.android.simplevocabularytrainer.DAO.Word;
import com.sera.android.simplevocabularytrainer.DAO.WordsSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sera on 24/04/2015.
 */
public class WordListAdapter extends BaseAdapter implements Filterable
{
    // Activity en la que está corriendo la aplicación. La necesitamos para sacar información del contexto y demás.
    private VocabularyVisualization vocabularyVisualizationActivity;
    private WordsSet originalData = null;
    private WordsSet filteredData = null;
    private ItemFilter mFilter = new ItemFilter();

    // Variable interna, para manejar la ListView
    private LayoutInflater inflater;


    public WordListAdapter(VocabularyVisualization slActivity) {
        this.vocabularyVisualizationActivity=slActivity;
        this.originalData = vocabularyVisualizationActivity.currentVocabulary;
        this.filteredData = vocabularyVisualizationActivity.currentVocabulary;
        this.inflater = (LayoutInflater)slActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return filteredData.size();
    }


    public long getItemId(int pos) {
        return pos;
    }

    public int getItemPos(long id) {
        Log.d(this.getClass().getName(), "Buscando palabra #" + id);

        // Buscamos la palabra entre las nativas y traducciones
        Iterator<Word> it = originalData.iterator();
        int i = 0;
        while (it.hasNext()) {
            Word w = it.next();
            if (w.id == id) {
                Log.d(this.getClass().getName(), "Palabra #" + id+" encontrada: "+w.strNative+" posicion "+i);
                return i;
            } else {
                i++;
            }
        }
        return 0;
    }

    /**
     * Devuelve la vista de linea del WordList
     */
    public View getView(final int pos, View wordView, ViewGroup parent)
    {
        wordView = inflater.inflate(R.layout.vocabulary_word, null);
        if (pos%2==0) {
            wordView.setBackgroundColor(wordView.getResources().getColor(R.color.mycolors_word_line_background));
        }

        final ViewHolder holder = new ViewHolder();
        holder.imgbtnDelete = (ImageButton)wordView.findViewById(R.id.btnWordDelete);
        holder.tvNative= (TextView)wordView.findViewById(R.id.tvNative);
        holder.tvForeign= (TextView)wordView.findViewById(R.id.tvForeign);
        holder.icoLearned = (ImageView)wordView.findViewById(R.id.icoWordLearned);

        final Word currentWord = getItem(pos);

        // Columna 1: Botón de borrado
        //		Sacamos un cuadro de diálogo para confirmar el borrado
        holder.imgbtnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(vocabularyVisualizationActivity);
                builder.setMessage(vocabularyVisualizationActivity.getResources().getText(R.string.str_confirm_delete)+" \""+currentWord.strNative+"\"?");
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        vocabularyVisualizationActivity.removeItem(currentWord);
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        // Columna 2: Palabra en nativo
        holder.tvNative.setText(currentWord.strNative);
        holder.tvNative.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vocabularyVisualizationActivity.editItem(currentWord);
            }
        });
        // Columna 3: Palabra en extranjero
        holder.tvForeign.setText(currentWord.strForeign);
        holder.tvForeign.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vocabularyVisualizationActivity.editItem(currentWord);
            }
        });
        // Columna 4: Check si está aprendida
        if (currentWord.learned()) {
            holder.icoLearned.setVisibility(View.VISIBLE);
            holder.tvNative.setTextColor(wordView.getResources().getColor(R.color.mycolors_learned_word));
            holder.tvForeign.setTextColor(wordView.getResources().getColor(R.color.mycolors_learned_word));
        } else {
            holder.icoLearned.setVisibility(View.INVISIBLE);
        }

        // Done!
        wordView.setTag(holder);
        return wordView;
    }


    // 3) Leer un item
    public Word getItem(int pos) {
        Word ret =(Word)(filteredData.toArray())[pos];
        return ret;
    }


    // 4) Leer lista de items
    public WordsSet getItems() {
        return filteredData;
    }


    public static class ViewHolder {
        public ImageButton imgbtnDelete;
        public TextView tvNative;
        public TextView tvForeign;
        public ImageView icoLearned;
    }


    /***** Filterable *****/
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            final WordsSet out = new WordsSet();

            // Buscamos la palabra entre las nativas y traducciones
            Iterator<Word> it = originalData.iterator();
            while (it.hasNext()) {
                Word w = it.next();
                if (w.strNative.toLowerCase().contains(filterString) || w.strForeign.toLowerCase().contains(filterString)) {
                    out.add(w);
                }

            }

            results.values = out;
            results.count = out.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (WordsSet) results.values;
            notifyDataSetChanged();
        }
    }
}
