package com.example.mybudgetapp.ui.widgets

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.mybudgetapp.ui.theme.inter

enum class BudgetValueTone {
    Hero,
    Prominent,
    Card,
    Compact,
}

@Composable
fun BudgetValueText(
    text: String,
    modifier: Modifier = Modifier,
    tone: BudgetValueTone = BudgetValueTone.Card,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 2,
    unitLabel: String? = null,
) {
    val valueStyle = adaptiveValueStyle(
        text = text,
        tone = tone,
    )
    val renderedText = buildAnnotatedString {
        append(text)
        if (!unitLabel.isNullOrBlank()) {
            append(" ")
            pushStyle(
                SpanStyle(
                    color = color.copy(alpha = 0.62f),
                    fontFamily = inter,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize = unitFontSize(tone),
                )
            )
            append(unitLabel)
            pop()
        }
    }

    Text(
        text = renderedText,
        modifier = modifier,
        style = valueStyle.copy(color = color),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
    )
}

@Composable
private fun adaptiveValueStyle(
    text: String,
    tone: BudgetValueTone,
): TextStyle {
    val normalizedLength = text.filterNot(Char::isWhitespace).length
    val typography = MaterialTheme.typography
    val baseStyle = when (tone) {
        BudgetValueTone.Hero -> when {
            normalizedLength > 20 -> typography.headlineMedium
            normalizedLength > 16 -> typography.displaySmall
            else -> typography.displayMedium
        }

        BudgetValueTone.Prominent -> when {
            normalizedLength > 18 -> typography.headlineSmall
            normalizedLength > 14 -> typography.headlineMedium
            else -> typography.displaySmall
        }

        BudgetValueTone.Card -> when {
            normalizedLength > 18 -> typography.titleSmall
            normalizedLength > 14 -> typography.titleMedium
            else -> typography.titleLarge
        }

        BudgetValueTone.Compact -> when {
            normalizedLength > 16 -> typography.labelLarge
            normalizedLength > 12 -> typography.titleSmall
            else -> typography.titleMedium
        }
    }

    return baseStyle.copy(
        fontFamily = inter,
        fontWeight = when (tone) {
            BudgetValueTone.Hero -> FontWeight.Black
            BudgetValueTone.Prominent -> FontWeight.Bold
            BudgetValueTone.Card -> FontWeight.Bold
            BudgetValueTone.Compact -> FontWeight.SemiBold
        },
        fontFeatureSettings = "tnum",
        letterSpacing = (-0.2).sp,
    )
}

private fun unitFontSize(tone: BudgetValueTone) = when (tone) {
    BudgetValueTone.Hero -> 16.sp
    BudgetValueTone.Prominent -> 14.sp
    BudgetValueTone.Card -> 12.sp
    BudgetValueTone.Compact -> 10.sp
}
