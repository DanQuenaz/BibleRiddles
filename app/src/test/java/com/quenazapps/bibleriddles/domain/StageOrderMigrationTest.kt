package com.quenazapps.bibleriddles.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StageOrderMigrationTest {
    @Test fun keepsNewPlayersAtTheNewFirstStage() {
        assertEquals(PlayerInfo(), PlayerInfo().withInsertedFirstStage())
    }

    @Test fun shiftsScoresHintsAndContinueTogetherWithoutChangingDailyBalance() {
        val old = PlayerInfo(
            lastPlayedStage = 3, highestUnlockedStage = 3,
            stageScores = mapOf(1 to 3, 2 to 2),
            purchasedStageTips = mapOf(2 to setOf("tip_1"), 3 to setOf("tip_2")),
            tipPointsSpentToday = 3, tipsUsageDayKey = 2026266,
        )
        val shifted = old.withInsertedFirstStage()
        assertEquals(4, shifted.stageToContinue)
        assertEquals(4, shifted.highestUnlockedStage)
        assertEquals(mapOf(2 to 3, 3 to 2), shifted.stageScores)
        assertEquals(mapOf(3 to setOf("tip_1"), 4 to setOf("tip_2")), shifted.purchasedStageTips)
        assertEquals(0, shifted.scoreForStage(1))
        assertEquals(0, shifted.tipsUsedForStage(1))
        assertEquals(old.tipPointsRemainingToday, shifted.tipPointsRemainingToday)
        assertEquals(old.tipsUsageDayKey, shifted.tipsUsageDayKey)
    }

    @Test fun preservesTheFormerLastStageAtFiftyOne() {
        val old = PlayerInfo(lastPlayedStage = 50, highestUnlockedStage = 50, stageScores = mapOf(50 to 3))
        val shifted = old.withInsertedFirstStage()
        assertEquals(51, shifted.stageToContinue)
        assertEquals(3, shifted.scoreForStage(51))
    }
}
