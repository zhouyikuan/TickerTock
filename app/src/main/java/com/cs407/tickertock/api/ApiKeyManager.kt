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

    /*
    "S1Q3JC1NDLE7ERI7",
"7GJ66YO83V3LXCJN",
"S9XTI2ITBQEI99LB",
"9VN02FGK60FGL723",
"TJH0L0KBOLQITN1K",
"8EZ2H7EQYHXLDMSN",
"L5DWR0TWHQS75PE3",
"PDXFW0RRMBZCOHYC",
"RRR97PZE28LKA2NP",
"1Y68UB0NP6HV5TKD",
"TC0C1L17CG78EFFX",
"SWVLWR5X3G5IY856",
"V88IWY3Q826SR90F",
"GONBE5UF1QF0LGHS",
"WJHS7BXJHRB611VT",
"S85H1CQZGSGF6NKI"
    */

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
