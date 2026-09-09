package com.quenazapps.bibleriddles.activity.stages

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme

/** Temporary renderer called by independent stages that do not have gameplay yet. */
internal fun ComponentActivity.showPlaceholderStage(stageNumber: Int) {
    enableEdgeToEdge()
    setContent {
        BibleRiddlesTheme(dynamicColor = false) {
            StageLayout(stageNumber = stageNumber, onBackClick = ::finish) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = getString(R.string.stage_coming_soon),
                        color = Color(0xFF4A2A12),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    )
                }
            }
        }
    }
}
