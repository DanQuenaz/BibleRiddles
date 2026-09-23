package com.quenazapps.bibleriddles.service

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.quenazapps.bibleriddles.domain.PlayerInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class StageOrderStorageTest {
    @Test fun migrationIsPersistentAndRunsOnlyOnceIncludingLegacyHints() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("stage_order_migration_test", Context.MODE_PRIVATE)
        val today = Calendar.getInstance().run { get(Calendar.YEAR) * 1000 + get(Calendar.DAY_OF_YEAR) }
        try {
            preferences.edit().clear()
                .putInt("last_played_stage", 2).putInt("highest_unlocked_stage", 2)
                .putInt("stage_score_1", 3).putInt("stage_tip_usage_1", 1)
                .putStringSet("purchased_stage_tips_2", setOf("tip_2"))
                .putInt("tip_points_spent_today", 3).putInt("tips_usage_day", today).commit()
            val migrated = LocalStorage(preferences).getPlayerInfo()
            assertEquals(3, migrated.lastPlayedStage)
            assertEquals(3, migrated.highestUnlockedStage)
            assertEquals(mapOf(2 to 3), migrated.stageScores)
            assertEquals(mapOf(2 to setOf("tip_1"), 3 to setOf("tip_2")), migrated.purchasedStageTips)
            assertEquals(1, migrated.tipPointsRemainingToday)
            assertEquals(2, preferences.getInt("stage_order_version", 0))
            assertEquals(migrated, LocalStorage(preferences).getPlayerInfo())
        } finally {
            preferences.edit().clear().commit()
        }
    }

    @Test fun newProgressIsNeverShifted() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("stage_order_new_player_test", Context.MODE_PRIVATE)
        try {
            preferences.edit().clear().commit()
            val storage = LocalStorage(preferences)
            assertEquals(1, storage.getPlayerInfo().stageToContinue)
            val player = PlayerInfo().withStageScore(1, 3)
            storage.savePlayerInfo(player)
            assertEquals(mapOf(1 to 3), LocalStorage(preferences).getPlayerInfo().stageScores)
        } finally {
            preferences.edit().clear().commit()
        }
    }
}
