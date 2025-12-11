package com.ensao.mytime.home.repository;
import com.ensao.mytime.home.model.Quote;
public interface ExternalContentInterface {

    // Méthode pour obtenir la citation de manière asynchrone
    void getDailyQuote(QuoteCallback callback);

    // Interface de rappel pour renvoyer le résultat (succès ou échec)
    interface QuoteCallback {
        void onSuccess(Quote quote);
        void onFailure(String errorMessage);
    }

}
