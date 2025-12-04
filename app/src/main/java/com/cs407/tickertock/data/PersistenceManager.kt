package com.cs407.tickertock.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Data class representing the entire app state for persistence
data class AppState(
    val watchlistStocks: List<String> = emptyList(),
    val stockDataMap: Map<String, Stock> = emptyMap(),
    val newsDataMap: Map<String, List<NewsArticle>> = emptyMap(),
    val swipedArticles: Map<String, Set<String>> = emptyMap(),
    val articleIndexPerStock: Map<String, Int> = emptyMap(),
    val endMessageShownForStocks: Set<String> = emptySet(),
    val aiSummaries: Map<String, String> = emptyMap(),
    val favoritedStock: String? = null
)

//Singleton class to manage app state persistence using SharedPreferences
class PersistenceManager private constructor(context: Context) {

    // holds the SharedPreferences instance
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    //Gson instance for JSON serialization/deserialization
    private val gson = Gson()

    // Define/creating static values
    companion object {
        private const val PREFS_NAME = "tickertock_prefs"
        private const val KEY_APP_STATE = "app_state"

        @Volatile // Thread safe
        private var instance: PersistenceManager? = null //Can be null if not value

        fun getInstance(context: Context): PersistenceManager {
            return instance ?: synchronized(this) { // Prevents two threads from creating two instances at the same time
                instance ?: PersistenceManager(context.applicationContext).also {
                    instance = it // Assigns the newly created PersistenceManager (Line above) to the static variable
                }
            }
        }
    }

//Save the entire app state to SharedPreferences
    fun saveAppState(appState: AppState) {
        try {
            //Serialize
            val json = gson.toJson(appState)
            //Save
            sharedPreferences.edit().putString(KEY_APP_STATE, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
            // Log error but don't crash the app
        }
    }

//Load the app state from SharedPreferences
//Returns null if no saved state exists or if there's an error
    fun loadAppState(): AppState? {
        return try {
            //Retrieve Data
            val json = sharedPreferences.getString(KEY_APP_STATE, null)

            //Might not have app state
            if (json != null) {
                //Deserialize into Appstate class and return
                gson.fromJson(json, AppState::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
