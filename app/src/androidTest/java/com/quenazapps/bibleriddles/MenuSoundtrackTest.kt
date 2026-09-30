package com.quenazapps.bibleriddles

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.quenazapps.bibleriddles.activity.StagesListActivity
import com.quenazapps.bibleriddles.activity.TutorialActivity
import com.quenazapps.bibleriddles.activity.stages.Stage8Activity
import com.quenazapps.bibleriddles.activity.stages.StageTransitionActivity
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuSoundtrackTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val soundtrack get() =
        (instrumentation.targetContext.applicationContext as BibleRiddlesApplication).menuSoundtrack

    private fun awaitOnMain(condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        do {
            var ready = false
            instrumentation.runOnMainSync { ready = condition() }
            if (ready) return
            SystemClock.sleep(50)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("Expected soundtrack state did not arrive")
    }

    private fun awaitPlaying() = awaitOnMain {
        runCatching { soundtrack.player?.isPlaying == true }.getOrDefault(false)
    }

    @Test fun soundtrackContinuesAcrossMenusAndStopsForGameplayAndBackground() {
        ActivityScenario.launch(MainMenuActivity::class.java).use { main ->
            awaitPlaying()
            lateinit var original: MediaPlayer
            var initialPosition = 0
            main.onActivity {
                original = requireNotNull(soundtrack.player)
                assertTrue(original.isLooping)
                initialPosition = original.currentPosition
            }

            fun visit(destination: Class<out Activity>, intent: Intent, expectMusic: Boolean) {
                val monitor = instrumentation.addMonitor(destination.name, null, false)
                var opened: Activity? = null
                try {
                    main.onActivity { it.startActivity(intent) }
                    opened = requireNotNull(monitor.waitForActivityWithTimeout(10_000))
                    if (expectMusic) {
                        awaitPlaying()
                        instrumentation.runOnMainSync {
                            assertSame("Menu navigation must retain the playing instance", original, soundtrack.player)
                        }
                    } else {
                        awaitOnMain { soundtrack.player == null }
                    }
                } finally {
                    opened?.let { activity -> instrumentation.runOnMainSync { activity.finish() } }
                    instrumentation.removeMonitor(monitor)
                }
                awaitPlaying()
            }

            for (destination in listOf(StagesListActivity::class.java, TutorialActivity::class.java)) {
                visit(destination, Intent(instrumentation.targetContext, destination), expectMusic = true)
                main.onActivity { assertSame(original, soundtrack.player) }
            }
            awaitOnMain { original.currentPosition > initialPosition + 200 }

            visit(Stage8Activity::class.java,
                Intent(instrumentation.targetContext, Stage8Activity::class.java), expectMusic = false)
            visit(StageTransitionActivity::class.java,
                StageTransitionActivity.createIntent(instrumentation.targetContext, 1), expectMusic = false)

            main.moveToState(Lifecycle.State.CREATED)
            instrumentation.runOnMainSync { assertNull("Background app must be silent", soundtrack.player) }
            main.moveToState(Lifecycle.State.RESUMED)
            awaitPlaying()
        }
        awaitOnMain { soundtrack.player == null }
    }
}
