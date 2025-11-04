package com.cs407.tickertock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cs407.tickertock.data.Stock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    watchlistStocks: List<String>,
    onStockAdd: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val allStocks = remember {
        listOf(
            Stock("NVDA", "NVIDIA Corporation", 875.43, 25.67, 3.02),
            Stock("TSM", "Taiwan Semiconductor", 145.22, -2.45, -1.66),
            Stock("QQQ", "Invesco QQQ Trust", 425.18, -5.12, -1.19),
            Stock("AAPL", "Apple Inc.", 189.95, 3.44, 1.84),
            Stock("MSFT", "Microsoft Corporation", 415.26, 8.12, 2.00),
            Stock("GOOGL", "Alphabet Inc.", 2847.15, -15.23, -0.53),
            Stock("AMZN", "Amazon.com Inc.", 3467.42, 22.18, 0.64),
            Stock("META", "Meta Platforms Inc.", 485.12, -7.33, -1.49),
            Stock("TSLA", "Tesla Inc.", 248.87, 12.45, 5.26),
            Stock("NFLX", "Netflix Inc.", 598.34, -3.21, -0.53),
            Stock("CRM", "Salesforce Inc.", 267.89, 4.56, 1.73),
            Stock("INTC", "Intel Corporation", 42.18, -0.87, -2.02),
            Stock("AMD", "Advanced Micro Devices", 152.44, 6.23, 4.26)
        )
    }

    // Filter out stocks already in watchlist and apply search query
    val filteredStocks = remember(searchQuery, watchlistStocks) {
        val availableStocks = allStocks.filter { stock ->
            stock.symbol !in watchlistStocks
        }

        if (searchQuery.isBlank()) {
            availableStocks
        } else {
            availableStocks.filter { stock ->
                stock.symbol.contains(searchQuery, ignoreCase = true) ||
                        stock.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Add to Watchlist",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search stocks") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        if (filteredStocks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No stocks found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStocks) { stock ->
                    SearchResultCard(
                        stock = stock,
                        onAddClick = { onStockAdd(stock.symbol) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    stock: Stock,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${String.format("%.2f", stock.currentPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onAddClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to watchlist",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}