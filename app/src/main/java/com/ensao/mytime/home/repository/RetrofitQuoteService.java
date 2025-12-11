package com.ensao.mytime.home.repository;

import android.content.Context;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.ensao.mytime.home.network.QuoteApi;
import com.ensao.mytime.home.model.Quote;

import java.util.List;

public class RetrofitQuoteService implements ExternalContentInterface {

    private static final String BASE_URL = "https://zenquotes.io/api/";

    private final QuoteApi quoteApi;
    private final QuoteCache quoteCache; // Interface de cache

    public RetrofitQuoteService(Context context) {
        // Initialisation de Retrofit : Création du client HTTP
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) // Permet à Gson d'analyser le JSON
                .build();

        // Crée l'implémentation de l'interface QuoteApi
        quoteApi = retrofit.create(QuoteApi.class);

        // INITIALISATION DU CACHE
        this.quoteCache = new SharedPreferencesCache(context);
    }

    @Override
    public void getDailyQuote(final QuoteCallback callback) {

        // Exécute l'appel API de manière asynchrone grâce à Retrofit
        quoteApi.getDailyQuote().enqueue(new Callback<List<Quote>>() {

            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {

                    // On extrait le premier (et unique) élément du tableau
                    Quote dailyQuote = response.body().get(0);

                    // UTILISATION DU CACHE : Sauvegarde de la citation réussie
                    quoteCache.saveQuote(dailyQuote);

                    callback.onSuccess(dailyQuote);
                } else {
                    // Si la réponse est vide ou non réussie (Erreur HTTP)
                    handleFailure(callback, "Erreur HTTP ou API a renvoyé une liste vide.");
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                // Gestion de l'échec de la connexion réseau
                handleFailure(callback, "Erreur de connexion réseau: " + t.getMessage());
            }

            // Méthode d'aide pour gérer l'échec et le cache
            private void handleFailure(QuoteCallback callback, String errorMessage) {
                // Tente de charger la dernière citation sauvegardée
                Quote cachedQuote = quoteCache.loadQuote();
                if (cachedQuote != null) {
                    // Succès du fallback : Affiche la citation hors ligne
                    callback.onSuccess(cachedQuote);
                } else {
                    // Échec total : Pas de réseau ET pas de cache
                    callback.onFailure(errorMessage + " (Aucun cache disponible)");
                }
            }
        });
    }
}