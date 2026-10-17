package com.example.easy_memorization

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

object LenientQuestionParser {

    private val lenientJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val textKeys = setOf("question", "q", "text", "문제")
    private val optionsKeys = setOf("options", "choices", "answers", "보기", "선택지")
    private val answerKeys = setOf("answer", "correctanswer", "correctanswerindex", "correct", "정답")
    private val explanationKeys = setOf("explanation", "desc", "description", "설명", "해설")
    private val titleKeys = setOf("title", "name", "제목")
    private val setArrayKeys = setOf("sets", "questionsets", "세트")
    private val questionsKeys = setOf("questions", "items", "questionlist", "list")

    fun parseQuestionSets(jsonString: String, defaultTitle: String = "Untitled"): List<QuestionSet> {
        val element = try {
            lenientJson.parseToJsonElement(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("json: invalid JSON: ${e.message}", e)
        }
        return when (element) {
            is JsonArray -> parseTopLevelArray(element, defaultTitle)
            is JsonObject -> parseTopLevelObject(element, defaultTitle)
            else -> throw IllegalArgumentException(
                "json: top-level value must be an object or an array, " +
                    "found ${describe(element)}"
            )
        }
    }

    private fun parseTopLevelObject(obj: JsonObject, defaultTitle: String): List<QuestionSet> {
        findArrayField(obj, setArrayKeys)?.let { setsArray ->
            return setsArray.mapIndexed { setIndex, setElement ->
                if (setElement !is JsonObject) {
                    throw IllegalArgumentException(
                        "set[$setIndex]: expected a set object, found ${describe(setElement)}"
                    )
                }
                parseSet(setElement, setIndex, defaultTitle)
            }
        }
        if (looksLikeQuestion(obj)) {
            return listOf(
                QuestionSet(
                    title = lookupString(obj, titleKeys) ?: defaultTitle,
                    questions = listOf(parseQuestion(obj, setIndex = 0, questionIndex = 0))
                )
            )
        }
        return listOf(parseSet(obj, setIndex = 0, defaultTitle = defaultTitle))
    }

    private fun parseTopLevelArray(array: JsonArray, defaultTitle: String): List<QuestionSet> {
        if (array.isEmpty()) return listOf(QuestionSet(title = defaultTitle, questions = emptyList()))
        val first = array[0]
        if (first is JsonObject && !looksLikeQuestion(first) &&
            (findField(first, titleKeys) != null ||
                findArrayField(first, questionsKeys) != null ||
                findArrayField(first, setArrayKeys) != null)
        ) {
            return array.mapIndexed { setIndex, setElement ->
                if (setElement !is JsonObject) {
                    throw IllegalArgumentException(
                        "set[$setIndex]: expected a set object, found ${describe(setElement)}"
                    )
                }
                parseSet(setElement, setIndex, defaultTitle)
            }
        }
        return listOf(
            QuestionSet(
                title = defaultTitle,
                questions = array.mapIndexed { questionIndex, questionElement ->
                    if (questionElement !is JsonObject) {
                        throw IllegalArgumentException(
                            "set[0].question[$questionIndex]: expected a question object, " +
                                "found ${describe(questionElement)}"
                        )
                    }
                    parseQuestion(questionElement, setIndex = 0, questionIndex = questionIndex)
                }
            )
        )
    }

    private fun parseSet(obj: JsonObject, setIndex: Int, defaultTitle: String): QuestionSet {
        val title = lookupString(obj, titleKeys) ?: defaultTitle
        val questionsArray = findArrayField(obj, questionsKeys)
        if (questionsArray == null) {
            if (findField(obj, textKeys) != null ||
                findField(obj, optionsKeys) != null ||
                findField(obj, answerKeys) != null
            ) {
                return QuestionSet(
                    title = title,
                    questions = listOf(parseQuestion(obj, setIndex = setIndex, questionIndex = 0))
                )
            }
            if (findField(obj, titleKeys) != null) {
                return QuestionSet(title = title, questions = emptyList())
            }
            throw IllegalArgumentException(
                "set[$setIndex]: no questions found (expected an array under " +
                    "'questions'/'items') and no question fields present"
            )
        }
        val questions = questionsArray.mapIndexed { questionIndex, questionElement ->
            if (questionElement !is JsonObject) {
                throw IllegalArgumentException(
                    "set[$setIndex].question[$questionIndex]: expected a question object, " +
                        "found ${describe(questionElement)}"
                )
            }
            parseQuestion(questionElement, setIndex = setIndex, questionIndex = questionIndex)
        }
        return QuestionSet(title = title, questions = questions)
    }

    private fun parseQuestion(obj: JsonObject, setIndex: Int, questionIndex: Int): Question {
        fun fail(reason: String): Nothing {
            throw IllegalArgumentException("set[$setIndex].question[$questionIndex]: $reason")
        }
        val text = lookupString(obj, textKeys)?.takeIf { it.isNotBlank() }
            ?: fail("missing question text (expected one of 'question'/'q'/'text'/'문제')")
        val optionsElement = findField(obj, optionsKeys)
            ?: fail("missing options (expected one of 'options'/'choices'/'answers'/'보기'/'선택지')")
        if (optionsElement !is JsonArray) {
            fail("options must be an array, found ${describe(optionsElement)}")
        }
        val options = optionsElement.mapIndexed { optionIndex, optionElement ->
            if (optionElement is JsonPrimitive && !optionElement.isString) {
                optionElement.contentOrNull ?: fail("option[$optionIndex] must be a string")
            } else if (optionElement is JsonPrimitive) {
                optionElement.content
            } else {
                fail("option[$optionIndex] must be a string, found ${describe(optionElement)}")
            }
        }
        if (options.size < 2) {
            fail("at least 2 options are required, found ${options.size}")
        }
        val answerElement = findField(obj, answerKeys)
            ?: fail("missing answer (expected one of 'answer'/'correctAnswer'/'correctAnswerIndex'/'correct'/'정답')")
        val correctAnswerIndex = resolveAnswer(answerElement, options) { fail(it) }
        val explanation = lookupString(obj, explanationKeys) ?: ""
        return Question(
            text = text,
            options = options,
            correctAnswerIndex = correctAnswerIndex,
            explanation = explanation
        )
    }

    private fun resolveAnswer(
        answerElement: JsonElement,
        options: List<String>,
        fail: (String) -> Nothing
    ): Int {
        if (answerElement is JsonPrimitive) {
            val asInt = answerElement.intOrNull
                ?: answerElement.contentOrNull?.trim()?.toIntOrNull()
            if (asInt != null) {
                if (asInt in 0 until options.size) return asInt
                if (asInt in 1..options.size) return asInt - 1
                // Out of range as an index: it may still be the exact option text (e.g. "4").
                if (answerElement.isString) {
                    val textIndex = options.indexOf(answerElement.content)
                    if (textIndex >= 0) return textIndex
                }
                fail("answer index $asInt out of range for ${options.size} options")
            }
            if (answerElement.isString) {
                val text = answerElement.content
                val index = options.indexOf(text)
                if (index >= 0) return index
                fail("answer text '$text' does not match any option exactly")
            }
            // Non-string primitives (boolean, etc.) that are not ints fall through.
            answerElement.longOrNull ?: answerElement.booleanOrNull
            fail("answer must be an option index or the exact option text")
        }
        fail("answer must be an option index or the exact option text, found ${describe(answerElement)}")
    }

    private fun looksLikeQuestion(obj: JsonObject): Boolean =
        findField(obj, textKeys) != null &&
            (findField(obj, optionsKeys) != null || findField(obj, answerKeys) != null)

    private fun asciiLowercase(name: String): String {
        val chars = name.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (c in 'A'..'Z') chars[i] = c + ('a' - 'A')
        }
        return String(chars)
    }

