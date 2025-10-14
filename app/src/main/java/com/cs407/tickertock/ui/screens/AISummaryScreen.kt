package com.cs407.tickertock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.Auto
//import androidx.compose.material.icons.filled.TrendingDown
//import androidx.compose.material.icons.filled.TrendingFlat
//import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.data.AISummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISummaryScreen(
    onStockClick: (String) -> Unit = {}
) {
    // Sample data representing stocks the user has "swiped right" on
    val swipedRightStocks = remember {
        listOf(
            AISummary(
                stockSymbol = "NVDA",
                summary = "NVIDIA's revolutionary AI chip architecture and cloud partnerships position it strongly in the growing AI infrastructure market.",
                keyPoints = listOf(),
                sentiment = "Very Positive",
                generatedAt = "Just now"
            ),
            AISummary(
                stockSymbol = "TSM",
                summary = "Taiwan Semiconductor's strong Q4 earnings exceeded expectations with 15% YoY growth driven by AI and automotive demand.",
                keyPoints = listOf(),
                sentiment = "Positive", 
                generatedAt = "2 minutes ago"
            ),
            AISummary(
                stockSymbol = "APPL",
                summary = "Apple's latest product announcements and strong services revenue growth continue to drive investor confidence.",
                keyPoints = listOf(),
                sentiment = "Positive",
                generatedAt = "5 minutes ago"
            ),
            AISummary(
                stockSymbol = "QQQ",
                summary = "The Invesco QQQ Trust reflects mixed market sentiment with tech sector rotation affecting overall performance.",
                keyPoints = listOf(),
                sentiment = "Neutral",
                generatedAt = "10 minutes ago"
            )
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete, //!!!!!!!!!!!!!!!!TODO
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Summaries",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
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
        
        items(swipedRightStocks) { stockSummary ->
            StockSummaryCard(
                stockSummary = stockSummary,
                onClick = { onStockClick(stockSummary.stockSymbol) }
            )
        }
    }
}

@Composable
fun StockSummaryCard(
    stockSummary: AISummary,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = stockSummary.stockSymbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                SentimentChip(sentiment = stockSummary.sentiment)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stockSummary.summary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Generated ${stockSummary.generatedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SentimentChip(sentiment: String) {
    val (backgroundColor, textColor, icon) = when (sentiment.lowercase()) {
        "very positive", "positive" -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.1f),
            Color(0xFF4CAF50),
            Icons.Default.PlayArrow //TODO
        )
        "negative", "very negative" -> Triple(
            Color(0xFFF44336).copy(alpha = 0.1f),
            Color(0xFFF44336),
            Icons.Default.FavoriteBorder //TODO
        )
        else -> Triple(
            Color(0xFF9E9E9E).copy(alpha = 0.1f),
            Color(0xFF9E9E9E),
            Icons.Default.Lock //TODO
        )
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = sentiment,
                color = textColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedAISummaryScreen(
    stockSymbol: String
) {
    val detailedSummary = remember(stockSymbol) {
        when (stockSymbol) {
            "NVDA" -> AISummary(
                stockSymbol = "NVDA",
                summary = "NVIDIA's stock moved significantly today due to the announcement of their revolutionary AI chip architecture and expanded cloud partnerships. The new GPU architecture promises 40% better performance per watt, positioning NVIDIA to capture more market share in the rapidly growing AI infrastructure space. Strategic partnerships with major cloud providers will create recurring revenue streams and increase market penetration.",
                keyPoints = listOf(
                    "New AI chip architecture offers 40% better performance per watt",
                    "Strategic partnerships with cloud providers expanding market reach",
                    "Strong positioning in AI infrastructure market",
                    "Expected revenue growth in data center segment",
                    "Recurring revenue potential from cloud integrations"
                ),
                sentiment = "Very Positive",
                generatedAt = "Just now"
            )
            "TSM" -> AISummary(
                stockSymbol = "TSM",
                summary = "Taiwan Semiconductor's price movement today was driven by strong Q4 earnings that exceeded expectations. The company reported 15% year-over-year revenue growth, primarily fueled by robust demand for advanced node semiconductors in AI and automotive applications. This positions TSM well for continued growth in high-margin segments.",
                keyPoints = listOf(
                    "Q4 earnings exceeded expectations with 15% YoY revenue growth",
                    "Strong demand for advanced node semiconductors",
                    "AI and automotive segments driving growth",
                    "High-margin business segments performing well"
                ),
                sentiment = "Positive",
                generatedAt = "2 minutes ago"
            )
            "APPL" -> AISummary(
                stockSymbol = "APPL",
                summary = "Apple's latest product announcements including the new M3 chip lineup and expanded services portfolio have strengthened investor confidence. The company's focus on AI integration across its ecosystem and growing services revenue create multiple growth vectors for sustained performance.",
                keyPoints = listOf(
                    "New M3 chip lineup showing strong performance metrics",
                    "AI integration across product ecosystem expanding",
                    "Services revenue growing at double-digit rates",
                    "Strong brand loyalty supporting premium pricing"
                ),
                sentiment = "Positive",
                generatedAt = "5 minutes ago"
            )
            "QQQ" -> AISummary(
                stockSymbol = "QQQ",
                summary = "The Invesco QQQ Trust reflects current market dynamics with technology sector rotation affecting performance. Recent rebalancing and institutional flows suggest mixed sentiment as investors weigh growth prospects against valuation concerns in the current interest rate environment.",
                keyPoints = listOf(
                    "Technology sector rotation creating volatility",
                    "Institutional flows showing mixed signals",
                    "Interest rate environment affecting growth valuations",
                    "Recent rebalancing impacting short-term performance"
                ),
                sentiment = "Neutral",
                generatedAt = "10 minutes ago"
            )
            else -> AISummary(
                stockSymbol = stockSymbol,
                summary = "Based on recent market analysis, ${stockSymbol} showed mixed signals from technical indicators. Limited news flow today suggests the price movement was primarily driven by broader market sentiment and sector rotation patterns.",
                keyPoints = listOf(
                    "Mixed technical indicators",
                    "Limited company-specific news",
                    "Influenced by broader market sentiment",
                    "Sector rotation affecting price"
                ),
                sentiment = "Neutral",
                generatedAt = "15 minutes ago"
            )
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete, //TODO
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Summary",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        item {
            // Stock symbol and sentiment
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = detailedSummary.stockSymbol,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        SentimentChip(sentiment = detailedSummary.sentiment)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Generated ${detailedSummary.generatedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            // Main summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = detailedSummary.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }
        }
        
        item {
            // Key points
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Key Points",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        items(detailedSummary.keyPoints) { point ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {}
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = point,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}