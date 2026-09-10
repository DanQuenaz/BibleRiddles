package com.quenazapps.bibleriddles.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ContinueProgressTest {
    @Test fun newPlayerStartsAtStageOne() {
        assertEquals(1, PlayerInfo().stageToContinue)
    }

    @Test fun unfinishedStageIsResumed() {
        val player = PlayerInfo().withStageScore(1, 3).withStageStarted(2)
        assertEquals(2, player.stageToContinue)
    }

    @Test fun closingAfterCorrectAnswerContinuesAtNextUnlockedStage() {
        assertEquals(2, PlayerInfo().withStageScore(1, 3).stageToContinue)
    }

    @Test fun reviewingOlderStageDoesNotLoseCurrentProgress() {
        val player = PlayerInfo().withStageScore(1, 3).withStageScore(2, 3).withStageStarted(1)
        assertEquals(3, player.stageToContinue)
    }

    @Test fun finalStageNeverAdvancesBeyondFifty() {
        val player = PlayerInfo(highestUnlockedStage = 50).withStageScore(50, 3)
        assertEquals(50, player.stageToContinue)
    }
}
