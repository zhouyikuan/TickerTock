package com.cs407.tickertock.data

data class Stock(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val priceChange: Double,
    val percentageChange: Double
) {
    val isPositive: Boolean get() = priceChange >= 0
}

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val publishedAt: String,
    val publisher: String,
    val stockSymbol: String,
    val isSelected: Boolean = false
)

data class AISummary(
    val stockSymbol: String,
    val summary: String,
    val keyPoints: List<String>,
    val sentiment: String,
    val generatedAt: String
)