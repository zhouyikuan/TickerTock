package com.cs407.tickertock.api

object ApiKeyManager {
    private val apiKeys = listOf(
        "05RQ0L87XP2WOI00",
        "23PS66HJD32QY3OB",
        "2XV5VZV6Z20IER6A",
        "H7I8RJMG16JCPYQ1",
        "LL05W6MO1D8V6X2Q",
        "AJIF8YJFUPYHLZSJ",
        "OL9E687N6RTWQQG9",
        "V04XO1XQ9IIQTI6D",
        "8Y6AGL7D8OP0RQD9",
        "WJXHDEQ1X42EDKB5",
        "8A6047BZX69G1FUQ",
        "KBNO28IH2U492416",
        "1F3UPDGC5YZYH3PX",
        "1KNM5S2NGXR74FU1",
        "KBIL4YSS36RXPQLT",
        "35L532QWI2EJD3SR"
    )

    private var currentIndex = 0

    /**
     * Get the next API key in round-robin fashion
     */
    @Synchronized
    fun getNextKey(): String {
        val key = apiKeys[currentIndex]
        currentIndex = (currentIndex + 1) % apiKeys.size
        return key
    }

    /**
     * Get total number of API keys available
     */
    fun getTotalKeys(): Int {
        return apiKeys.size
    }
}
