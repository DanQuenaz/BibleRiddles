package com.quenazapps.bibleriddles.activity.stages

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed interface StageTip {
    val id: String
    val cost: Int

    data class TextTip(
        override val id: String,
        override val cost: Int,
        @param:StringRes val textRes: Int,
    ) : StageTip {
        init { validateTip(id, cost) }
    }

    data class ImageTip(
        override val id: String,
        override val cost: Int,
        @param:DrawableRes val imageRes: Int,
    ) : StageTip {
        init { validateTip(id, cost) }
    }
}

private fun validateTip(id: String, cost: Int) {
    require(id.isNotBlank())
    require(cost in 1..com.quenazapps.bibleriddles.domain.PlayerInfo.DAILY_TIP_POINTS)
}

internal fun orderedTips(tips: List<StageTip>): List<StageTip> {
    require(tips.map { it.id }.distinct().size == tips.size) { "Tip IDs must be unique per stage" }
    return tips.sortedBy { it.cost }
}