    private fun fieldMap(obj: JsonObject): Map<String, JsonElement> {
        val map = LinkedHashMap<String, JsonElement>(obj.size)
        for ((key, value) in obj) {
            map.putIfAbsent(asciiLowercase(key), value)
        }
        return map
    }

    private fun findField(obj: JsonObject, keys: Set<String>): JsonElement? {
        val map = fieldMap(obj)
        for (key in keys) {
            map[asciiLowercase(key)]?.let { return it }
        }
        return null
    }

    private fun findArrayField(obj: JsonObject, keys: Set<String>): JsonArray? {
        val element = findField(obj, keys) ?: return null
        if (element is JsonNull) return null
        if (element !is JsonArray) {
            throw IllegalArgumentException(
                "expected an array for '${keys.first()}', found ${describe(element)}"
            )
        }
        return element
    }

    private fun lookupString(obj: JsonObject, keys: Set<String>): String? {
        val element = findField(obj, keys) ?: return null
        if (element is JsonNull) return null
        if (element is JsonPrimitive) {
            if (element.isString) return element.content
            return element.contentOrNull
        }
        throw IllegalArgumentException(
            "expected a string for '${keys.first()}', found ${describe(element)}"
        )
    }

    private fun describe(element: JsonElement): String = when (element) {
        is JsonObject -> "object"
        is JsonArray -> "array"
        is JsonNull -> "null"
        is JsonPrimitive ->
            if (element.isString) "string" else "number/boolean"
    }
}
