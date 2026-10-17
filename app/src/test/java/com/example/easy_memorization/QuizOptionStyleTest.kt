package com.example.easy_memorization

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * resolveQuizOptionStyle is pure presentation logic extracted during the
 * minimal modern-AI-app restyle: it decides which visual style a quiz option
 * button gets. Behavior must stay: unselected -> DEFAULT, selected+correct ->
 * CORRECT, selected+wrong -> WRONG.
 */
class QuizOptionStyleTest {

    @Test
    fun unselectedOption_isDefault_regardlessOfCorrectness() {
        assertEquals(QuizOptionStyle.DEFAULT, resolveQuizOptionStyle(isSelected = false, isCorrect = false))
        assertEquals(QuizOptionStyle.DEFAULT, resolveQuizOptionStyle(isSelected = false, isCorrect = true))
    }

    @Test
    fun selectedCorrectOption_isCorrect() {
        assertEquals(QuizOptionStyle.CORRECT, resolveQuizOptionStyle(isSelected = true, isCorrect = true))
    }

    @Test
    fun selectedWrongOption_isWrong() {
        assertEquals(QuizOptionStyle.WRONG, resolveQuizOptionStyle(isSelected = true, isCorrect = false))
    }
}
