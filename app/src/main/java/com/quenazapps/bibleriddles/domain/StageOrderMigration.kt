package com.quenazapps.bibleriddles.domain

/** Called once for progress saved before the instructions riddle was inserted. */
internal fun PlayerInfo.withInsertedFirstStage(): PlayerInfo {
    val hasOldProgress = hasPlayedAnyStage || highestUnlockedStage > 1 || purchasedStageTips.isNotEmpty()
    return copy(
        lastPlayedStage = if (lastPlayedStage > 0) (lastPlayedStage + 1).coerceAtMost(PlayerInfo.TOTAL_STAGES) else 0,
        highestUnlockedStage = if (hasOldProgress) {
            (highestUnlockedStage + 1).coerceAtMost(PlayerInfo.TOTAL_STAGES)
        } else {
            1
        },
        stageScores = stageScores.filterKeys { it in 1 until PlayerInfo.TOTAL_STAGES }
            .mapKeys { (stage, _) -> stage + 1 },
        purchasedStageTips = purchasedStageTips.filterKeys { it in 1 until PlayerInfo.TOTAL_STAGES }
            .mapKeys { (stage, _) -> stage + 1 },
    )
}
