package com.quenazapps.bibleriddles.domain

/**
 * The small amount of player progress needed by the game between launches.
 *
 * A [lastPlayedStage] of zero means that the player has not started the game yet.
 */
data class PlayerInfo(
    val lastPlayedStage: Int = 0,
    val highestUnlockedStage: Int = 1,
    val stageScores: Map<Int, Int> = emptyMap(),
    val purchasedStageTips: Map<Int, Set<String>> = emptyMap(),
    val tipPointsSpentToday: Int = 0,
    val tipsUsageDayKey: Int = 0,
) {
    val hasPlayedAnyStage: Boolean
        get() = lastPlayedStage > 0 || stageScores.isNotEmpty()

    fun scoreForStage(stageNumber: Int): Int =
        stageScores[stageNumber]?.coerceIn(0, MAX_STAGE_SCORE) ?: 0

    fun isStageUnlocked(stageNumber: Int): Boolean =
        stageNumber in 1..highestUnlockedStage.coerceIn(1, TOTAL_STAGES)

    val tipPointsRemainingToday: Int
        get() = (DAILY_TIP_POINTS - tipPointsSpentToday).coerceIn(0, DAILY_TIP_POINTS)

    fun tipsUsedForStage(stageNumber: Int): Int =
        purchasedStageTips[stageNumber]?.size ?: 0

    fun ownsTip(stageNumber: Int, tipId: String): Boolean =
        tipId in purchasedStageTips[stageNumber].orEmpty()

    fun withStageStarted(stageNumber: Int): PlayerInfo = copy(
        lastPlayedStage = stageNumber.coerceIn(1, TOTAL_STAGES),
    )

    fun withStageScore(stageNumber: Int, score: Int): PlayerInfo {
        val safeStageNumber = stageNumber.coerceIn(1, TOTAL_STAGES)
        val safeScore = maxOf(
            scoreForStage(safeStageNumber),
            score.coerceIn(0, MAX_STAGE_SCORE),
        )
        val updatedScores = stageScores.toMutableMap().apply {
            if (safeScore == 0) remove(safeStageNumber) else put(safeStageNumber, safeScore)
        }
        val unlockedStage = if (safeScore > 0) {
            maxOf(highestUnlockedStage, (safeStageNumber + 1).coerceAtMost(TOTAL_STAGES))
        } else {
            highestUnlockedStage
        }

        return copy(
            lastPlayedStage = safeStageNumber,
            highestUnlockedStage = unlockedStage.coerceIn(1, TOTAL_STAGES),
            stageScores = updatedScores,
        )
    }

    /** IDs are stable within a stage, regardless of tip price or display order. */
    fun withTipPurchased(
        stageNumber: Int,
        tipId: String,
        cost: Int,
        currentDayKey: Int,
    ): PlayerInfo? {
        require(stageNumber in 1..TOTAL_STAGES)
        require(tipId.isNotBlank())
        require(cost in 1..DAILY_TIP_POINTS)
        val refreshed = refreshedForDay(currentDayKey)
        if (refreshed.ownsTip(stageNumber, tipId)) return refreshed
        if (!isStageUnlocked(stageNumber) || cost > refreshed.tipPointsRemainingToday) return null
        return refreshed.copy(
            tipPointsSpentToday = refreshed.tipPointsSpentToday + cost,
            purchasedStageTips = refreshed.purchasedStageTips +
                (stageNumber to (refreshed.purchasedStageTips[stageNumber].orEmpty() + tipId)),
        )
    }

    fun refreshedForDay(currentDayKey: Int): PlayerInfo =
        if (tipsUsageDayKey == currentDayKey) {
            this
        } else {
            copy(tipPointsSpentToday = 0, tipsUsageDayKey = currentDayKey)
        }

    companion object {
        const val TOTAL_STAGES = 50
        const val MAX_STAGE_SCORE = 3
        const val DAILY_TIP_POINTS = 4
    }
}
