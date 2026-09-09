package com.quenazapps.bibleriddles.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerInfoTest {
    @Test
    fun newPlayerHasNotPlayedAStage() {
        assertFalse(PlayerInfo().hasPlayedAnyStage)
    }

    @Test
    fun playerWithLastPlayedStageHasProgress() {
        assertTrue(PlayerInfo(lastPlayedStage = 1).hasPlayedAnyStage)
    }

    @Test
    fun stageScoreIsStoredAndUnlocksNextStage() {
        val playerInfo = PlayerInfo().withStageScore(stageNumber = 1, score = 2)

        assertEquals(2, playerInfo.scoreForStage(1))
        assertTrue(playerInfo.isStageUnlocked(2))
        assertFalse(playerInfo.isStageUnlocked(3))
    }

    @Test
    fun scoreIsLimitedToThreeStars() {
        val playerInfo = PlayerInfo().withStageScore(stageNumber = 1, score = 10)

        assertEquals(3, playerInfo.scoreForStage(1))
    }

    @Test
    fun purchasesSpendTheirPricesAcrossStagesAndCannotOverdraw() {
        val dayKey = 2_026_100
        val first = requireNotNull(PlayerInfo(highestUnlockedStage = 2)
            .withTipPurchased(1, "expensive", 3, dayKey))
        assertEquals(1, first.tipPointsRemainingToday)
        assertTrue(first.withTipPurchased(2, "too_expensive", 2, dayKey) == null)
        val second = requireNotNull(first.withTipPurchased(2, "cheap", 1, dayKey))
        assertEquals(0, second.tipPointsRemainingToday)
        assertTrue(second.withTipPurchased(1, "another", 1, dayKey) == null)
        assertEquals(1, second.tipsUsedForStage(1))
        assertEquals(1, second.tipsUsedForStage(2))
    }

    @Test
    fun dailyTipsResetOnANewDay() {
        val playerInfo = requireNotNull(
            PlayerInfo().withTipPurchased(1, "tip_2", 2, 1),
        ).refreshedForDay(currentDayKey = 2)

        assertEquals(4, playerInfo.tipPointsRemainingToday)
        assertTrue(playerInfo.ownsTip(1, "tip_2"))
    }

    @Test
    fun completingStagePreservesPurchasedTips() {
        val playerInfo = requireNotNull(
            PlayerInfo().withTipPurchased(1, "tip_1", 1, 1),
        ).withStageScore(stageNumber = 1, score = 2)

        assertEquals(1, playerInfo.tipsUsedForStage(1))
        assertTrue(playerInfo.ownsTip(1, "tip_1"))
    }

    @Test
    fun boughtTipReopensForFreeEvenWithNoBalanceOrAfterDayChanges() {
        val spent = requireNotNull(PlayerInfo().withTipPurchased(1, "tip", 4, 1))
        assertEquals(spent, spent.withTipPurchased(1, "tip", 4, 1))
        val tomorrow = requireNotNull(spent.withTipPurchased(1, "tip", 4, 2))
        assertEquals(4, tomorrow.tipPointsRemainingToday)
        assertEquals(1, tomorrow.tipsUsedForStage(1))
    }

    @Test
    fun stageCanOwnMoreThanTwoTipsAndPurchaseThemOutOfOrder() {
        var player = PlayerInfo()
        for (id in listOf("tip_4", "tip_2", "tip_3", "tip_1")) {
            player = requireNotNull(player.withTipPurchased(1, id, 1, 1))
        }
        assertEquals(4, player.tipsUsedForStage(1))
        assertEquals(0, player.tipPointsRemainingToday)
        assertEquals(4, player.refreshedForDay(2).tipsUsedForStage(1))
    }

    @Test
    fun lockedStageCannotSpendPoints() {
        assertTrue(PlayerInfo().withTipPurchased(2, "tip", 1, 1) == null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativePriceIsRejected() {
        PlayerInfo().withTipPurchased(1, "tip", -1, 1)
    }
}
