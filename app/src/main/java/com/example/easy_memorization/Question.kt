package com.example.easy_memorization

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String = ""
)

@Serializable
data class QuestionSet(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val questions: List<Question> = emptyList()
)
