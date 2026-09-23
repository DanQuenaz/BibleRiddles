package com.quenazapps.bibleriddles.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.ui.theme.BibleRiddlesTheme

class TutorialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                TutorialScreen(onBackClick = ::finish)
            }
        }
    }
}

@Composable
private fun TutorialScreen(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = painterResource(R.mipmap.stage_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.mipmap.top_menu_stage_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
                Text(
                    text = stringResource(R.string.tutorial_title),
                    color = TutorialGold,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .heightIn(min = 72.dp)
                        .padding(horizontal = 76.dp, vertical = 20.dp)
                        .semantics { heading() },
                )
                Image(
                    painter = painterResource(R.mipmap.back_button),
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 11.dp)
                        .size(56.dp)
                        .clickable(role = Role.Button, onClick = onBackClick),
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                itemsIndexed(TutorialSteps) { index, textResource ->
                    Surface(
                        color = Color(0xF21E160F),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF79603B)),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF503A20), CircleShape),
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    color = TutorialGold,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                )
                            }
                            TutorialText(
                                text = stringResource(textResource),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                item {
                    Surface(
                        color = Color(0xF21E160F),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        TutorialText(
                            text = stringResource(R.string.tutorial_closing),
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                        )
                    }
                }
                item {
                    Surface(
                        color = Color(0xFF244B40),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TutorialGold),
                    ) {
                        Text(
                            text = stringResource(R.string.tutorial_final_word),
                            color = TutorialGold,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = AnnotatedString.fromHtml(text),
        color = TutorialGold,
        fontFamily = FontFamily.Serif,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        modifier = modifier,
    )
}

private val TutorialGold = Color(0xFFF2D39A)
private val TutorialSteps = listOf(
    R.string.tutorial_step_1,
    R.string.tutorial_step_2,
    R.string.tutorial_step_3,
    R.string.tutorial_step_4,
    R.string.tutorial_step_5,
    R.string.tutorial_step_6,
    R.string.tutorial_step_7,
    R.string.tutorial_step_8,
)

@Preview(showBackground = true, widthDp = 412, heightDp = 800)
@Composable
private fun TutorialPreview() {
    BibleRiddlesTheme(dynamicColor = false) {
        TutorialScreen(onBackClick = {})
    }
}
