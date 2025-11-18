package com.cs407.tickertock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.api.GroqService
import com.cs407.tickertock.data.AISummary
import com.cs407.tickertock.data.Stock
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISummaryScreen(
    swipedArticles: Map<String, Set<String>>,
    onStockClick: (String) -> Unit = {}
) {
    // Get stocks that have swiped articles and count them
    val stocksWithSwipedArticles = remember(swipedArticles) {
        swipedArticles.filter { it.value.isNotEmpty() }.map { (stockSymbol, articleIds) ->
            val count = articleIds.size
            stockSymbol to count
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
            val (stockSymbol, count) = stocksWithSwipedArticles[index]
            StockSummaryCard(
                stockSymbol = stockSymbol,
                articleCount = count,
                onClick = { onStockClick(stockSymbol) }
            )
        }
    }
}

@Composable
fun StockSummaryCard(
    stockSymbol: String,
    articleCount: Int,
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
                text = "You swiped right on $articleCount article${if (articleCount != 1) "s" else ""}",
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
    newsDataMap: Map<String, List<com.cs407.tickertock.data.NewsArticle>>,
    stockDataMap: Map<String, Stock>,
    aiSummaries: Map<String, String>,
    endMessageShownForStocks: Set<String>,
    onBackClick: () -> Unit = {},
    onGenerateSummary: (String, String) -> Unit = { _, _ -> },
    onViewSummary: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isGeneratingSummary by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Store the articles on this stock that were swiped on the news screen, which is passed from Single-Source-Of-Truth nav page.
    val swipedArticleIds = remember(stockSymbol, swipedArticles) {
        swipedArticles[stockSymbol] ?: emptySet()
    }

    // Get actual article details from the news data
    val articles = remember(swipedArticleIds, newsDataMap) {
        val allArticles = newsDataMap[stockSymbol] ?: emptyList()
        swipedArticleIds.mapNotNull { articleId ->
            allArticles.find { it.id == articleId }
        }
    }

    // Ensure that all articles for given stock are stored
    val allArticles = newsDataMap[stockSymbol] ?: emptyList()
    val hasViewedAllArticles = stockSymbol in endMessageShownForStocks
    val hasSummary = stockSymbol in aiSummaries

    //Get stock data
    val stock = stockDataMap[stockSymbol]

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
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
                    text = "$stockSymbol Articles",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // AI Button
            Button(
                onClick = {
                    if (hasSummary) {
                        onViewSummary(stockSymbol)
                    } else if (hasViewedAllArticles && stock != null) {
                        // Generate new summary
                        isGeneratingSummary = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = GroqService.generateStockSummary(stock, articles)
                            isGeneratingSummary = false
                            if (result.isSuccess) {
                                val summary = result.getOrNull()
                                if (summary != null) {
                                    onGenerateSummary(stockSymbol, summary)
                                }
                            } else {
                                errorMessage = "Issue generating a Summary"
                            }
                        }
                    }
                },
                enabled = hasViewedAllArticles && !isGeneratingSummary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isGeneratingSummary) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
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
                val article = articles[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${article.publishedAt} by ${article.publisher}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Error dialog
        errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Error") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISummaryDisplayScreen(
    stockSymbol: String,
    summary: String,
    onBackClick: () -> Unit = {}
) {
    // Parse the summary into sections
    val sections = remember(summary) {
        parseSummaryIntoSections(summary)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header row with back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$stockSymbol AI News",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Divider()

        // Summary content with scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Executive Summary Card
            item {
                SummarySection(
                    title = "Executive Summary",
                    content = sections.executiveSummary
                )
            }

            // Key Points Card
            item {
                SummarySection(
                    title = "Key Points",
                    content = sections.keyPoints
                )
            }

            // Sentiment Analysis Card
            item {
                SummarySection(
                    title = "Sentiment Analysis",
                    content = sections.sentimentAnalysis
                )
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SummarySection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
        }
    }
}

data class SummarySections(
    val executiveSummary: String,
    val keyPoints: String,
    val sentimentAnalysis: String
)

fun parseSummaryIntoSections(summary: String): SummarySections {
    // This regex was hard to set up, I used https://regex101.com/ for debugging, lots of edge case, workflow was basically to get a few prompts, put them into the regex website until I was able to identify all of the different parts
    val executiveSummaryMatch = "\\*\\*Executive Summary\\*\\*\\s*\\n(.+?)(?=\\n\\*\\*Key Points\\*\\*|$)".toRegex(RegexOption.DOT_MATCHES_ALL)
    val keyPointsMatch = "\\*\\*Key Points\\*\\*\\s*\\n(.+?)(?=\\n\\*\\*Sentiment Analysis\\*\\*|$)".toRegex(RegexOption.DOT_MATCHES_ALL)
    val sentimentMatch = "\\*\\*Sentiment Analysis\\*\\*\\s*\\n(.+)".toRegex(RegexOption.DOT_MATCHES_ALL)

    val executiveSummary = executiveSummaryMatch.find(summary)?.groupValues?.get(1)?.trim() ?: ""
    val keyPoints = keyPointsMatch.find(summary)?.groupValues?.get(1)?.trim() ?: ""
    val sentimentAnalysis = sentimentMatch.find(summary)?.groupValues?.get(1)?.trim() ?: ""

    return SummarySections(
        executiveSummary = executiveSummary,
        keyPoints = keyPoints,
        sentimentAnalysis = sentimentAnalysis
    )
}
