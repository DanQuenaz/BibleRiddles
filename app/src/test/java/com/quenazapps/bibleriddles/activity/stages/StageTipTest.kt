package com.quenazapps.bibleriddles.activity.stages

import org.junit.Assert.assertEquals
import org.junit.Test

class StageTipTest {
    @Test
    fun tipsSortByPriceKeepingStableIdsAndEqualPriceOrder() {
        val tips = listOf(
            StageTip.TextTip("detailed", 3, 1),
            StageTip.ImageTip("picture", 1, 2),
            StageTip.TextTip("reference", 1, 3),
        )
        assertEquals(listOf("picture", "reference", "detailed"), orderedTips(tips).map { it.id })
        assertEquals(listOf(1, 1, 3), orderedTips(tips).map { it.cost })
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateIdsAreRejected() {
        orderedTips(listOf(StageTip.TextTip("same", 1, 1), StageTip.ImageTip("same", 2, 2)))
    }
}
