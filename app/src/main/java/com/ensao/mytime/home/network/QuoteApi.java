package com.ensao.mytime.home.network;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;
import com.ensao.mytime.home.model.Quote;

public interface QuoteApi {
    @GET("random")
    Call<List<Quote>> getDailyQuote();

}