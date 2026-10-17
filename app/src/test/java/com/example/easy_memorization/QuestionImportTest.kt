package com.example.easy_memorization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class QuestionImportTest {

    @Test
    fun strictArray_shape_usesDefaultTitle() {
        val json = """
            [
              {"text": "Q?", "options": ["A", "B"], "correctAnswerIndex": 1, "explanation": "E"}
            ]
        """.trimIndent()
        val sets = parseImportedSets(json, defaultTitle = "File")
        assertEquals(1, sets.size)
        assertEquals("File", sets[0].title)
        assertEquals(1, sets[0].questions.size)
        assertEquals("Q?", sets[0].questions[0].text)
        assertEquals(1, sets[0].questions[0].correctAnswerIndex)
    }

    @Test
    fun lenientShape_fallsBackToLenientParser() {
        val json = """
            {
              "name": "Alias Set",
              "questions": [
                {"q": "Q?", "choices": ["A", "B"], "correct": "B"}
              ]
            }
        """.trimIndent()
        val sets = parseImportedSets(json, defaultTitle = "File")
        assertEquals(1, sets.size)
        assertEquals("Alias Set", sets[0].title)
        assertEquals(1, sets[0].questions[0].correctAnswerIndex)
    }

    @Test
    fun invalidContent_throwsWithParserMessage() {
        val json = """
            {
              "title": "S",
              "questions": [
                {"question": "Q?", "options": ["A", "B"], "answer": 9}
              ]
            }
        """.trimIndent()
        try {
            parseImportedSets(json, defaultTitle = "File")
            fail("expected exception")
        } catch (e: Exception) {
            assertTrue("message was: ${e.message}", e.message!!.contains("set[0].question[0]"))
        }
    }

    @Test
    fun stripJsonExtension_stripsTrailingJsonOnly() {
        assertEquals("my-set", stripJsonExtension("my-set.json"))
        assertEquals("my-set", stripJsonExtension("my-set.JSON"))
        assertEquals("my-set.txt", stripJsonExtension("my-set.txt"))
        assertEquals("my-set", stripJsonExtension("my-set"))
    }

    @Test
    fun sampleJson_parsesSuccessfully() {
        val sets = parseImportedSets(SAMPLE_IMPORT_JSON, defaultTitle = "Sample")
        assertEquals(1, sets.size)
        assertEquals("과학 상식", sets[0].title)
        assertEquals(2, sets[0].questions.size)
        assertEquals("물의 화학식은?", sets[0].questions[0].text)
        assertEquals(0, sets[0].questions[0].correctAnswerIndex)
        assertEquals("물은 수소 2개와 산소 1개로 이루어져 있습니다.", sets[0].questions[0].explanation)
        assertEquals("2 + 2 = ?", sets[0].questions[1].text)
        assertEquals(1, sets[0].questions[1].correctAnswerIndex)
        assertEquals("", sets[0].questions[1].explanation)
    }
}
