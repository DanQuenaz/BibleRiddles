package com.quenazapps.bibleriddles.activity.stages

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StageAnswerSimilarityTest {
    @Test
    fun acceptsExactlyEightyPercentButRejectsBelowThreshold() {
        val answers = StageAnswers("abcdefghij")
        assertTrue(answers.acceptsSimilar("abcdxxghij", 80)) // 2 edits / 10 characters
        assertFalse(answers.acceptsSimilar("abcdxxxhij", 80)) // 3 edits / 10 characters
    }

    @Test
    fun countsMissingAndAddedCharactersAgainstLongerText() {
        val answers = StageAnswers("abcdefghij")
        assertTrue(answers.acceptsSimilar("abcdefgh", 80)) // 80%
        assertFalse(answers.acceptsSimilar("abcdefg", 80)) // 70%
        assertTrue(answers.acceptsSimilar("abcdefghijxy", 80)) // 83.33%
        assertFalse(answers.acceptsSimilar("abcdefghijxyz", 80)) // 76.92%
    }

    @Test
    fun normalizesBothMainAnswerAndSubmissionBeforeMeasuringSimilarity() {
        val answers = StageAnswers("  MÉL e LEÃO?!  ")
        assertTrue(answers.acceptsSimilar("mel e leao", 100))
        assertTrue(answers.acceptsSimilar("“MEL”\u00a0e\nleã!", 80))
    }

    @Test
    fun comparesEveryAlternativeAsWellAsTheMainAnswer() {
        val answers = StageAnswers("unrelated main answer", listOf("another option", "Mél e leão!"))
        assertTrue(answers.acceptsSimilar("unrelated main answe", 80))
        assertTrue(answers.acceptsSimilar("mel e leao", 100))
        assertTrue(answers.acceptsSimilar("mel e lea", 80))
        assertFalse(answers.acceptsSimilar("completely wrong", 80))
    }

    @Test
    fun acceptsSmallTyposInStageOneRiddleAnswer() {
        val answers = StageAnswers("O que é mais doce que o mel? O que é mais forte que o leão?")
        assertTrue(answers.acceptsSimilar("O que e mais doce que o mel? O que e mais fort que o leao?", 80))
        assertFalse(answers.acceptsSimilar("mel", 80))
    }

    @Test
    fun rejectsBlankAndPunctuationOnlySubmissions() {
        val answers = StageAnswers("Mel e leão", listOf("...", ""))
        for (input in listOf("", "  ", "?!", "“”", "\n\t")) {
            assertFalse(answers.acceptsSimilar(input, 80))
        }
    }

    @Test
    fun exactMatchingForOtherStagesIsUnchanged() {
        val answers = StageAnswers("Ezequiel", listOf("Profeta Ezequiel"))
        assertTrue(answers.accepts(" EZEQUIEL! "))
        assertFalse(answers.accepts("Ezequie"))
        assertTrue(answers.acceptsSimilar("Ezequie", 80))
        assertFalse(answers.acceptsSimilar("Ezequie", 100))
    }

    @Test
    fun rejectsVeryLongUnrelatedSubmissions() {
        assertFalse(StageAnswers("Mel e leão").acceptsSimilar("x".repeat(100_000), 80))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidSimilarityThreshold() {
        StageAnswers("Mel e leão").acceptsSimilar("mel e leao", 101)
    }
}
