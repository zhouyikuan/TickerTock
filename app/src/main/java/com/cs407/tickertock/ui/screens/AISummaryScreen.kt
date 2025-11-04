package com.cs407.tickertock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.data.AISummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISummaryScreen(
    swipedArticles: Map<String, Set<String>>,
    onStockClick: (String) -> Unit = {}
) {
    // Get stocks that have swiped articles and extract article titles
    val stocksWithSwipedArticles = remember(swipedArticles) {
        swipedArticles.filter { it.value.isNotEmpty() }.map { (stockSymbol, articleIds) ->
            val titles = articleIds.map { articleId ->
                // Extract article number from ID like "NVDA_1" -> "NVDA Title 1"
                val articleNum = articleId.substringAfterLast("_")
                "$stockSymbol Title $articleNum"
            }.joinToString(", ")

            stockSymbol to titles
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Header
            Text(
                text = "AI Summaries",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "Stocks you've swiped right on",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (stocksWithSwipedArticles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles swiped yet.\nSwipe right on articles in the News screen!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        items(stocksWithSwipedArticles.size) { index ->
            val (stockSymbol, titles) = stocksWithSwipedArticles[index]
            StockSummaryCard(
                stockSymbol = stockSymbol,
                articleTitles = titles,
                onClick = { onStockClick(stockSymbol) }
            )
        }
    }
}

@Composable
fun StockSummaryCard(
    stockSymbol: String,
    articleTitles: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stockSymbol,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = articleTitles,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedAISummaryScreen(
    stockSymbol: String,
    swipedArticles: Map<String, Set<String>>,
    onBackClick: () -> Unit = {}
) {
    // Get the articles that were swiped for this stock
    val swipedArticleIds = remember(stockSymbol, swipedArticles) {
        swipedArticles[stockSymbol] ?: emptySet()
    }

    // Generate article details from the IDs
    val articles = remember(swipedArticleIds) {
        swipedArticleIds.map { articleId ->
            val articleNum = articleId.substringAfterLast("_")
            mapOf(
                "title" to "$stockSymbol Title $articleNum",
                "publisher" to "$stockSymbol Publisher $articleNum",
                "time" to "$stockSymbol Time $articleNum",
                "body" to "$stockSymbol Body $articleNum"
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Back button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to AI Summaries",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Swiped Articles for $stockSymbol",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (articles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No articles swiped for this stock",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(articles.size) { index ->
                val article = articles.elementAt(index)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = article["title"] ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${article["time"]} by ${article["publisher"]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = article["body"] ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}