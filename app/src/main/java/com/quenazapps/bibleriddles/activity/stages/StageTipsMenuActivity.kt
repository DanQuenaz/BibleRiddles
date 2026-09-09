package com.quenazapps.bibleriddles.activity.stages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage

/** A full-screen menu; each independent stage supplies its own tip definitions. */
class StageTipsMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stageNumber = intent.getIntExtra(EXTRA_STAGE_NUMBER, 0)
        val definitions = intent.getBundleExtra(EXTRA_TIPS)
        if (stageNumber !in 1..PlayerInfo.TOTAL_STAGES || definitions == null) {
            finish()
            return
        }

        // Primitive Bundle values preserve text/image definitions across Activity recreation.
        val tips = List(definitions.getInt(KEY_COUNT)) { index ->
            val definition = requireNotNull(definitions.getBundle(index.toString()))
            val id = requireNotNull(definition.getString(KEY_ID))
            val cost = definition.getInt(KEY_COST)
            if (definition.containsKey(KEY_TEXT)) {
                StageTip.TextTip(id, cost, definition.getInt(KEY_TEXT))
            } else {
                StageTip.ImageTip(id, cost, definition.getInt(KEY_IMAGE))
            }
        }
        val localStorage = LocalStorage(this)
        enableEdgeToEdge()
        setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                StageTipsMenu(
                    stageNumber = stageNumber,
                    tips = tips,
                    localStorage = localStorage,
                    onOpenTip = { number, tip ->
                        startActivity(TipActivity.createIntent(this, stageNumber, number, tip))
                    },
                    onClose = ::finish,
                )
            }
        }
    }

    companion object {
        private const val EXTRA_STAGE_NUMBER = "stage_number"
        private const val EXTRA_TIPS = "tips"
        private const val KEY_COUNT = "count"
        private const val KEY_ID = "id"
        private const val KEY_COST = "cost"
        private const val KEY_TEXT = "text_res"
        private const val KEY_IMAGE = "image_res"

        fun createIntent(context: Context, stageNumber: Int, tips: List<StageTip>): Intent {
            require(stageNumber in 1..PlayerInfo.TOTAL_STAGES)
            val sortedTips = orderedTips(tips)
            return Intent(context, StageTipsMenuActivity::class.java).apply {
                putExtra(EXTRA_STAGE_NUMBER, stageNumber)
                putExtra(EXTRA_TIPS, Bundle().apply {
                    putInt(KEY_COUNT, sortedTips.size)
                    sortedTips.forEachIndexed { index, tip ->
                        putBundle(index.toString(), Bundle().apply {
                            putString(KEY_ID, tip.id)
                            putInt(KEY_COST, tip.cost)
                            when (tip) {
                                is StageTip.TextTip -> putInt(KEY_TEXT, tip.textRes)
                                is StageTip.ImageTip -> putInt(KEY_IMAGE, tip.imageRes)
                            }
                        })
                    }
                })
            }
        }
    }
}
