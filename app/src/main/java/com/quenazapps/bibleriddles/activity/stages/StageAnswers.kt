package com.quenazapps.bibleriddles.activity.stages

import java.text.Normalizer
import java.util.Locale

/** The main answer is used for review; alternatives only expand accepted submissions. */
internal class StageAnswers(
    val mainAnswer: String,
    alternativeAnswers: List<String> = emptyList(),
) {
    private val acceptedAnswers: Set<String> = (listOf(mainAnswer) + alternativeAnswers)
        .map(::normalizeAnswer)
        .filter(String::isNotBlank)
        .toSet()

    init {
        require(normalizeAnswer(mainAnswer).isNotBlank()) { "A stage must have a main answer" }
    }

    fun accepts(answer: String): Boolean {
        val normalizedAnswer = normalizeAnswer(answer)
        return normalizedAnswer.isNotBlank() && normalizedAnswer in acceptedAnswers
    }

    /** Similarity = 100 * (1 - Levenshtein distance / longer normalized text length). */
    fun acceptsSimilar(answer: String, minimumSimilarityPercent: Int): Boolean {
        require(minimumSimilarityPercent in 1..100)
        val normalizedAnswer = normalizeAnswer(answer)
        if (normalizedAnswer.isBlank()) return false

        return acceptedAnswers.any { correctAnswer ->
            if (normalizedAnswer == correctAnswer) {
                true
            } else {
                val longerLength = maxOf(normalizedAnswer.length, correctAnswer.length)
                val shorterLength = minOf(normalizedAnswer.length, correctAnswer.length)
                val requiredScore = minimumSimilarityPercent.toLong() * longerLength
                // Even a perfect substring cannot reach the threshold if lengths differ too much.
                // This also avoids expensive comparisons for excessively long submissions.
                shorterLength * 100L >= requiredScore &&
                    (longerLength - levenshteinDistance(normalizedAnswer, correctAnswer)) * 100L >= requiredScore
            }
        }
    }
}

private fun levenshteinDistance(first: String, second: String): Int {
    val shorter = if (first.length <= second.length) first else second
    val longer = if (first.length <= second.length) second else first
    var previous = IntArray(shorter.length + 1) { it }
    var current = IntArray(shorter.length + 1)

    for (row in 1..longer.length) {
        current[0] = row
        for (column in 1..shorter.length) {
            val replacementCost = if (longer[row - 1] == shorter[column - 1]) 0 else 1
            current[column] = minOf(
                previous[column] + 1,
                current[column - 1] + 1,
                previous[column - 1] + replacementCost,
            )
        }
        val swap = previous
        previous = current
        current = swap
    }
    return previous[shorter.length]
}

internal fun normalizeAnswer(value: String): String = Normalizer
    .normalize(value, Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "")
    .lowercase(Locale.ROOT)
    .replace("\\p{P}+".toRegex(), " ")
    .replace("[\\p{Z}\\s]+".toRegex(), " ")
    .trim()
