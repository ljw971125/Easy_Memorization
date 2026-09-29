package com.example.easy_memorization

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

object QuestionRepository {
    private const val FILE_NAME = "question_sets.json"
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun saveQuestionSets(context: Context, sets: List<QuestionSet>) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            val jsonString = json.encodeToString(sets)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadQuestionSets(context: Context): List<QuestionSet> {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString<List<QuestionSet>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
