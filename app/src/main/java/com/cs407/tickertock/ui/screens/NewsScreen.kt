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
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    stockSymbol: String,
    onAISummaryClick: () -> Unit,
    onStockChange: (String) -> Unit
) {
    val stocks = remember {
        listOf("NVDA", "TSM", "QQQ", "APPL")
    }
    
    var currentStockIndex by remember(stockSymbol) {
        mutableStateOf(stocks.indexOf(stockSymbol).takeIf { it >= 0 } ?: 0)
    }
    
    var currentArticleIndex by remember(stockSymbol) {
        mutableStateOf(0)
    }
    
    val sampleArticles = remember(stockSymbol) {
        when (stockSymbol) {
            "NVDA" -> listOf(
                NewsArticle(
                    id = "1",
                    title = "NVIDIA Announces Revolutionary AI Chip Architecture",
                    summary = "NVIDIA unveiled its next-generation GPU architecture designed specifically for AI workloads, promising 40% better performance per watt compared to previous generation. The new chips are expected to drive significant revenue growth in data center segment.",
                    publishedAt = "2 hours ago",
                    stockSymbol = "NVDA"
                ),
                NewsArticle(
                    id = "2",
                    title = "Partnership with Major Cloud Providers Expands",
                    summary = "NVIDIA announced strategic partnerships with leading cloud service providers to integrate their AI accelerators into cloud infrastructure. This move is expected to increase market penetration and recurring revenue streams.",
                    publishedAt = "4 hours ago",
                    stockSymbol = "NVDA"
                )
            )
            "TSM" -> listOf(
                NewsArticle(
                    id = "3",
                    title = "Taiwan Semiconductor Reports Strong Q4 Earnings",
                    summary = "TSM exceeded quarterly expectations with robust demand for advanced node semiconductors. Revenue grew 15% YoY driven by AI and automotive chip segments.",
                    publishedAt = "1 hour ago",
                    stockSymbol = "TSM"
                )
            )
            else -> listOf(
                NewsArticle(
                    id = "4",
                    title = "Market Analysis for ${stockSymbol}",
                    summary = "Latest market trends and analysis for ${stockSymbol} showing mixed signals from technical indicators.",
                    publishedAt = "3 hours ago",
                    stockSymbol = stockSymbol
                )
            )
        }
    }
    
    var selectedArticles by remember { mutableStateOf(setOf<String>()) }
    
    LaunchedEffect(currentStockIndex) {
        if (currentStockIndex in stocks.indices) {
            onStockChange(stocks[currentStockIndex])
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { 
                        // Reset article index when changing stocks
                        currentArticleIndex = 0
                    }
                ) { change, dragAmount ->
                    // Vertical swipe to change stocks
                    if (abs(dragAmount.y) > abs(dragAmount.x)) {
                        if (dragAmount.y > 50) {
                            // Swipe down - previous stock
                            if (currentStockIndex > 0) {
                                currentStockIndex--
                            }
                        } else if (dragAmount.y < -50) {
                            // Swipe up - next stock
                            if (currentStockIndex < stocks.size - 1) {
                                currentStockIndex++
                            }
                        }
                    }
                    // Horizontal swipe to navigate articles
                    else if (abs(dragAmount.x) > abs(dragAmount.y)) {
                        if (dragAmount.x > 50) {
                            // Swipe right - include article
                            if (sampleArticles.isNotEmpty()) {
                                selectedArticles = selectedArticles + sampleArticles[currentArticleIndex].id
                                if (currentArticleIndex < sampleArticles.size - 1) {
                                    currentArticleIndex++
                                }
                            }
                        } else if (dragAmount.x < -50) {
                            // Swipe left - skip article
                            if (currentArticleIndex < sampleArticles.size - 1) {
                                currentArticleIndex++
                            }
                        }
                    }
                }
            }
    ) {
        // Header with AI Summary button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAISummaryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Summary")
            }
            
            Text(
                text = stockSymbol,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stock navigation indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                tint = if (currentStockIndex < stocks.size - 1) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Article display
        if (sampleArticles.isNotEmpty() && currentArticleIndex < sampleArticles.size) {
            val currentArticle = sampleArticles[currentArticleIndex]
            val isSelected = selectedArticles.contains(currentArticle.id)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = currentArticle.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = currentArticle.publishedAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentArticle.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (isSelected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "✓ Selected for AI Summary",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // No articles available
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles available for $stockSymbol",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gesture instructions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
        
        // Article counter
        if (sampleArticles.isNotEmpty()) {
            Text(
                text = "${currentArticleIndex + 1} of ${sampleArticles.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}