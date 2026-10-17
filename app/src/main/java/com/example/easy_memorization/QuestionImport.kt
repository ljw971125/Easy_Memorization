package com.example.easy_memorization

import kotlinx.serialization.json.Json

private val importJson = Json { ignoreUnknownKeys = true }

/** Strips one trailing `.json` extension (case-insensitive) from a display name. */
fun stripJsonExtension(displayName: String): String =
    if (displayName.endsWith(".json", ignoreCase = true)) {
        displayName.dropLast(".json".length)
    } else {
        displayName
    }

/**
 * Example JSON shown on the import screen. Must always parse successfully;
 * covered by a unit test.
 */
val SAMPLE_IMPORT_JSON = """
{
  "name": "과학 상식",
  "questions": [
    {
      "q": "물의 화학식은?",
      "choices": ["H2O", "CO2", "O2"],
      "correct": "H2O",
      "설명": "물은 수소 2개와 산소 1개로 이루어져 있습니다."
    },
    {
      "문제": "2 + 2 = ?",
      "보기": ["3", "4", "5"],
      "정답": "4"
    }
  ]
}
""".trimIndent()

/**
 * Parses imported file content into question sets.
 *
 * Tries the strict legacy format first (a JSON array of [Question], kept for
 * backward compatibility), then falls back to [LenientQuestionParser].
 * Throws with a descriptive message when both fail.
 */
fun parseImportedSets(content: String, defaultTitle: String): List<QuestionSet> {
    try {
        val questions = importJson.decodeFromString<List<Question>>(content)
        return listOf(QuestionSet(title = defaultTitle, questions = questions))
    } catch (_: Exception) {
        // Fall through to the lenient parser.
    }
    return LenientQuestionParser.parseQuestionSets(content, defaultTitle)
}
