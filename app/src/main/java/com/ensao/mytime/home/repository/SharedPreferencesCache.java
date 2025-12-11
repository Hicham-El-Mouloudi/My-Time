package com.ensao.mytime.home.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.ensao.mytime.home.model.Quote;

// Implémentation concrète de l'interface QuoteCache utilisant SharedPreferences
public class SharedPreferencesCache implements QuoteCache {

    private static final String PREF_NAME = "QuoteCachePrefs";
    private static final String KEY_QUOTE_TEXT = "quote_text";
    private static final String KEY_QUOTE_AUTHOR = "quote_author";

    private final SharedPreferences sharedPreferences;

    // Le Context est nécessaire pour accéder à SharedPreferences
    public SharedPreferencesCache(Context context) {
        // Mode PRIVATE : Seule cette application peut lire/écrire
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void saveQuote(Quote quote) {
        // Enregistre la citation dans SharedPreferences
        sharedPreferences.edit()
                .putString(KEY_QUOTE_TEXT, quote.getText())
                .putString(KEY_QUOTE_AUTHOR, quote.getAuthor())
                .apply(); // 'apply()' sauvegarde en arrière-plan
    }

    @Override
    public Quote loadQuote() {
        // Charge les données de SharedPreferences
        String text = sharedPreferences.getString(KEY_QUOTE_TEXT, null);
        String author = sharedPreferences.getString(KEY_QUOTE_AUTHOR, null);

        // Si le texte est null, cela signifie qu'aucune citation n'a été sauvegardée.
        if (text != null && author != null) {
            // Recrée l'objet Quote à partir des données locales
            return new Quote(text, author);
        }
        return null; // Retourne null si aucune donnée n'est trouvée
    }
}