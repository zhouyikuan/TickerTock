package com.cs407.tickertock.api

import com.cs407.tickertock.data.NewsArticle
import com.cs407.tickertock.data.Stock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GroqService {
    private const val BASE_URL = "https://api.groq.com/openai/v1/"
    private const val API_KEY = "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" // Free grok API KEY so we don't have it in a private file, but the API is actually able to detect that we pushed to a public repo, so we need to move it into a private file
    private const val MODEL = "llama-3.1-8b-instant" // Cheapest and fastest model

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GroqApi::class.java)

    suspend fun generateStockSummary(
        stock: Stock,
        articles: List<NewsArticle>
    ): Result<String> {
        return try {
            // Build the prompt with stock data and articles
            val articlesText = articles.joinToString("\n\n") { article ->
                """
                Title: ${article.title}
                Publisher: ${article.publisher}
                Published: ${article.publishedAt}
                Summary: ${article.summary}
                """.trimIndent()
            }

            val userPrompt = """
                Stock: ${stock.symbol} (${stock.name})
                Current Price: $${stock.currentPrice}
                Price Change: ${if (stock.priceChange >= 0) "+" else ""}$${stock.priceChange} (${if (stock.percentageChange >= 0) "+" else ""}${stock.percentageChange}%)

                Here are ${articles.size} news articles about this stock:

                $articlesText

                Please provide a comprehensive analysis of these articles for ${stock.symbol}.
            """.trimIndent()

            val systemPrompt = """
                You are a financial news analyst. Analyze the provided news articles and create a summary with the following EXACT structure:

                **Executive Summary**
                [Write 2-3 paragraphs summarizing the key themes and developments from all articles]

                **Key Points**
                • [First key point]
                • [Second key point]
                • [Third key point]
                • [Fourth key point]
                • [Fifth key point]

                **Sentiment Analysis**
                Overall Market Sentiment: [Bullish/Bearish/Neutral]
                [Write 1-2 sentences explaining the reasoning for this sentiment based on the articles]

                Be concise, factual, and maintain this exact format for consistency across all summaries.
            """.trimIndent()
	    // Here is the actual entrypoint to the api 
            val request = GroqChatRequest(
                model = MODEL,
                messages = listOf(
                    GroqMessage(role = "system", content = systemPrompt),
                    GroqMessage(role = "user", content = userPrompt)
                ),
                temperature = 0.5,
                maxTokens = 2048
            )

            val response = api.createChatCompletion(
                authorization = "Bearer $API_KEY",
                request = request
            )

            val summary = response.choices.firstOrNull()?.message?.content
            if (summary != null) {
                Result.success(summary)
            } else {
                Result.failure(Exception("No response from AI"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
