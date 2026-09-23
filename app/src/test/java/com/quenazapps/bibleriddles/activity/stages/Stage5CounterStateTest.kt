package com.quenazapps.bibleriddles.activity.stages

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Stage5CounterStateTest {
    @Test
    fun portraitIsBlankAndEachDirectionStartsAtOne() {
        assertNull(Stage5CounterState().withOrientation(0).value)
        val left = Stage5CounterState().withOrientation(270)
        assertEquals(Stage5Side.LEFT, left.side)
        assertEquals(1, left.value)
        val right = Stage5CounterState().withOrientation(90)
        assertEquals(Stage5Side.RIGHT, right.side)
        assertEquals(1, right.value)
    }

    @Test
    fun leftCountsEverySecondAndStopsAt390() {
        var state = Stage5CounterState().withOrientation(270)
        for (expected in 1..390) {
            assertEquals(expected, state.value)
            state = state.advanceBy(999L)
            assertEquals(expected, state.value)
            state = state.advanceBy(1L)
        }
        assertEquals(390, state.advanceBy(600_000L).value)
    }

    @Test
    fun rightCountsEverySecondAndStopsAt40() {
        var state = Stage5CounterState().withOrientation(90)
        for (expected in 1..40) {
            assertEquals(expected, state.value)
            state = state.advanceBy(1_000L)
        }
        assertEquals(40, state.advanceBy(600_000L).value)
    }

    @Test
    fun changingSidesOrReturningFromPortraitRestartsAtOne() {
        val left = Stage5CounterState().withOrientation(270).advanceBy(20_000L)
        assertEquals(1, left.withOrientation(90).value)
        val portrait = left.withOrientation(0).advanceBy(10_000L)
        assertNull(portrait.value)
        assertEquals(1, portrait.withOrientation(270).value)
        assertNull(left.withOrientation(180).value)
    }

    @Test
    fun sensorJitterAndUnknownReadingsDoNotRestartCounting() {
        val left = Stage5CounterState().withOrientation(270).advanceBy(12_500L)
        for (degrees in listOf(269, 271, 230, 310, -1)) {
            assertEquals(left, left.withOrientation(degrees))
        }
        val right = Stage5CounterState().withOrientation(90).advanceBy(12_500L)
        for (degrees in listOf(89, 91, 50, 130, -1)) {
            assertEquals(right, right.withOrientation(degrees))
        }
        assertNull(Stage5CounterState().withOrientation(230).value)
        assertNull(Stage5CounterState().withOrientation(50).value)
    }

    @Test
    fun restoredStatePreservesPartialSeconds() {
        val original = Stage5CounterState().withOrientation(270).advanceBy(12_500L)
        val restored = Stage5CounterState(original.side, original.elapsedMillis)
        assertEquals(13, restored.withOrientation(270).value)
        assertEquals(14, restored.advanceBy(500L).value)
    }

    @Test
    fun answerComparisonIgnoresCaseWhitespaceAndPunctuation() {
        assertEquals(normalizeAnswer("Ezequiel"), normalizeAnswer("  EZEQUIEL.  "))
    }
}
