package com.quenazapps.bibleriddles.service

import android.content.Context
import androidx.core.content.edit
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.domain.PlayerInfo.Companion.MAX_STAGE_SCORE
import com.quenazapps.bibleriddles.domain.PlayerInfo.Companion.TOTAL_STAGES
import java.util.Calendar

/** Persists the player's progress in Android SharedPreferences. */
class LocalStorage(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun getPlayerInfo(): PlayerInfo {
        val scores = (1..TOTAL_STAGES).mapNotNull { stageNumber ->
            val score = preferences.getInt(stageScoreKey(stageNumber), 0)
                .coerceIn(0, MAX_STAGE_SCORE)
            if (score > 0) stageNumber to score else null
        }.toMap()
        val purchasedTips = (1..TOTAL_STAGES).mapNotNull { stageNumber ->
            // Old tips were sequential: preserve access to tip_1 and tip_2.
            val ids = if (preferences.contains(purchasedTipsKey(stageNumber))) {
                preferences.getStringSet(purchasedTipsKey(stageNumber), emptySet()).orEmpty().toSet()
            } else {
                val legacyCount = preferences.getInt(stageTipUsageKey(stageNumber), 0).coerceIn(0, 2)
                (1..legacyCount).map { "tip_$it" }.toSet()
            }
            if (ids.isEmpty()) null else stageNumber to ids
        }.toMap()

        return PlayerInfo(
            lastPlayedStage = preferences.getInt(KEY_LAST_PLAYED_STAGE, 0)
                .coerceIn(0, TOTAL_STAGES),
            highestUnlockedStage = preferences.getInt(KEY_HIGHEST_UNLOCKED_STAGE, 1)
                .coerceIn(1, TOTAL_STAGES),
            stageScores = scores,
            purchasedStageTips = purchasedTips,
            tipPointsSpentToday = preferences.getInt(
                KEY_TIP_POINTS_SPENT,
                preferences.getInt(KEY_TIPS_USED_TODAY, 0),
            ).coerceIn(0, PlayerInfo.DAILY_TIP_POINTS),
            tipsUsageDayKey = preferences.getInt(KEY_TIPS_USAGE_DAY, 0),
        ).refreshedForDay(currentDayKey())
    }

    fun savePlayerInfo(playerInfo: PlayerInfo) {
        preferences.edit {
            putInt(
                KEY_LAST_PLAYED_STAGE,
                playerInfo.lastPlayedStage.coerceIn(0, TOTAL_STAGES),
            )
            putInt(
                KEY_HIGHEST_UNLOCKED_STAGE,
                playerInfo.highestUnlockedStage.coerceIn(1, TOTAL_STAGES),
            )
            putInt(KEY_TIP_POINTS_SPENT, playerInfo.tipPointsSpentToday)
            putInt(KEY_TIPS_USAGE_DAY, playerInfo.tipsUsageDayKey)
            (1..TOTAL_STAGES).forEach { stageNumber ->
                putInt(
                    stageScoreKey(stageNumber),
                    playerInfo.scoreForStage(stageNumber),
                )
                putStringSet(
                    purchasedTipsKey(stageNumber),
                    playerInfo.purchasedStageTips[stageNumber].orEmpty().toSet(),
                )
            }
        }
    }

    /** A single read/check/write transaction, including repeat purchases. */
    fun purchaseTip(stageNumber: Int, tipId: String, cost: Int): PlayerInfo? =
        synchronized(PURCHASE_LOCK) {
            val updatedPlayer = getPlayerInfo()
                .withTipPurchased(stageNumber, tipId, cost, currentDayKey())
                ?: return@synchronized null
            savePlayerInfo(updatedPlayer)
            updatedPlayer
        }

    private fun stageScoreKey(stageNumber: Int) = "$KEY_STAGE_SCORE_PREFIX$stageNumber"

    private fun stageTipUsageKey(stageNumber: Int) =
        "$KEY_STAGE_TIP_USAGE_PREFIX$stageNumber"

    private fun purchasedTipsKey(stageNumber: Int) = "purchased_stage_tips_$stageNumber"

    private fun currentDayKey(): Int = Calendar.getInstance().run {
        get(Calendar.YEAR) * 1_000 + get(Calendar.DAY_OF_YEAR)
    }

    private companion object {
        val PURCHASE_LOCK = Any()
        const val PREFERENCES_NAME = "player_progress"
        const val KEY_LAST_PLAYED_STAGE = "last_played_stage"
        const val KEY_HIGHEST_UNLOCKED_STAGE = "highest_unlocked_stage"
        const val KEY_STAGE_SCORE_PREFIX = "stage_score_"
        const val KEY_STAGE_TIP_USAGE_PREFIX = "stage_tip_usage_"
        const val KEY_TIPS_USED_TODAY = "tips_used_today"
        const val KEY_TIP_POINTS_SPENT = "tip_points_spent_today"
        const val KEY_TIPS_USAGE_DAY = "tips_usage_day"
    }
}
