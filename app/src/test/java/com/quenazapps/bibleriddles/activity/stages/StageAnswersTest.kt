package com.quenazapps.bibleriddles.activity.stages

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StageAnswersTest {
    @Test
    fun acceptsMainAnswerAndEveryAlternativeAfterNormalizingBothSides() {
        val answers = StageAnswers("Ezequiel", listOf("Ezekiel"))
        assertTrue(answers.accepts(" EZEQUIEL. "))
        assertFalse(answers.accepts("profeta   ezequiel!"))
        assertTrue(answers.accepts("ézékiel"))
        assertFalse(answers.accepts("Davi"))
        assertFalse(answers.accepts("profeta"))
        assertFalse(answers.accepts("Ezequiel ou Davi"))
    }

    @Test
    fun normalizesConfiguredAlternativesNotOnlyPlayerInput() {
        val answers = StageAnswers("4", listOf("QUÁTRO!"))
        assertTrue(answers.accepts("4"))
        assertTrue(answers.accepts("quatro"))
        assertFalse(answers.accepts("QUARTO DIA."))
        assertFalse(answers.accepts("dia4"))
        assertFalse(answers.accepts("3"))
    }

    @Test
    fun stageTwoAcceptsOnlyHoneyAfterNormalization() {
        val answers = StageAnswers("MEL")
        assertTrue(answers.accepts("mel"))
        assertTrue(answers.accepts("MeL"))
        assertTrue(answers.accepts("M.E-L!"))
        assertFalse(answers.accepts("MEL E LEAO"))
        assertFalse(answers.accepts("O que é mais doce que o mel? O que é mais forte que o leão?"))
        assertFalse(answers.accepts("leão"))
        assertFalse(answers.accepts("mell"))
        assertFalse(answers.accepts("me"))
        assertEquals("MEL", answers.mainAnswer)
    }

    @Test
    fun handlesUnicodePunctuationAndWhitespace() {
        val answers = StageAnswers("Davi")
        assertTrue(answers.accepts("“DÁVI”"))
        assertTrue(answers.accepts("[Da—vi]"))
        assertFalse(answers.accepts("O\u00a0rei\n\tDavi"))
        assertFalse(answers.accepts("Da\u00a0vi"))
        assertFalse(answers.accepts("[O rei Davi]"))
    }

    @Test
    fun rejectsBlankPunctuationOnlyAndSymbolSubmissions() {
        val answers = StageAnswers("Davi")
        for (input in listOf("", " ", "...", "?!", "\n\t", "“”")) {
            assertFalse(answers.accepts(input))
        }
        assertFalse(answers.accepts("Da\$vi"))
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

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPhraseAsMainAnswer() {
        StageAnswers("Rei Davi")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPhraseAsAlternative() {
        StageAnswers("Davi", listOf("O rei Davi"))
    }

    @Test
    fun rejectsTyposRatherThanAcceptingApproximateMatches() {
        assertFalse(StageAnswers("Ezequiel").accepts("Ezequie"))
        assertFalse(StageAnswers("4").accepts("44"))
        assertFalse(StageAnswers("MEL").accepts("x".repeat(100_000)))
    }
}
