package com.cs407.tickertock.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cs407.tickertock.ui.screens.AISummaryScreen
import com.cs407.tickertock.ui.screens.DetailedAISummaryScreen
import com.cs407.tickertock.ui.screens.NewsScreen
import com.cs407.tickertock.ui.screens.SearchScreen
import com.cs407.tickertock.ui.screens.WatchlistScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Watchlist : Screen("watchlist", "W", Icons.Default.List)
    object News : Screen("news", "N", Icons.Default.Newspaper)
    object AISummary : Screen("ai_summary", "AI", Icons.Default.Analytics)
    object DetailedAISummary : Screen("detailed_ai_summary/{stockSymbol}", "AI Detail", Icons.Default.Analytics)
    object Search : Screen("search", "Search", Icons.Default.List)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TickerTockNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedStock by remember { mutableStateOf("NVDA") }
    var watchlistStocks by remember {
        mutableStateOf(listOf("NVDA", "TSM", "QQQ", "AAPL"))
    }
    // Track which articles have been swiped right for each stock
    var swipedArticles by remember {
        mutableStateOf<Map<String, Set<String>>>(emptyMap())
    }
    // Track article index per stock (persists across navigation)
    var articleIndexPerStock by remember {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }
    // Track which stocks have shown the end message
    var endMessageShownForStocks by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Watchlist.route
            ) {
                composable(Screen.Watchlist.route) {
                    WatchlistScreen(
                        watchlistStocks = watchlistStocks,
                        onStockClick = { stock ->
                            selectedStock = stock
                            navController.navigate(Screen.News.route)
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        onStockRemove = { stockSymbol ->
                            // Remove from watchlist
                            watchlistStocks = watchlistStocks.filter { it != stockSymbol }

                            // Clear all data for this stock
                            swipedArticles = swipedArticles - stockSymbol
                            articleIndexPerStock = articleIndexPerStock - stockSymbol
                            endMessageShownForStocks = endMessageShownForStocks - stockSymbol

                            // Update selected stock
                            if (selectedStock == stockSymbol) {
                                selectedStock = if (watchlistStocks.isNotEmpty()) {
                                    watchlistStocks.first()
                                } else {
                                    "" // Empty string when no stocks left
                                }
                            }
                        }
                    )
                }

                composable(Screen.News.route) {
                    NewsScreen(
                        stockSymbol = selectedStock,
                        watchlistStocks = watchlistStocks,
                        articleIndexPerStock = articleIndexPerStock,
                        endMessageShownForStocks = endMessageShownForStocks,
                        onAISummaryClick = {
                            // Navigate to detailed AI summary for the current stock
                            navController.navigate("detailed_ai_summary/$selectedStock")
                        },
                        onStockChange = { newStock ->
                            selectedStock = newStock
                        },
                        onArticleSwiped = { stockSymbol, articleId ->
                            val currentArticles = swipedArticles[stockSymbol] ?: emptySet()
                            swipedArticles = swipedArticles + (stockSymbol to (currentArticles + articleId))
                        },
                        onArticleIndexChanged = { stockSymbol, newIndex ->
                            articleIndexPerStock = articleIndexPerStock + (stockSymbol to newIndex)
                        },
                        onEndMessageShown = { stockSymbol ->
                            endMessageShownForStocks = endMessageShownForStocks + stockSymbol
                        }
                    )
                }

                composable(Screen.AISummary.route) {
                    AISummaryScreen(
                        swipedArticles = swipedArticles,
                        onStockClick = { stockSymbol ->
                            navController.navigate("detailed_ai_summary/$stockSymbol")
                        }
                    )
                }

                composable(Screen.DetailedAISummary.route) { backStackEntry ->
                    val stockSymbol = backStackEntry.arguments?.getString("stockSymbol") ?: "NVDA"
                    DetailedAISummaryScreen(
                        stockSymbol = stockSymbol,
                        swipedArticles = swipedArticles,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        watchlistStocks = watchlistStocks,
                        onStockAdd = { stockSymbol ->
                            if (stockSymbol !in watchlistStocks) {
                                watchlistStocks = watchlistStocks + stockSymbol
                            }
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Watchlist,
        Screen.News,
        Screen.AISummary
    )
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
