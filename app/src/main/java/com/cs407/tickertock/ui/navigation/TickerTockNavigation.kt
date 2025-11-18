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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cs407.tickertock.api.ApiKeyManager
import com.cs407.tickertock.data.NewsArticle
import com.cs407.tickertock.data.Stock
import com.cs407.tickertock.repository.StockRepository
import com.cs407.tickertock.ui.screens.AISummaryDisplayScreen
import com.cs407.tickertock.ui.screens.AISummaryScreen
import com.cs407.tickertock.ui.screens.DetailedAISummaryScreen
import com.cs407.tickertock.ui.screens.NewsScreen
import com.cs407.tickertock.ui.screens.SearchScreen
import com.cs407.tickertock.ui.screens.WatchlistScreen
import kotlinx.coroutines.launch


//Defines 5 navigation destinations that are in this app
sealed class Screen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Watchlist : Screen("watchlist", "W", Icons.Default.List)
    object News : Screen("news", "N", Icons.Default.Newspaper)
    object AISummary : Screen("ai_summary", "AI", Icons.Default.Analytics)
    object DetailedAISummary :
        Screen("detailed_ai_summary/{stockSymbol}", "AI Detail", Icons.Default.Analytics)

    object AISummaryDisplay :
        Screen("ai_summary_display/{stockSymbol}", "AI Summary", Icons.Default.Analytics)

    object Search : Screen("search", "Search", Icons.Default.List)
}

//Shows the 3 main tabs (Watchlist, News, AI) with their icons. Clicking their tab navigates to that screen.
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


