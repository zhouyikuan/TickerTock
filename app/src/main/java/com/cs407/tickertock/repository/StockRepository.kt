package com.cs407.tickertock.repository

import com.cs407.tickertock.api.AlphaVantageService
import com.cs407.tickertock.data.NewsArticle
import com.cs407.tickertock.data.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockRepository {
    private val apiService = AlphaVantageService.getInstance()

    // Cache for stock data
    private val stockDataCache = mutableMapOf<String, Stock>()
    private val newsDataCache = mutableMapOf<String, List<NewsArticle>>()

    /**
     * Fetch stock and news data when adding to watchlist
     */
    suspend fun fetchStockAndNews(symbol: String): Result<Pair<Stock, List<NewsArticle>>> {
        return withContext(Dispatchers.IO) {
            try {
                val result = apiService.fetchStockAndNews(symbol)
                if (result.isSuccess) {
                    val (stock, news) = result.getOrThrow()
                    // Cache the data
                    stockDataCache[symbol] = stock
                    newsDataCache[symbol] = news
                    Result.success(Pair(stock, news))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Refresh stock prices only (not news)
     */
    suspend fun refreshStockPrices(symbols: List<String>): Result<List<Stock>> {
        return withContext(Dispatchers.IO) {
            try {
                val stocks = mutableListOf<Stock>()
                for (symbol in symbols) {
                    val result = apiService.fetchStockData(symbol)
                    if (result.isSuccess) {
                        val stock = result.getOrThrow()
                        stockDataCache[symbol] = stock
                        stocks.add(stock)
                    } else {
                        // If one fails, return failure
                        return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Failed to refresh $symbol"))
                    }
                }
                Result.success(stocks)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get cached stock data
     */
    fun getCachedStock(symbol: String): Stock? {
        return stockDataCache[symbol]
    }

    /**
     * Get cached news data
     */
    fun getCachedNews(symbol: String): List<NewsArticle>? {
        return newsDataCache[symbol]
    }

    /**
     * Clear cache for a specific stock
     */
    fun clearCache(symbol: String) {
        stockDataCache.remove(symbol)
        newsDataCache.remove(symbol)
    }

    companion object {
        @Volatile
        private var instance: StockRepository? = null

        fun getInstance(): StockRepository {
            return instance ?: synchronized(this) {
                instance ?: StockRepository().also { instance = it }
            }
        }
    }
}
