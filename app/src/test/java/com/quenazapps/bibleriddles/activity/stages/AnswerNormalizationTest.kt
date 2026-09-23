package com.quenazapps.bibleriddles.activity.stages

import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerNormalizationTest {
    @Test
    fun normalizationIgnoresCaseAccentsAndPunctuationInsideTheWord() {
        assertEquals("mel", normalizeAnswer("  “M-é.L!”  "))
        assertEquals("graca", normalizeAnswer("GrAçA?!"))
        assertEquals("42", normalizeAnswer("4.2!"))
    }

    @Test
    fun inputFilterRemovesAllWhitespaceButPreservesWhatThePlayerTyped() {
        assertEquals("MeL!", filterAnswerInput(" M\u00a0e\tL\n!\r\u2003\u202f "))
        assertEquals("GrAçA?!", filterAnswerInput("GrAçA?!"))
        assertEquals("", filterAnswerInput(" \t\n\u00a0"))
    }

    @Test
    fun normalizationDoesNotJoinWordsIntoAnAnswer() {
        assertEquals("m e l", normalizeAnswer("m e l"))
    }
}
