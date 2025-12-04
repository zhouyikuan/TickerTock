package com.cs407.tickertock.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.cs407.tickertock.MainActivity
import com.cs407.tickertock.R
import com.cs407.tickertock.data.PersistenceManager

class StockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget is created
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // Last widget is removed
        super.onDisabled(context)
    }

    companion object {
        /**
         * Update the widget with current favorited stock data
         */
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.stock_widget)

            // Loads the app as a background process to get info for the widget
            val persistenceManager = PersistenceManager.getInstance(context)
            val appState = persistenceManager.loadAppState()

            if (appState != null && appState.favoritedStock != null) {
                val favoritedStock = appState.favoritedStock
                val stock = appState.stockDataMap[favoritedStock]

                if (stock != null) {
                    // Show stock data
                    views.setViewVisibility(R.id.widget_stock_symbol, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_stock_price, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_stock_change, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_stock_percentage, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_no_favorite_message, View.GONE)

                    // Set stock symbol
                    views.setTextViewText(R.id.widget_stock_symbol, stock.symbol)

                    // Set current price
                    views.setTextViewText(
                        R.id.widget_stock_price,
                        "$${String.format("%.2f", stock.currentPrice)}"
                    )

                    // Determine color based on positive/negative change
                    val changeColor = if (stock.isPositive) {
                        Color.parseColor("#4CAF50") // Green
                    } else {
                        Color.parseColor("#F44336") // Red
                    }

                    // Set price change with color
                    val changeText = "${if (stock.isPositive) "+" else ""}$${String.format("%.2f", stock.priceChange)}"
                    views.setTextViewText(R.id.widget_stock_change, changeText)
                    views.setTextColor(R.id.widget_stock_change, changeColor)

                    // Set percentage change with color
                    val percentageText = "(${if (stock.isPositive) "+" else ""}${String.format("%.2f", stock.percentageChange)}%)"
                    views.setTextViewText(R.id.widget_stock_percentage, percentageText)
                    views.setTextColor(R.id.widget_stock_percentage, changeColor)
                } else {
                    // No stock data available for the favorited stock
                    showNoFavoriteMessage(views)
                }
            } else {
                // No favorited stock
                showNoFavoriteMessage(views)
            }

            // Set up click to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_stock_symbol, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_no_favorite_message, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         Default message
         */
        private fun showNoFavoriteMessage(views: RemoteViews) {
            views.setViewVisibility(R.id.widget_stock_symbol, View.GONE)
            views.setViewVisibility(R.id.widget_stock_price, View.GONE)
            views.setViewVisibility(R.id.widget_stock_change, View.GONE)
            views.setViewVisibility(R.id.widget_stock_percentage, View.GONE)
            views.setViewVisibility(R.id.widget_no_favorite_message, View.VISIBLE)
        }

        /**
         * Trigger widget update from anywhere in the app
         */
        fun triggerWidgetUpdate(context: Context) {
            val intent = Intent(context, StockWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, StockWidgetProvider::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
