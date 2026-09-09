package com.quenazapps.bibleriddles.activity.stages

import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerNormalizationTest {
    @Test
    fun normalizationIgnoresAccentsPunctuationCaseAndExtraSpaces() {
        val expected = "O que é mais doce que o mel? O que é mais forte que o leão?"
        val playerAnswer = "  o que e mais doce que o mel, o que e mais forte que o leao.  "

        assertEquals(normalizeAnswer(expected), normalizeAnswer(playerAnswer))
    }
}
