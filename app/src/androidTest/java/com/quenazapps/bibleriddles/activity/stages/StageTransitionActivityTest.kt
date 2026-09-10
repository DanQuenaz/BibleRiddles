package com.quenazapps.bibleriddles.activity.stages

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quenazapps.bibleriddles.service.LocalStorage
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StageTransitionActivityTest {
    @Test
    fun fadeAnimationOpensRequestedStageWithoutCrashing() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val localStorage = LocalStorage(context)
        val originalPlayer = localStorage.getPlayerInfo()
        val stageNumber = originalPlayer.stageToContinue
        val destinationClass = stageActivityClass(stageNumber)
        val monitor = instrumentation.addMonitor(destinationClass.name, null, false)
        try {
            ActivityScenario.launch<StageTransitionActivity>(
                StageTransitionActivity.createIntent(context, stageNumber),
            ).use {
                val destination = instrumentation.waitForMonitorWithTimeout(monitor, 10_000L)
                assertNotNull("The fade must finish and open the requested stage", destination)
                instrumentation.runOnMainSync {
                    assertEquals(destinationClass, destination.javaClass)
                    destination.finish()
                }
            }
        } finally {
            instrumentation.removeMonitor(monitor)
            // The transition updates lastPlayedStage; leave the player's saved progress unchanged.
            localStorage.savePlayerInfo(originalPlayer)
        }
    }
}
