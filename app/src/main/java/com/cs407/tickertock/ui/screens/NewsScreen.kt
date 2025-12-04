package com.cs407.tickertock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.data.NewsArticle
import com.cs407.tickertock.data.Stock
import com.cs407.tickertock.utils.TiltDetector
import kotlin.math.abs


/**
 * A composable representing the NewScreen Activity.
 *
 * This composable delivers a single news article to the user at a time.
 * The user can use swipe gestures (left and right) to discard or save the article for the AI
 * summary tool. After swiping, the composable will show the next available news article or
 * an end message if all articles have been read.
 * The user can use swipe gestures (up and down) view news articles of different stocks in the
 * watchlist.
 *
 *
 * This composable implements state hoisting up to TickerTOckNavigation as our single source of
 * truth for all params below.
 * @param stockSymbol The currently selected stock to show news articles for
 * @param watchlistStocks The list of stocks symbols in user's watchlist
 * @param newsDataMap The mapping of stock symbols to their respective news articles
 * @param articleIndexPerStock the mapping of stock symbols to the current article to show the user
 * @param endMessageShownForStocks the set of stock symbols to show the use the end message
 * @param onStockChange callback invoked when the user switches to a different stock
 *                      Expected behavior is to update stockSymbol and manage the top-level states
 * @param onArticleSwiped callback to manage the top-level states when user swipes a stock
 *                        (included or skipped)
 * @param onArticleIndexChanged callback invoked when current article index changes for a stock.
 *                              Expected behavior is to update articleIndexPerStock manage the
 *                              top-level states
 * @param onEndMessageShown callback invoked when a user reads all news articles for a given stock.
 *                          Expected behavior is to add the stock into set endMessageShownForStocks
 *                          and manage the top-level states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    stockSymbol: String,
    watchlistStocks: List<String>,
    newsDataMap: Map<String, List<NewsArticle>>,
    articleIndexPerStock: Map<String, Int>,
    endMessageShownForStocks: Set<String>,
    onStockChange: (String) -> Unit,
    onArticleSwiped: (String, String) -> Unit,
    onArticleIndexChanged: (String, Int) -> Unit,
    onEndMessageShown: (String) -> Unit
) {
    var currentStockIndex by remember(stockSymbol, watchlistStocks) {
        mutableStateOf(watchlistStocks.indexOf(stockSymbol).takeIf { it >= 0 } ?: 0)
    }

    val currentArticleIndex = articleIndexPerStock[stockSymbol] ?: 0
    val showEndMessage = stockSymbol in endMessageShownForStocks

    val articles = remember(stockSymbol, newsDataMap) {
        newsDataMap[stockSymbol] ?: emptyList()
    }

    var totalDragX by remember { mutableStateOf(0f) }
    var totalDragY by remember { mutableStateOf(0f) }

    LaunchedEffect(currentStockIndex, watchlistStocks) {
        if (currentStockIndex in watchlistStocks.indices) {
            onStockChange(watchlistStocks[currentStockIndex])
        }
    }

    // Helper function for handling swipe right (include article)
    val handleSwipeRight = {
        if (!showEndMessage && articles.isNotEmpty() && currentArticleIndex < articles.size) {
            val currentArticle = articles[currentArticleIndex]
            onArticleSwiped(stockSymbol, currentArticle.id)

            if (currentArticleIndex == articles.size - 1) {
                onEndMessageShown(stockSymbol)
            } else {
                onArticleIndexChanged(stockSymbol, currentArticleIndex + 1)
            }
        }
    }

    // Helper function for handling swipe left (skip article)
    val handleSwipeLeft = {
        if (!showEndMessage && articles.isNotEmpty()) {
            if (currentArticleIndex == articles.size - 1) {
                onEndMessageShown(stockSymbol)
            } else {
                onArticleIndexChanged(stockSymbol, currentArticleIndex + 1)
            }
        }
    }

    // Enable tilt detection only when there are articles to view
    TiltDetector(
        enabled = !showEndMessage && articles.isNotEmpty() && currentArticleIndex < articles.size,
        onTiltRight = handleSwipeRight,
        onTiltLeft = handleSwipeLeft
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(currentStockIndex, currentArticleIndex) {
                // ---
                // Implements dragging features
                // ---
                detectDragGestures(
                    onDragStart = {
                        // reset
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        if (abs(totalDragY) > abs(totalDragX) && abs(totalDragY) > 100) {
                            // ---
                            // implements vertical drag stock swap
                            // ---
                            if (totalDragY > 0) {
                                if (currentStockIndex > 0) {
                                    currentStockIndex--
                                }
                            } else {
                                if (currentStockIndex < watchlistStocks.size - 1) {
                                    currentStockIndex++
                                }
                            }
                        } else if (abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > 100) {
                            // Implements horizontal drag (news article accept or remove)
                            if (totalDragX > 0) {
                                handleSwipeRight()
                            } else {
                                handleSwipeLeft()
                            }
                        }
                        totalDragX = 0f
                        totalDragY = 0f
                    }
                ) { change, dragAmount ->
                    totalDragX += dragAmount.x
                    totalDragY += dragAmount.y
                    change.consume()
                }
            }
    ) {
        if (watchlistStocks.isEmpty() || stockSymbol.isEmpty()) {
            // ---
            // Empty Watchlist
            // ---
            EmptyWatchlistState()
        } else {
            StockHeader(stockSymbol = stockSymbol)

            Spacer(modifier = Modifier.height(16.dp))

            StockNavigationIndicator(
                currentStockIndex = currentStockIndex,
                watchlistStocks = watchlistStocks
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Article display or end message - THIS TAKES UP REMAINING SPACE
            if (showEndMessage) {
                EndMessageCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Takes remaining space after header/nav
                )
            } else if (articles.isNotEmpty() && currentArticleIndex < articles.size) {
                val currentArticle = articles[currentArticleIndex]
                ArticleDisplaySection(
                    article = currentArticle,
                    currentIndex = currentArticleIndex,
                    totalArticles = articles.size,
                    modifier = Modifier.weight(1f) // Pass weight to the composable
                )
            } else {
                // Explicit fallback
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// Keep these helper composables separate
@Composable
fun EmptyWatchlistState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Your watchlist is empty.\nAdd stocks to get started!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GestureInstructions(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SwipeLeft,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Skip",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF44336)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SwipeRight,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Include",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun StockHeader(stockSymbol: String, modifier: Modifier = Modifier) {
    Text(
        text = stockSymbol,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun StockNavigationIndicator(
    currentStockIndex: Int,
    watchlistStocks: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Previous stock",
            tint = if (currentStockIndex > 0) MaterialTheme.colorScheme.primary else Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Swipe up/down to change stocks",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Next stock",
            tint = if (currentStockIndex < watchlistStocks.size - 1) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}

@Composable
fun EndMessageCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Go to the AI summary page to see the articles you swiped on!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Composable that displays the article card, gesture instructions, and counter
 * This wraps all the elements that should appear when viewing articles
 *
 * IMPORTANT: The modifier parameter should receive .weight(1f) from the parent Column
 * This allows the entire section to take up remaining space while keeping instructions
 * and counter visible at the bottom
 */
@Composable
fun ArticleDisplaySection(
    article: NewsArticle,
    currentIndex: Int,
    totalArticles: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Article Card takes most of the space
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // This weight is within THIS Column, not the parent
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${article.publishedAt} by ${article.publisher}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gesture instructions - always visible
        GestureInstructions()

        Spacer(modifier = Modifier.height(8.dp))

        // Article counter - always visible
        Text(
            text = "${currentIndex + 1} of ${totalArticles}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}