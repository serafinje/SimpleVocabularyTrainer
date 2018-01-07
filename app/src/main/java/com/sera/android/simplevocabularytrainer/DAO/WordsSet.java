package com.sera.android.simplevocabularytrainer.DAO;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.ArrayList;

/**
 * Vamos a hacer una extensión de TreeSet que nos permita manejar elementos comparándolos solamente por el nombre, en lugar de los criterios del Comparable
 * Created by Sera on 24/04/2015.
 */
public class WordsSet extends TreeSet<Word> {
    // Para que sea serializable
    private static final long serialVersionUID = 1L;

    /**
     * 0) Indica si ya existe la palabra.
     */
    public boolean containsItem(Word item)
    {
        // Buscamos el item por el nombre
        Iterator<Word> it = this.iterator();
        Word p = null;
        boolean encontrado=false;
        while (it.hasNext() && !encontrado) {
            p = it.next();
            encontrado = p.strNative.equalsIgnoreCase(item.strNative);
            encontrado = encontrado || p.strForeign.equalsIgnoreCase(item.strForeign);
            encontrado = encontrado && p.partOfSpeech.equalsIgnoreCase(item.partOfSpeech);
        }

        return encontrado;
    }

    /**
     * 1) Añade un Producto a un TreeSet de Productos
     * Primero mira si ya existe (por el nombre) y lo borra antes de añadirlo.
     * Es necesario borrar e insertar porque de lo contrario el TreeSet no reordena.
     * @return
     */
    public void addItem(Word item)
    {
        //Log.d(this.getClass().getName(), "AddItem ["+this.toString()+"] + "+item.toString());
        // Primero buscamos el item por el nombre, para borrarlo
        Iterator<Word> it = this.iterator();
        Word p = null;
        boolean encontrado=false;
        while (it.hasNext() && !encontrado) {
            p = it.next();
            encontrado = p.strNative.equalsIgnoreCase(item.strNative);
            encontrado = encontrado || p.strForeign.equalsIgnoreCase(item.strForeign);
            encontrado = encontrado && p.partOfSpeech.equalsIgnoreCase(item.partOfSpeech);
        }

        if (encontrado) {
            this.remove(p);
        }

        // Despues lo añadimos
        this.add(item);
    }


    /**
     * 2) Borra un Producto de un TreeSet de Productos
     */
    public void removeItem(Word item)
    {
        //Log.d(this.getClass().getName(), "RemoveItem ["+this.toString()+"] - "+item.toString());
        // Primero buscamos el item por el nombre, para borrarlo
        Iterator<Word> it = this.iterator();
        Word p = null;
        boolean encontrado=false;
        while (it.hasNext() && !encontrado) {
            p = it.next();
            encontrado = p.strNative.equalsIgnoreCase(item.strNative);
            encontrado = encontrado || p.strForeign.equalsIgnoreCase(item.strForeign);
            encontrado = encontrado && p.partOfSpeech.equalsIgnoreCase(item.partOfSpeech);
        }

        if (encontrado) {
            //Log.d(this.getClass().getName(),"Borrado="+this.remove(p));
        }
    }


    /**
     * 3) Devuelve un producto buscado por el nombre. Si no lo encuentra, devuelve null.
     * @param name
     * @return
     */
    public Word getItem(String name) {
        Iterator<Word> it = this.iterator();
        while (it.hasNext()) {
            Word p = it.next();
            if (p.strNative.equalsIgnoreCase(name)) {
                //Log.d(this.getClass().getName(),"GetItem["+name+"] -> "+p.toString());
                return p;
            }
        }

        return null;
    }

    public Word getItem(long id) {
        Iterator<Word> it = this.iterator();
        while (it.hasNext()) {
            Word p = it.next();
            if (p.id==id) {
                return p;
            }
        }

        return null;
    }

    /**
     * 4) Devuelve un Array ordenado de los nombres de productos.
     * @return
     */
    public ArrayList<String> getItemNames()
    {
        ArrayList<String> ret = new ArrayList<String>();
        TreeSet<String> t = new TreeSet<String>();
        Iterator<Word> it = this.iterator();
        while (it.hasNext()) {
            Word p = it.next();
            t.add(p.strNative);
        }
        Iterator<String> it2 = t.iterator();
        while (it2.hasNext()) ret.add(it2.next());
        return ret;
    }


    public ArrayList<String> getItemTranslations()
    {
        ArrayList<String> ret = new ArrayList<String>();
        TreeSet<String> t = new TreeSet<String>();
        Iterator<Word> it = this.iterator();
        while (it.hasNext()) {
            Word p = it.next();
            t.add(p.strForeign);
        }
        Iterator<String> it2 = t.iterator();
        while (it2.hasNext()) ret.add(it2.next());
        return ret;
    }
}
