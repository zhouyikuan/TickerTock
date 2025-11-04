package com.cs407.tickertock.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVantageApi {

    @GET("query")
    suspend fun getGlobalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<GlobalQuoteResponse>

    @GET("query")
    suspend fun getNewsSentiment(
        @Query("function") function: String = "NEWS_SENTIMENT",
        @Query("tickers") tickers: String,
        @Query("apikey") apiKey: String
    ): Response<NewsSentimentResponse>
}
