package com.example.mybudgetapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class BudgetSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 28.dp,
    val xxl: Dp = 40.dp,
    val xxxl: Dp = 56.dp,
)

@Immutable
data class BudgetRadii(
    val sm: Dp = 12.dp,
    val md: Dp = 18.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val pill: Dp = 999.dp,
)

@Immutable
data class BudgetElevations(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 2.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 10.dp,
)

@Immutable
data class BudgetExtendedColors(
    val canvas: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val accentBlue: Color,
    val accentCoral: Color,
    val accentGold: Color,
    val food: Color,
    val transit: Color,
    val others: Color,
    val income: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val edge: Color,
    val mist: Color,
)

internal val LocalBudgetSpacing = staticCompositionLocalOf { BudgetSpacing() }
internal val LocalBudgetRadii = staticCompositionLocalOf { BudgetRadii() }
internal val LocalBudgetElevations = staticCompositionLocalOf { BudgetElevations() }
internal val LocalBudgetExtendedColors = staticCompositionLocalOf {
    BudgetExtendedColors(
        canvas = backgroundLight,
        success = Color(0xFF58A73D),
        warning = Color(0xFF9FBF4B),
        danger = Color(0xFF8C675A),
        accentBlue = Color(0xFF55C084),
        accentCoral = Color(0xFF78B95B),
        accentGold = Color(0xFFC2D75A),
        food = Color(0xFF5EBB4A),
        transit = Color(0xFF40B88D),
        others = Color(0xFF9AAF47),
        income = Color(0xFF4FAF33),
        heroStart = Color(0xFFF2FFD8),
        heroEnd = Color(0xFFD7F7A6),
        edge = Color(0xFFDAEBCB),
        mist = Color(0xFFFBFFF6),
    )
}

object BudgetTheme {
    val spacing: BudgetSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalBudgetSpacing.current

    val radii: BudgetRadii
        @Composable
        @ReadOnlyComposable
        get() = LocalBudgetRadii.current

    val elevations: BudgetElevations
        @Composable
        @ReadOnlyComposable
        get() = LocalBudgetElevations.current

    val extendedColors: BudgetExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBudgetExtendedColors.current
}

internal val BudgetLightExtendedColors = BudgetExtendedColors(
    canvas = backgroundLight,
    success = Color(0xFF58A73D),
    warning = Color(0xFF9FBF4B),
    danger = Color(0xFF8C675A),
    accentBlue = Color(0xFF55C084),
    accentCoral = Color(0xFF78B95B),
    accentGold = Color(0xFFC2D75A),
    food = Color(0xFF5EBB4A),
    transit = Color(0xFF40B88D),
    others = Color(0xFF9AAF47),
    income = Color(0xFF4FAF33),
    heroStart = Color(0xFFF2FFD8),
    heroEnd = Color(0xFFD7F7A6),
    edge = Color(0xFFDAEBCB),
    mist = Color(0xFFFBFFF6),
)

internal val BudgetDarkExtendedColors = BudgetExtendedColors(
    canvas = backgroundDark,
    success = Color(0xFF9AC79D),
    warning = Color(0xFFB5C89A),
    danger = Color(0xFFD0AAA0),
    accentBlue = Color(0xFF92BC98),
    accentCoral = Color(0xFF8CB291),
    accentGold = Color(0xFFBDD09C),
    food = Color(0xFF9BC39C),
    transit = Color(0xFFA0C2A3),
    others = Color(0xFFB2C4A1),
    income = Color(0xFF91C294),
    heroStart = Color(0xFF1E2E22),
    heroEnd = Color(0xFF2A3C30),
    edge = Color(0xFF273229),
    mist = Color(0xFF1A221C),
)
