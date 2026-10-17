package com.example.easy_memorization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LenientQuestionParserTest {

    @Test
    fun aliases_areAccepted() {
        val json = """
            {
              "name": "Alias Set",
              "questions": [
                {
                  "q": "Capital of France?",
                  "choices": ["Berlin", "Paris", "Rome"],
                  "correct": "Paris",
                  "desc": "Paris is the capital."
                },
                {
                  "문제": "2 + 2?",
                  "보기": ["3", "4", "5"],
                  "정답": "4",
                  "설명": "Basic math."
                },
                {
                  "text": "Sky color?",
                  "answers": ["Blue", "Green"],
                  "correctAnswer": "Blue",
                  "description": "Clear sky."
                },
                {
                  "question": "Korean choice?",
                  "선택지": ["A", "B"],
                  "answer": "B",
                  "해설": "Second."
                }
              ]
            }
        """.trimIndent()
        val sets = LenientQuestionParser.parseQuestionSets(json)
        assertEquals(1, sets.size)
        assertEquals("Alias Set", sets[0].title)
        assertEquals(4, sets[0].questions.size)
        assertEquals("Capital of France?", sets[0].questions[0].text)
        assertEquals(1, sets[0].questions[0].correctAnswerIndex)
        assertEquals("Paris is the capital.", sets[0].questions[0].explanation)
        assertEquals(1, sets[0].questions[1].correctAnswerIndex)
        assertEquals("Basic math.", sets[0].questions[1].explanation)
        assertEquals(0, sets[0].questions[2].correctAnswerIndex)
        assertEquals(1, sets[0].questions[3].correctAnswerIndex)
    }

    @Test
    fun setTitle_aliases_areAccepted() {
        assertEquals(
            "T1",
            LenientQuestionParser.parseQuestionSets("""{"title": "T1", "questions": []}""")[0].title
        )
        assertEquals(
            "T2",
            LenientQuestionParser.parseQuestionSets("""{"name": "T2", "questions": []}""")[0].title
        )
        assertEquals(
            "T3",
            LenientQuestionParser.parseQuestionSets("""{"제목": "T3", "questions": []}""")[0].title
        )
        // ASCII title keys are case-insensitive.
        assertEquals(
            "T4",
            LenientQuestionParser.parseQuestionSets("""{"TITLE": "T4", "questions": []}""")[0].title
        )
    }

    @Test
    fun zeroBasedIndex_answer_isAccepted() {
        val json = """
            {
              "title": "S",
              "questions": [
                {"question": "Q?", "options": ["A", "B", "C"], "answer": 0}
              ]
            }
        """.trimIndent()
        val sets = LenientQuestionParser.parseQuestionSets(json)
        assertEquals(0, sets[0].questions[0].correctAnswerIndex)
    }

    @Test
    fun oneBasedIndex_answer_isAccepted() {
        val json = """
            {
              "title": "S",
              "questions": [
                {"question": "Q1?", "options": ["A", "B", "C"], "answer": 3},
                {"question": "Q2?", "options": ["A", "B", "C"], "correctAnswerIndex": "3"}
              ]
            }
        """.trimIndent()
        val sets = LenientQuestionParser.parseQuestionSets(json)
        assertEquals(2, sets[0].questions[0].correctAnswerIndex)
        assertEquals(2, sets[0].questions[1].correctAnswerIndex)
    }

    @Test
    fun optionText_answer_isAccepted() {
        val json = """
            {
              "title": "S",
              "questions": [
                {"question": "Q?", "options": ["A", "B", "C"], "answer": "C"}
              ]
            }
        """.trimIndent()
        val sets = LenientQuestionParser.parseQuestionSets(json)
        assertEquals(2, sets[0].questions[0].correctAnswerIndex)
        assertEquals("", sets[0].questions[0].explanation)
    }

    @Test
    fun singleSetObject_shape_isAccepted() {
        val json = """
            {
              "title": "Single",
              "questions": [
                {"question": "Q?", "options": ["A", "B"], "answer": 1, "extra": "ignored"}
              ]
            }
        """.trimIndent()
        val sets = LenientQuestionParser.parseQuestionSets(json)
        assertEquals(1, sets.size)
        assertEquals("Single", sets[0].title)
        assertEquals(1, sets[0].questions.size)
    }

    @Test
    fun setArray_shapes_areAccepted() {
        val template = { key: String ->
            """
            {
              "$key": [
                {"title": "A", "questions": [{"question": "Q?", "options": ["X", "Y"], "answer": 0}]},
                {"title": "B", "questions": [{"question": "Q?", "options": ["X", "Y"], "answer": 1}]}
              ]
            }
            """.trimIndent()
        }
        for (key in listOf("sets", "questionSets", "세트", "SETS")) {
            val sets = LenientQuestionParser.parseQuestionSets(template(key))
            assertEquals("key=$key", 2, sets.size)
            assertEquals("key=$key", "A", sets[0].title)
            assertEquals("key=$key", "B", sets[1].title)
            assertEquals("key=$key", 0, sets[0].questions[0].correctAnswerIndex)
            assertEquals("key=$key", 1, sets[1].questions[0].correctAnswerIndex)
        }
    }

    @Test
    fun bareArray_shape_usesDefaultTitle() {
        val json = """
            [
              {"question": "Q1?", "options": ["A", "B"], "answer": 0},
              {"question": "Q2?", "options": ["A", "B"], "answer": 1}
            ]
        """.trimIndent()
        val sets = LenientQuestionParser.parseQuestionSets(json, defaultTitle = "Default")
        assertEquals(1, sets.size)
        assertEquals("Default", sets[0].title)
        assertEquals(2, sets[0].questions.size)
    }

    @Test
    fun singleQuestionObject_shape_isWrapped() {
        val json = """{"question": "Solo?", "options": ["A", "B"], "answer": 1}"""
        val sets = LenientQuestionParser.parseQuestionSets(json, defaultTitle = "Default")
        assertEquals(1, sets.size)
        assertEquals("Default", sets[0].title)
        assertEquals(1, sets[0].questions.size)
        assertEquals("Solo?", sets[0].questions[0].text)
        assertEquals(1, sets[0].questions[0].correctAnswerIndex)
    }

    @Test
    fun malformedJson_throws() {
        try {
            LenientQuestionParser.parseQuestionSets("{ not json")
            fail("expected exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("invalid JSON"))
        }
    }

    @Test
    fun invalidQuestion_throwsWithIndex() {
        val json = """
            {
              "title": "S",
              "questions": [
                {"question": "Q1?", "options": ["A", "B"], "answer": 0},
                {"question": "Q2?", "options": ["A", "B"], "answer": 7}
              ]
            }
        """.trimIndent()
        try {
            LenientQuestionParser.parseQuestionSets(json)
            fail("expected exception")
        } catch (e: Exception) {
            assertTrue("message was: ${e.message}", e.message!!.contains("set[0].question[1]"))
        }
    }
}
