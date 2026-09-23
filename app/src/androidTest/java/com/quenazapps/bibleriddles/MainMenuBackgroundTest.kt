package com.quenazapps.bibleriddles

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import pl.droidsonroids.gif.GifDrawable

@RunWith(AndroidJUnit4::class)
class MainMenuBackgroundTest {
    @Test
    fun backgroundKeepsAnimatingAcrossLoopsAndReturningToMenu() {
        ActivityScenario.launch(MainMenuActivity::class.java).use { scenario ->
            lateinit var gif: GifDrawable
            scenario.onActivity { activity ->
                val background = findGif(activity.window.decorView)
                assertNotNull("Menu must display the GIF", background)
                gif = background!!
                assertEquals("GIF must repeat forever", 0, gif.loopCount)
                assertTrue("GIF must have multiple frames", gif.numberOfFrames > 1)
                assertTrue(gif.isRunning)
            }

            // Observe real decoding through two complete loops, not just the loop setting.
            val deadline = System.currentTimeMillis() + gif.duration * 3L + 10_000L
            var completedLoops = 0
            var previousFrame = gif.currentFrameIndex
            while (completedLoops < 2 && System.currentTimeMillis() < deadline) {
                Thread.sleep(20)
                val currentFrame = gif.currentFrameIndex
                if (currentFrame < previousFrame) completedLoops++
                previousFrame = currentFrame
            }
            assertTrue("GIF must complete at least two loops", completedLoops >= 2)
            assertTrue("GIF must still be running after looping", gif.isRunning)

            scenario.moveToState(Lifecycle.State.CREATED)
            assertFalse("Hidden menu must stop animating", gif.isRunning)
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertTrue("Returning to the menu must resume animation", gif.isRunning)

            val firstFrame = gif.currentFrameIndex
            val frameDeadline = System.currentTimeMillis() + 3_000L
            while (gif.currentFrameIndex == firstFrame && System.currentTimeMillis() < frameDeadline) {
                Thread.sleep(50)
            }
            assertTrue("Frames must advance after returning", gif.currentFrameIndex != firstFrame)
        }
    }

    private fun findGif(view: View): GifDrawable? {
        if (view is ImageView && view.drawable is GifDrawable) return view.drawable as GifDrawable
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findGif(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }
}
