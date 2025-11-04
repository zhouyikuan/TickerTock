package com.cs407.tickertock.api

import com.cs407.tickertock.data.NewsArticle
import com.cs407.tickertock.data.Stock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AlphaVantageService {

    private val api: AlphaVantageApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.alphavantage.co/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AlphaVantageApi::class.java)
    }

    /**
     * Fetch stock price data for a single symbol
     */
    suspend fun fetchStockData(symbol: String, attemptCount: Int = 0): Result<Stock> {
        return try {
            // If we've tried all keys, return failure
            if (attemptCount >= ApiKeyManager.getTotalKeys()) {
                return Result.failure(Exception("All API keys have reached their rate limit"))
            }

            val apiKey = ApiKeyManager.getNextKey()
            val response = api.getGlobalQuote(symbol = symbol, apiKey = apiKey)

            if (response.isSuccessful) {
                val body = response.body()

                // Check if rate limit hit (Information or Note field present)
                if (body?.information != null || body?.note != null) {
                    // Rate limit hit, try next key
                    return fetchStockData(symbol, attemptCount + 1)
                }

                val quote = body?.globalQuote
                if (quote != null) {
                    val stock = Stock(
                        symbol = quote.symbol,
                        name = getStockName(quote.symbol),
                        currentPrice = quote.price.toDoubleOrNull() ?: 0.0,
                        priceChange = quote.change.toDoubleOrNull() ?: 0.0,
                        percentageChange = quote.changePercent.replace("%", "").toDoubleOrNull() ?: 0.0
                    )
                    Result.success(stock)
                } else {
                    Result.failure(Exception("No data returned for $symbol"))
                }
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch news articles for a symbol
     */
    suspend fun fetchNews(symbol: String, attemptCount: Int = 0): Result<List<NewsArticle>> {
        return try {
            // If we've tried all keys, return failure
            if (attemptCount >= ApiKeyManager.getTotalKeys()) {
                return Result.failure(Exception("All API keys have reached their rate limit"))
            }

            val apiKey = ApiKeyManager.getNextKey()
            val response = api.getNewsSentiment(tickers = symbol, apiKey = apiKey)

            if (response.isSuccessful) {
                val body = response.body()

                // Check if rate limit hit (Information or Note field present)
                if (body?.information != null || body?.note != null) {
                    // Rate limit hit, try next key
                    return fetchNews(symbol, attemptCount + 1)
                }

                // Take up to 20 articles from the feed (API returns up to 50, but we limit to 20 for user)
                val newsItems = body?.feed?.take(20) ?: emptyList()
                val articles = newsItems.mapIndexed { index, item ->
                    NewsArticle(
                        id = "${symbol}_${index + 1}",
                        title = item.title ?: "Article ${index + 1}",
                        summary = item.summary ?: "No summary available",
                        publishedAt = formatTimeAgo(item.timePublished),
                        publisher = item.source ?: "Unknown Publisher",
                        stockSymbol = symbol
                    )
                }
                Result.success(articles)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch both stock data and news in one call
     * Fetches news first - if no news available, returns error without fetching stock data
     */
    suspend fun fetchStockAndNews(symbol: String): Result<Pair<Stock, List<NewsArticle>>> {
        // Fetch news first
        val newsResult = fetchNews(symbol)
        if (newsResult.isFailure) {
            return Result.failure(newsResult.exceptionOrNull() ?: Exception("Failed to fetch news"))
        }

        val news = newsResult.getOrThrow()

        // Check if news is empty
        if (news.isEmpty()) {
            return Result.failure(Exception("No news available for $symbol"))
        }

        // Only fetch stock data if news is available
        val stockResult = fetchStockData(symbol)
        if (stockResult.isFailure) {
            return Result.failure(stockResult.exceptionOrNull() ?: Exception("Failed to fetch stock data"))
        }

        return Result.success(Pair(stockResult.getOrThrow(), news))
    }

    /**
     * Convert Alpha Vantage timestamp to relative time
     * Format: 20240315T133000 -> "2 hours ago"
     */
    private fun formatTimeAgo(timestamp: String?): String {
        if (timestamp == null) return "Unknown time"

        return try {
            // Parse: 20240315T133000
            val format = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date = format.parse(timestamp) ?: return timestamp

            val now = System.currentTimeMillis()
            val diff = now - date.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
                else -> "Just now"
            }
        } catch (e: Exception) {
            timestamp
        }
    }

    /**
     * Get stock name from symbol
     */
    private fun getStockName(symbol: String): String {
        return when (symbol) {
            "NVDA" -> "NVIDIA Corporation"
            "TSM" -> "Taiwan Semiconductor"
            "QQQ" -> "Invesco QQQ Trust"
            "AAPL" -> "Apple Inc."
            "MSFT" -> "Microsoft Corporation"
            "GOOGL" -> "Alphabet Inc."
            "AMZN" -> "Amazon.com Inc."
            "META" -> "Meta Platforms Inc."
            "TSLA" -> "Tesla Inc."
            "NFLX" -> "Netflix Inc."
            "CRM" -> "Salesforce Inc."
            "INTC" -> "Intel Corporation"
            "AMD" -> "Advanced Micro Devices"
            else -> symbol
        }
    }

    companion object {
        @Volatile
        private var instance: AlphaVantageService? = null

        fun getInstance(): AlphaVantageService {
            return instance ?: synchronized(this) {
                instance ?: AlphaVantageService().also { instance = it }
            }
        }
    }
}
