package com.ensao.mytime.home.repository;

import com.ensao.mytime.home.model.Quote;

// Interface définissant les méthodes de cache requises
public interface QuoteCache {
    void saveQuote(Quote quote);
    Quote loadQuote();
}