//composable that holds all of the App state
@OptIn(ExperimentalMaterial3Api::class)
//Mat 3 gives us Bottom navigation bar, padding/spacing utility
@Composable
fun TickerTockNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    //State Variables
    // instance that handles all API call
    // things like repository.fetchStockAndNews(), repository.refreshStockPrices(), repository.clearCache()
    val repository = remember { StockRepository.getInstance() }

    //Routine for api calls
    val coroutineScope = rememberCoroutineScope()

    //Stock symbol currently being viewed in News scree
    var selectedStock by remember { mutableStateOf("") }

    //List of stock symbols in user's watchlist
    var watchlistStocks by remember {
        mutableStateOf(emptyList<String>())
    }

    //Limit watchlist size bc api calls are expensive
    val maxWatchlistSize = 3

    // Maps stock symbol to their price data
    var stockDataMap by remember {
        mutableStateOf<Map<String, Stock>>(emptyMap())
    }

    // Maps stock symbol to their news data
    var newsDataMap by remember {
        mutableStateOf<Map<String, List<NewsArticle>>>(emptyMap())
    }

    // Loading states to show loading indicator CircularProgressIndicator
    var isLoadingStock by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }


    //Any api issue pops up using this data
    var errorMessage by remember { mutableStateOf<String?>(null) }

    //  Maps stock symbol to article IDs that user swiped right on
    var swipedArticles by remember {
        mutableStateOf<Map<String, Set<String>>>(emptyMap())
    }

    //  Maps stock symbol to current article index (Swiped 4 out of 20)
    var articleIndexPerStock by remember {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }

    // Set of stocks users has swiped completly for
    var endMessageShownForStocks by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    // Maps stock symbol to generated AI summary text
    var aiSummaries by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    //We are using remember to persist across UI rebuilds and
    // mutableStateOf to keep values updated as they change

    //Nested onto padding for looks
    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                //Main part, defining nav host for app navigation
                NavHost(
                    navController = navController,
                    startDestination = Screen.Watchlist.route
                ) {
                    composable(Screen.Watchlist.route) {
                        WatchlistScreen(
                            //What it needs

                            //Stocks to show
                            watchlistStocks = watchlistStocks,
                            //Map to show price
                            stockDataMap = stockDataMap,
                            //For loading indicator
                            isRefreshing = isRefreshing,
                            //List of stocks are clickable
                            onStockClick = { stock ->
                                selectedStock = stock
                                navController.navigate(Screen.News.route)
                            },
                            //Go to serach screen
                            onSearchClick = {
                                if (watchlistStocks.size >= maxWatchlistSize) {
                                    errorMessage =
                                        "Maximum $maxWatchlistSize stocks allowed in watchlist"
                                } else {
                                    navController.navigate(Screen.Search.route)
                                }
                            },
                            //Call repository (Calls api) to get new prices
                            onRefresh = {
                                if (watchlistStocks.isNotEmpty()) {
                                    isRefreshing = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val result = repository.refreshStockPrices(watchlistStocks)
                                        isRefreshing = false
                                        if (result.isSuccess) {
                                            val stocks = result.getOrThrow()
                                            val newStockDataMap = stockDataMap.toMutableMap()
                                            stocks.forEach { stock ->
                                                newStockDataMap[stock.symbol] = stock
                                            }
                                            stockDataMap = newStockDataMap
                                        } else {
                                            val errorMsg =
                                                result.exceptionOrNull()?.message ?: "Unknown error"
                                            if (errorMsg.contains("rate limit")) {
                                                errorMessage =
                                                    "API key limit reached. Please try again later."
                                            } else {
                                                errorMessage = "Failed to refresh: $errorMsg"
                                            }
                                        }
                                    }
                                }
                            },
                            //Garbage button
                            onStockRemove = { stockSymbol ->
                                // Remove from watchlist
                                watchlistStocks = watchlistStocks.filter { it != stockSymbol }

                                // Clear all data for this stock
                                swipedArticles = swipedArticles - stockSymbol
                                articleIndexPerStock = articleIndexPerStock - stockSymbol
                                endMessageShownForStocks = endMessageShownForStocks - stockSymbol
                                aiSummaries = aiSummaries - stockSymbol
                                stockDataMap = stockDataMap - stockSymbol
                                newsDataMap = newsDataMap - stockSymbol
                                repository.clearCache(stockSymbol)

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
                            //Stock currently looking at
                            stockSymbol = selectedStock,
                            //Stocks to show
                            watchlistStocks = watchlistStocks,
                            //Map stocks to news
                            newsDataMap = newsDataMap,
                            //Map Stock to which articles its on
                            articleIndexPerStock = articleIndexPerStock,
                            //Map of stocks that are fully swiped
                            endMessageShownForStocks = endMessageShownForStocks,
                            //Swiped Up/Down
                            onStockChange = { newStock ->
                                selectedStock = newStock
                            },
                            //Swiped left/right
                            onArticleSwiped = { stockSymbol, articleId ->
                                val currentArticles = swipedArticles[stockSymbol] ?: emptySet()
                                swipedArticles =
                                    swipedArticles + (stockSymbol to (currentArticles + articleId))
                            },
                            //When onArticleSwiped update articleIndexPerStock
                            onArticleIndexChanged = { stockSymbol, newIndex ->
                                articleIndexPerStock =
                                    articleIndexPerStock + (stockSymbol to newIndex)
                            },
                            //If swiped through all articles update endMessageShownForStocks
                            onEndMessageShown = { stockSymbol ->
                                endMessageShownForStocks = endMessageShownForStocks + stockSymbol
                            }
                        )
                    }

                    composable(Screen.AISummary.route) {
                        AISummaryScreen(
                            //Show relevant articles
                            swipedArticles = swipedArticles,
                            //if you click on the stock navigate to its article page
                            onStockClick = { stockSymbol ->
                                navController.navigate("detailed_ai_summary/$stockSymbol")
                            }
                        )
                    }

                    composable(Screen.DetailedAISummary.route) { backStackEntry ->
                        //link to relevant stock
                        val stockSymbol =
                            backStackEntry.arguments?.getString("stockSymbol") ?: ""
                        DetailedAISummaryScreen(
                            //Stock you clicked
                            stockSymbol = stockSymbol,
                            //Articles user cares about
                            swipedArticles = swipedArticles,
                            //Map the relevant stock to articles
                            newsDataMap = newsDataMap,
                            //Map the relevant stock to price (for AI)
                            stockDataMap = stockDataMap,
                            //Maps stock symbol to generated AI summary text
                            aiSummaries = aiSummaries,
                            //Needs to know if user swiped on all news for this stock
                            endMessageShownForStocks = endMessageShownForStocks,
                            //Bac Button
                            onBackClick = {
                                navController.popBackStack()
                            },
                            //Generate new summary and navigate to it, add to aiSummaries for that stock
                            onGenerateSummary = { stockSymbol, summary ->
                                aiSummaries = aiSummaries + (stockSymbol to summary)
                                navController.navigate("ai_summary_display/$stockSymbol")
                            },
                            //Navigate to ai summary if it already exists
                            onViewSummary = { stockSymbol ->
                                navController.navigate("ai_summary_display/$stockSymbol")
                            }
                        )
                    }

                    composable(Screen.AISummaryDisplay.route) { backStackEntry ->
                        //Stock to show for AI summary
                        val stockSymbol =
                            backStackEntry.arguments?.getString("stockSymbol") ?: ""
                        //Ai summary to show given stock
                        val summary = aiSummaries[stockSymbol] ?: ""
                        AISummaryDisplayScreen(
                            stockSymbol = stockSymbol,
                            summary = summary,
                            //Back button
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.Search.route) {
                        SearchScreen(
                            //Needs stocks already in watchlist to know what not to show
                            watchlistStocks = watchlistStocks,
                            //Make repository call to get price and news
                            onStockAdd = { stockSymbol ->
                                if (stockSymbol !in watchlistStocks && watchlistStocks.size < maxWatchlistSize) {
                                    isLoadingStock = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val result = repository.fetchStockAndNews(stockSymbol)
                                        isLoadingStock = false
                                        if (result.isSuccess) {
                                            val (stock, news) = result.getOrThrow()
                                            // Add to watchlist
                                            watchlistStocks = watchlistStocks + stockSymbol
                                            // Store data
                                            stockDataMap = stockDataMap + (stockSymbol to stock)
                                            newsDataMap = newsDataMap + (stockSymbol to news)
                                            // Set as selected stock if it's the first one
                                            if (selectedStock.isEmpty()) {
                                                selectedStock = stockSymbol
                                            }
                                            navController.popBackStack()
                                        } else {
                                            val errorMsg =
                                                result.exceptionOrNull()?.message ?: "Unknown error"
                                            if (errorMsg.contains("rate limit")) {
                                                errorMessage =
                                                    "API key limit reached. Please try again later."
                                            } else if (errorMsg.contains("No news available")) {
                                                errorMessage = "No news available for $stockSymbol"
                                            } else {
                                                errorMessage = "Failed to add stock: $errorMsg"
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }


            // Loading indicator
            if (isLoadingStock || isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error dialog if it exists
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

}
