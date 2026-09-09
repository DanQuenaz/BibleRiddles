package com.quenazapps.bibleriddles.activity.stages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme

class TipActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val stageNumber = intent.getIntExtra(EXTRA_STAGE_NUMBER, 1)
        val tipNumber = intent.getIntExtra(EXTRA_TIP_NUMBER, 1)
        val tipText = intent.getStringExtra(EXTRA_TIP_TEXT)
        val tipImageRes = intent.getIntExtra(EXTRA_TIP_IMAGE_RES, NO_TIP_IMAGE)

        setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                TipScreen(
                    stageNumber = stageNumber,
                    tipNumber = tipNumber,
                    tipText = tipText,
                    tipImageRes = tipImageRes,
                    onBackClick = ::finish,
                )
            }
        }
    }

    companion object {
        private const val EXTRA_STAGE_NUMBER = "stage_number"
        private const val EXTRA_TIP_NUMBER = "tip_number"
        private const val EXTRA_TIP_TEXT = "tip_text"
        private const val EXTRA_TIP_IMAGE_RES = "tip_image_res"

        fun createIntent(
            context: Context,
            stageNumber: Int,
            tipNumber: Int,
            tip: StageTip,
        ): Intent = Intent(context, TipActivity::class.java).apply {
            putExtra(EXTRA_STAGE_NUMBER, stageNumber)
            putExtra(EXTRA_TIP_NUMBER, tipNumber)
            when (tip) {
                is StageTip.TextTip -> putExtra(EXTRA_TIP_TEXT, context.getString(tip.textRes))
                is StageTip.ImageTip -> putExtra(EXTRA_TIP_IMAGE_RES, tip.imageRes)
            }
        }
    }
}

@Composable
private fun TipScreen(
    stageNumber: Int,
    tipNumber: Int,
    tipText: String?,
    @DrawableRes tipImageRes: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        StageBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            StageTopMenu(
                title = stringResource(R.string.tip_title, stageNumber, tipNumber),
                onBackClick = onBackClick,
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            ) {
                if (tipImageRes != NO_TIP_IMAGE) {
                    Image(
                        painter = painterResource(tipImageRes),
                        contentDescription = stringResource(R.string.tip_image_description, tipNumber),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(280.dp),
                    )
                } else {
                    Text(
                        text = tipText.orEmpty(),
                        color = Color(0xFF4A2A12),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        lineHeight = 43.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(color = Color(0x558A5A28), blurRadius = 2f),
                        ),
                    )
                }
            }
        }
    }
}

private const val NO_TIP_IMAGE = 0

@Preview(showBackground = true, widthDp = 412, heightDp = 800)
@Composable
private fun TipPreview() {
    BibleRiddlesTheme(dynamicColor = false) {
        TipScreen(
            stageNumber = 1,
            tipNumber = 1,
            tipText = "Juízes",
            tipImageRes = 0,
            onBackClick = {},
        )
    }
}
