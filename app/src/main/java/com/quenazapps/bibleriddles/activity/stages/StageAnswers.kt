package com.quenazapps.bibleriddles.activity.stages

import java.text.Normalizer
import java.util.Locale

/** Each accepted answer must be one word or one number, never a phrase. */
internal class StageAnswers(
    val mainAnswer: String,
    alternativeAnswers: List<String> = emptyList(),
) {
    private val acceptedAnswers: Set<String> = (listOf(mainAnswer) + alternativeAnswers)
        .map(::normalizeAnswer)
        .toSet()

    init {
        require(acceptedAnswers.all(::isWordOrNumber)) {
            "Every stage answer must be a single word or number"
        }
    }

    fun accepts(answer: String): Boolean {
        val normalizedAnswer = normalizeAnswer(answer)
        return isWordOrNumber(normalizedAnswer) && normalizedAnswer in acceptedAnswers
    }
}

private fun isWordOrNumber(value: String): Boolean = value.isNotEmpty() &&
    (value.all(Char::isLetter) || value.all(Char::isDigit))

/** Keep punctuation in the field, but never let typing or pasting introduce whitespace. */
internal fun filterAnswerInput(value: String): String = value.filterNot(Char::isWhitespace)

internal fun normalizeAnswer(value: String): String = Normalizer
    .normalize(value, Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "")
    .lowercase(Locale.ROOT)
    .replace("\\p{P}+".toRegex(), "")
    .trim()
