package com.quenazapps.bibleriddles.activity.stages

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.R
import java.text.Normalizer
import java.util.Locale

/** Shared visual shell. Each stage supplies its own independent content and behavior. */
@Composable
internal fun StageLayout(
    stageNumber: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    tipPointsRemainingToday: Int? = null,
    tipsUsedForStage: Int = 0,
    onTipClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
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
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding(),
        ) {
            StageTopMenu(
                title = stringResource(R.string.stage_title, stageNumber),
                onBackClick = onBackClick,
                tipPointsRemainingToday = tipPointsRemainingToday,
                tipsUsedForStage = tipsUsedForStage,
                onTipClick = onTipClick,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                content = content,
            )
        }
    }
}

@Composable
internal fun StageTextRiddle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color(0xFF4A2A12),
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 36.sp,
        textAlign = TextAlign.Center,
        style = TextStyle(
            shadow = Shadow(color = Color(0x558A5A28), blurRadius = 2f),
        ),
        modifier = modifier,
    )
}

@Composable
internal fun StageAnswerForm(
    answer: String,
    feedbackMessage: String?,
    answerIsCorrect: Boolean,
    onAnswerChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    compact: Boolean = false,
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = answer,
        onValueChange = onAnswerChange,
        placeholder = { Text(stringResource(R.string.answer_placeholder)) },
        minLines = if (compact) 1 else 3,
        maxLines = if (compact) 2 else 5,
        enabled = !answerIsCorrect,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onSubmitClick()
            },
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFDF0CB),
            unfocusedContainerColor = Color(0xFFFDF0CB),
            disabledContainerColor = Color(0xFFFDF0CB),
            focusedBorderColor = Color(0xFF8B5A27),
            unfocusedBorderColor = Color(0xFF6B431E),
            focusedTextColor = Color(0xFF3B2412),
            unfocusedTextColor = Color(0xFF3B2412),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 72.dp else 132.dp),
    )

    Spacer(modifier = Modifier.height(12.dp))

    StageImageButton(
        text = stringResource(R.string.submit_answer),
        enabled = !answerIsCorrect,
        onClick = {
            focusManager.clearFocus()
            onSubmitClick()
        },
    )

    feedbackMessage?.let { message ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = if (answerIsCorrect) Color(0xFF28620F) else Color(0xFF8B1E13),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun ColumnScope.CompletedStageReview(
    correctAnswer: String,
    riddleContent: @Composable BoxScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        content = riddleContent,
    )

    Text(
        text = stringResource(R.string.correct_answer_label),
        color = Color(0xFF7A4B20),
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = correctAnswer,
        color = Color(0xFF28620F),
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 31.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x99FDF0CB))
            .padding(18.dp),
    )
}

@Composable
internal fun StageTopMenu(
    title: String,
    onBackClick: () -> Unit,
    tipPointsRemainingToday: Int? = null,
    tipsUsedForStage: Int = 0,
    onTipClick: (() -> Unit)? = null,
    @DrawableRes navigationIconRes: Int = R.mipmap.back_button,
    @StringRes navigationDescriptionRes: Int = R.string.back,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(STAGE_TOP_MENU_HEIGHT),
    ) {
        Image(
            painter = painterResource(R.mipmap.top_menu_stage_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Image(
            painter = painterResource(navigationIconRes),
            contentDescription = stringResource(navigationDescriptionRes),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 11.dp, bottom = 8.dp)
                .size(56.dp)
                .clickable(role = Role.Button, onClick = onBackClick),
        )

        Text(
            text = title,
            color = Color(0xFFF2D39A),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 23.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = Shadow(color = Color.Black, blurRadius = 4f)),
        )

        if (tipPointsRemainingToday != null && onTipClick != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 5.dp)
                    .size(58.dp)
                    .clickable(role = Role.Button, onClick = onTipClick),
            ) {
                Image(
                    painter = painterResource(R.mipmap.oil_lamp),
                    contentDescription = stringResource(
                        R.string.tips_button_description,
                        tipPointsRemainingToday,
                    ),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                Text(
                    text = tipPointsRemainingToday.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                        .size(21.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD3A1E0A))
                        .padding(top = 2.dp),
                )
                if (tipsUsedForStage > 0) {
                    Text(
                        text = stringResource(R.string.tips_owned_count, tipsUsedForStage),
                        color = Color(0xFFF2D39A),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
        }
    }
}

@Composable
internal fun StageBackground() {
    Image(
        painter = painterResource(R.mipmap.stage_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun StageImageButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 190.dp, height = 64.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.mipmap.button),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = text,
            color = Color(0xFFF2D39A),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = Shadow(color = Color.Black, blurRadius = 3f)),
        )
    }
}

internal fun normalizeAnswer(value: String): String = Normalizer
    .normalize(value, Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "")
    .lowercase(Locale.ROOT)
    .replace("[.,!?;:]".toRegex(), " ")
    .replace("\\s+".toRegex(), " ")
    .trim()

internal val STAGE_TOP_MENU_HEIGHT = 72.dp
