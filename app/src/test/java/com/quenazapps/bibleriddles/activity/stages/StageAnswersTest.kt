package com.quenazapps.bibleriddles.activity.stages

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StageAnswersTest {
    @Test
    fun acceptsMainAnswerAndEveryAlternativeAfterNormalizingBothSides() {
        val answers = StageAnswers("Ezequiel", listOf("Profeta Ezequiel", "Ezekiel"))
        assertTrue(answers.accepts(" EZEQUIEL. "))
        assertTrue(answers.accepts("profeta   ezequiel!"))
        assertTrue(answers.accepts("ézékiel"))
        assertFalse(answers.accepts("Davi"))
        assertFalse(answers.accepts("profeta"))
        assertFalse(answers.accepts("Ezequiel ou Davi"))
    }

    @Test
    fun normalizesConfiguredAlternativesNotOnlyPlayerInput() {
        val answers = StageAnswers("4", listOf("  QUÁTRO!  ", "Quarto—dia"))
        assertTrue(answers.accepts("4"))
        assertTrue(answers.accepts("quatro"))
        assertTrue(answers.accepts("QUARTO DIA."))
        assertFalse(answers.accepts("3"))
    }

    @Test
    fun acceptsShortRiddleSolutionButNotAnIncompleteAnswer() {
        val main = "O que é mais doce que o mel? O que é mais forte que o leão?"
        val answers = StageAnswers(main, listOf("Mel e leão", "Leão e mel"))
        assertTrue(answers.accepts(main))
        assertTrue(answers.accepts("MEL E LEAO"))
        assertTrue(answers.accepts("leão e mel."))
        assertFalse(answers.accepts("mel"))
        assertFalse(answers.accepts("leão"))
        assertEquals(main, answers.mainAnswer)
    }

    @Test
    fun handlesUnicodePunctuationAndWhitespace() {
        val answers = StageAnswers("Davi", listOf("O rei Davi"))
        assertTrue(answers.accepts("“DÁVI”"))
        assertTrue(answers.accepts("O\u00a0rei\n\tDavi"))
        assertTrue(answers.accepts("[O rei Davi]"))
    }

    @Test
    fun blankAlternativesNeverAllowBlankSubmissions() {
        val answers = StageAnswers("Davi", listOf("", "  ", "..."))
        for (input in listOf("", " ", "...", "?!", "\n\t", "“”")) {
            assertFalse(answers.accepts(input))
        }
    }

    @Test
    fun supportsMainAnswerWithoutAlternatives() {
        val answers = StageAnswers("Davi")
        assertTrue(answers.accepts("DAVI"))
        assertFalse(answers.accepts("David"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingMainAnswer() {
        StageAnswers("...", listOf("Davi"))
    }
}
