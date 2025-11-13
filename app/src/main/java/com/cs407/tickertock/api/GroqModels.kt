package com.cs407.tickertock.api

import com.google.gson.annotations.SerializedName

data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.5,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqChatResponse(
    val id: String,
    val choices: List<GroqChoice>,
    val usage: GroqUsage
)

data class GroqChoice(
    val message: GroqMessage,
    val index: Int,
    @SerializedName("finish_reason")
    val finishReason: String
)

data class GroqUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)
