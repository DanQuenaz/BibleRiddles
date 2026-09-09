package com.quenazapps.bibleriddles.activity.stages

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quenazapps.bibleriddles.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Stage3RiddleTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun swipesFollowCreationSequenceAndStopAtBothEnds() {
        assertEquals(listOf(
            R.mipmap.creation_1, R.mipmap.creation_2, R.mipmap.creation_3,
            R.mipmap.creation_5, R.mipmap.creation_6, R.mipmap.creation_7,
        ), STAGE3_IMAGES)
        lateinit var pager: PagerState
        compose.setContent {
            pager = rememberPagerState { STAGE3_IMAGES.size }
            Stage3Riddle(modifier = Modifier.testTag("riddle"), pagerState = pager)
        }
        fun assertPage(expected: Int) = compose.runOnIdle {
            assertEquals(expected, pager.currentPage)
        }
        assertPage(0)
        compose.onNodeWithTag("riddle").performTouchInput { swipeRight() }
        assertPage(0)
        for (page in 1..5) {
            compose.onNodeWithTag("riddle").performTouchInput { swipeLeft() }
            assertPage(page)
        }
        compose.onNodeWithTag("riddle").performTouchInput { swipeLeft() }
        assertPage(5)
        for (page in 4 downTo 0) {
            compose.onNodeWithTag("riddle").performTouchInput { swipeRight() }
            assertPage(page)
        }
    }
}
