package com.quenazapps.bibleriddles.activity.stages

import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Stage8GameplayTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    // Use real accessibility actions and real time across Activity transitions.
    private fun awaitNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        fun find(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            node ?: return null
            if (node.isVisibleToUser && predicate(node)) return node
            for (index in 0 until node.childCount) find(node.getChild(index))?.let { return it }
            return null
        }
        val deadline = SystemClock.uptimeMillis() + 10_000
        do {
            find(instrumentation.uiAutomation.rootInActiveWindow)?.let { return it }
            SystemClock.sleep(50)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("Expected accessible control did not appear within 10 seconds")
    }

    private fun text(value: String) = awaitNode { it.text?.toString() == value }
    private fun description(value: String) = awaitNode { it.contentDescription?.toString() == value }
    private fun click(node: AccessibilityNodeInfo) {
        var target = node
        while (!target.isClickable) target = requireNotNull(target.parent)
        assertTrue(target.performAction(AccessibilityNodeInfo.ACTION_CLICK))
    }

    @Test fun tipsOpenAndCloseAndPurchasesUpdateTheStageScore() {
        val storage = LocalStorage(context)
        val originalPlayer = storage.getPlayerInfo()
        val menuMonitor = instrumentation.addMonitor(StageTipsMenuActivity::class.java.name, null, false)
        val tipMonitor = instrumentation.addMonitor(TipActivity::class.java.name, null, false)
        try {
            storage.savePlayerInfo(PlayerInfo(highestUnlockedStage = 8))
            ActivityScenario.launch(Stage8Activity::class.java).use {
                description(context.getString(R.string.stage8_image_description))
                fun openMenu(balance: Int) {
                    click(description(context.getString(R.string.tips_button_description, balance)))
                    text(context.getString(R.string.tips_menu_title))
                    text(context.getString(R.string.tip_points_balance, balance))
                }
                fun closeTip() {
                    click(description(context.getString(R.string.back)))
                    text(context.getString(R.string.tips_menu_title))
                }
                openMenu(4)
                click(description(context.getString(R.string.close)))
                assertEquals(4, storage.getPlayerInfo().tipPointsRemainingToday)
                openMenu(4)
                click(text(context.getString(R.string.tip_buy)))
                text(context.getString(R.string.stage8_tip_1))
                closeTip()
                assertEquals(3, storage.getPlayerInfo().tipPointsRemainingToday)
                click(text(context.getString(R.string.tip_open_free)))
                text(context.getString(R.string.stage8_tip_1))
                closeTip()
                assertEquals(3, storage.getPlayerInfo().tipPointsRemainingToday)
                click(text(context.getString(R.string.tip_buy)))
                text(context.getString(R.string.stage8_tip_2))
                closeTip()
                assertEquals(1, storage.getPlayerInfo().tipPointsRemainingToday)
                click(description(context.getString(R.string.close)))
                description(context.getString(R.string.tips_button_description, 1))
                val field = awaitNode { it.isEditable }
                assertTrue(field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "deuteronomio")
                }))
                click(text(context.getString(R.string.submit_answer)))
                text(context.getString(R.string.next_stage))
                assertEquals(1, storage.getPlayerInfo().scoreForStage(8))
                assertEquals(9, storage.getPlayerInfo().highestUnlockedStage)
            }
        } finally {
            listOf(tipMonitor, menuMonitor).forEach { monitor ->
                monitor.lastActivity?.let { activity -> instrumentation.runOnMainSync { activity.finish() } }
                instrumentation.removeMonitor(monitor)
            }
            storage.savePlayerInfo(originalPlayer)
        }
    }
}
