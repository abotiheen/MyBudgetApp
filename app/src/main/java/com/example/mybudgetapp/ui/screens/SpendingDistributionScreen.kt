package com.example.mybudgetapp.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.domain.spending.CategoryFilter
import com.example.mybudgetapp.domain.spending.CategorySliceUi
import com.example.mybudgetapp.domain.spending.DistributionCategoryUi
import com.example.mybudgetapp.domain.spending.PeriodFilter
import com.example.mybudgetapp.domain.spending.SpendingDistributionUiState
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.theme.MyBudgetAppTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.SpendingDistributionViewModel
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.CategoryIcon
import com.example.mybudgetapp.ui.widgets.categoryAccentColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

object SpendingDistributionDestination : NavigationDestination {
    override val route = "SpendingDistribution"
    override val titleRes = R.string.spending_on_category_screen
}

@Composable
fun SpendingDistributionScreen(
    navigateToTransactions: (LocalDate, LocalDate, List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SpendingDistributionViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    SpendingDistributionContent(
        modifier = modifier,
        uiState = uiState,
        onSelectPeriod = viewModel::selectPeriod,
        onOpenCustomRange = { showDatePicker = true },
        onSelectAllCategories = viewModel::selectAllCategories,
        onSelectTopCategories = { viewModel.selectTopCategories() },
        onOpenCustomCategories = { showCategoryPicker = true },
        onSelectCustomCategories = viewModel::selectCustomCategories,
        onSelectCategory = viewModel::selectCategory,
        onOpenTransactions = { slice ->
            navigateToTransactions(
                uiState.rangeStartDate,
                uiState.rangeEndDate,
                slice.sourceCategoryIds,
            )
        },
    )

    if (showDatePicker) {
        CustomRangeSheet(
            initialStartDate = uiState.rangeStartDate,
            initialEndDate = uiState.rangeEndDate,
            onDismissRequest = { showDatePicker = false },
            onApply = {
                viewModel.selectPeriod(PeriodFilter.Custom(it.first, it.second))
                showDatePicker = false
            },
        )
    }

    if (showCategoryPicker) {
        CustomCategorySheet(
            categories = uiState.categories,
            currentFilter = uiState.categoryFilter,
            onDismissRequest = { showCategoryPicker = false },
            onApply = {
                viewModel.selectCustomCategories(it)
                showCategoryPicker = false
            },
        )
    }
}

@Composable
private fun SpendingDistributionContent(
    uiState: SpendingDistributionUiState,
    onSelectPeriod: (PeriodFilter) -> Unit,
    onOpenCustomRange: () -> Unit,
    onSelectAllCategories: () -> Unit,
    onSelectTopCategories: () -> Unit,
    onOpenCustomCategories: () -> Unit,
    onSelectCustomCategories: (Set<String>) -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenTransactions: (CategorySliceUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedSlice = uiState.slices.firstOrNull { it.categoryId == uiState.selectedCategoryId }
        ?: uiState.slices.firstOrNull()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
    ) { paddingValues ->
        BudgetBackdrop(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = BudgetTheme.spacing.lg,
                    end = BudgetTheme.spacing.lg,
                    top = BudgetTheme.spacing.lg,
                    bottom = 136.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
            ) {
                item {
                    DistributionHeader(
                        periodLabel = uiState.periodLabel,
                        totalAmount = uiState.totalAmount,
                        comparisonLabel = uiState.comparisonLabel,
                    )
                }
                item {
                    PeriodSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        onSelectPeriod = onSelectPeriod,
                        onOpenCustomRange = onOpenCustomRange,
                    )
                }
                item {
                    CategoryFilterSelector(
                        selectedFilter = uiState.categoryFilter,
                        onSelectAllCategories = onSelectAllCategories,
                        onSelectTopCategories = onSelectTopCategories,
                        onOpenCustomCategories = onOpenCustomCategories,
                    )
                }
                item {
                    DistributionChartCard(
                        uiState = uiState,
                        selectedSlice = selectedSlice,
                        onSelectCategory = onSelectCategory,
                        onOpenTransactions = onOpenTransactions,
                    )
                }
                item {
                    DistributionListHeader(sliceCount = uiState.slices.size)
                }
                if (uiState.slices.isEmpty()) {
                    item {
                        EmptyDistributionCard()
                    }
                } else {
                    items(uiState.slices, key = { it.categoryId }) { slice ->
                        CategoryDistributionRow(
                            slice = slice,
                            selected = slice.categoryId == selectedSlice?.categoryId,
                            onClick = { onSelectCategory(slice.categoryId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DistributionHeader(
    periodLabel: String,
    totalAmount: Double,
    comparisonLabel: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(BudgetTheme.radii.md),
                ) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Spending Distribution",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            BudgetValueText(
                text = formatCurrencyIraqiDinar(totalAmount),
                tone = BudgetValueTone.Hero,
                color = MaterialTheme.colorScheme.onSurface,
                unitLabel = "IQD",
            )
            Text(
                text = comparisonLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: PeriodFilter,
    onSelectPeriod: (PeriodFilter) -> Unit,
    onOpenCustomRange: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
    ) {
        PeriodChip("This month", selectedPeriod == PeriodFilter.ThisMonth) {
            onSelectPeriod(PeriodFilter.ThisMonth)
        }
        PeriodChip("Last month", selectedPeriod == PeriodFilter.LastMonth) {
            onSelectPeriod(PeriodFilter.LastMonth)
        }
        PeriodChip("Last 3 months", selectedPeriod == PeriodFilter.LastThreeMonths) {
            onSelectPeriod(PeriodFilter.LastThreeMonths)
        }
        PeriodChip("This year", selectedPeriod == PeriodFilter.ThisYear) {
            onSelectPeriod(PeriodFilter.ThisYear)
        }
        PeriodChip("Custom", selectedPeriod is PeriodFilter.Custom, onOpenCustomRange)
    }
}

@Composable
private fun PeriodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        } else {
            null
        },
    )
}

@Composable
private fun CategoryFilterSelector(
    selectedFilter: CategoryFilter,
    onSelectAllCategories: () -> Unit,
    onSelectTopCategories: () -> Unit,
    onOpenCustomCategories: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
    ) {
        PeriodChip("All", selectedFilter == CategoryFilter.All, onSelectAllCategories)
        PeriodChip("Top 5", selectedFilter is CategoryFilter.TopN, onSelectTopCategories)
        PeriodChip("Custom", selectedFilter is CategoryFilter.Custom, onOpenCustomCategories)
    }
}

@Composable
private fun DistributionChartCard(
    uiState: SpendingDistributionUiState,
    selectedSlice: CategorySliceUi?,
    onSelectCategory: (String) -> Unit,
    onOpenTransactions: (CategorySliceUi) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(344.dp),
                contentAlignment = Alignment.Center,
            ) {
                DonutChart(
                    slices = uiState.slices,
                    selectedCategoryId = selectedSlice?.categoryId,
                    onSliceClick = onSelectCategory,
                    modifier = Modifier.size(324.dp),
                )
                CenterInfoContent(
                    slice = selectedSlice,
                    modifier = Modifier
                        .width(200.dp)
                        .animateContentSize(),
                )
            }
            if (selectedSlice != null) {
                OutlinedButton(onClick = { onOpenTransactions(selectedSlice) }) {
                    Text("View transactions")
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    slices: List<CategorySliceUi>,
    selectedCategoryId: String?,
    onSliceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = slices.sumOf { it.amount }.coerceAtLeast(0.0)
    val currentOnSliceClick by rememberUpdatedState(onSliceClick)
    val animatedFractions = slices.map { slice ->
        animateFloatAsState(
            targetValue = if (total <= 0.0) 0f else (slice.amount / total).toFloat(),
            animationSpec = tween(durationMillis = 520),
            label = "donutSliceFraction-${slice.categoryId}",
        )
    }
    val selectionScale by animateFloatAsState(
        targetValue = if (selectedCategoryId != null) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "donutSelectionScale",
    )
    val colors = slices.map { categoryAccentColor(it.colorHex, it.categoryId) }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val selectedSlice = slices.firstOrNull { it.categoryId == selectedCategoryId }
    val chartDescription = selectedSlice?.accessibilitySummary()
        ?: if (slices.isEmpty()) {
            "Spending distribution chart. No spending in this view."
        } else {
            "Spending distribution chart. Tap a slice to inspect its share and change."
        }

    Canvas(
        modifier = modifier
            .semantics { contentDescription = chartDescription }
            .pointerInput(slices, total) {
                detectTapGestures { tapOffset ->
                    if (total <= 0.0 || slices.isEmpty()) return@detectTapGestures
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = tapOffset.x - center.x
                    val dy = tapOffset.y - center.y
                    val distance = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    val outerRadius = minOf(size.width, size.height) / 2f
                    val strokeWidth = outerRadius * 0.2f
                    if (distance !in (outerRadius - strokeWidth * 1.5f)..outerRadius) return@detectTapGestures

                    val angle = ((atan2(dy, dx) * 180f / PI.toFloat()) + 90f + 360f) % 360f
                    var start = 0f
                    var selectedId: String? = null
                    for (slice in slices) {
                        val sweep = ((slice.amount / total).toFloat() * 360f).coerceAtLeast(0f)
                        if (angle >= start && angle <= start + sweep) {
                            selectedId = slice.categoryId
                            break
                        }
                        start += sweep
                    }
                    selectedId?.let(currentOnSliceClick)
                }
            },
    ) {
        val diameter = minOf(size.width, size.height)
        val strokeWidth = diameter * 0.12f
        val inset = strokeWidth / 2f + 8.dp.toPx()
        val arcSize = Size(diameter - inset * 2f, diameter - inset * 2f)
        val topLeft = Offset(
            x = (size.width - arcSize.width) / 2f,
            y = (size.height - arcSize.height) / 2f,
        )

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )

        var startAngle = -90f
        animatedFractions.forEachIndexed { index, animatedFraction ->
            val slice = slices[index]
            val selected = slice.categoryId == selectedCategoryId
            val color = colors[index]
            val gap = if (slices.size > 1) 3f else 0f
            val sweep = (animatedFraction.value * 360f - gap).coerceAtLeast(0f)
            val alpha = if (selectedCategoryId == null || selected) 1f else 0.34f
            val selectedWidth = if (selected) strokeWidth + 8.dp.toPx() * selectionScale else strokeWidth

            drawArc(
                color = color.copy(alpha = alpha),
                startAngle = startAngle + gap / 2f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = selectedWidth, cap = StrokeCap.Butt),
            )
            startAngle += animatedFraction.value * 360f
        }
    }
}

@Composable
fun CenterInfoContent(
    slice: CategorySliceUi?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        shape = CircleShape,
    ) {
        AnimatedContent(
            targetState = slice,
            label = "distributionCenterContent",
        ) { targetSlice ->
            if (targetSlice == null) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "No spending",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "0% · 0 IQD",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val deltaTone = deltaColor(targetSlice.amountDelta)
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = targetSlice.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${targetSlice.percentage.roundToInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Spent ${formatCompactCurrencyIraqiDinar(targetSlice.amount)} IQD",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = targetSlice.changeSummaryText(),
                        style = MaterialTheme.typography.labelMedium,
                        color = deltaTone,
                    )
                    targetSlice.previousPercentage?.let {
                        Text(
                            text = "Previous share ${it.roundToInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun deltaColor(delta: Double?): Color = when {
    delta == null -> MaterialTheme.colorScheme.onSurfaceVariant
    delta > 0.0 -> BudgetTheme.extendedColors.danger
    delta < 0.0 -> BudgetTheme.extendedColors.success
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun CategorySliceUi.changeSummaryText(): String {
    val amountDeltaValue = amountDelta ?: return "No previous data"
    val direction = when {
        amountDeltaValue > 0.0 -> "Up"
        amountDeltaValue < 0.0 -> "Down"
        else -> "No change"
    }
    val sharePoints = percentageDelta?.roundToInt() ?: 0
    val shareText = when {
        sharePoints > 0 -> "+$sharePoints pts"
        sharePoints < 0 -> "$sharePoints pts"
        else -> "0 pts"
    }
    return if (amountDeltaValue == 0.0) {
        "$direction vs previous"
    } else {
        "$direction ${formatCompactCurrencyIraqiDinar(kotlin.math.abs(amountDeltaValue))} IQD · $shareText"
    }
}

private fun CategorySliceUi.rowSummaryText(): String =
    "${percentage.roundToInt()}% share · ${formatCompactCurrencyIraqiDinar(amount)} IQD spent"

private fun CategorySliceUi.accessibilitySummary(): String =
    "$label, ${percentage.roundToInt()} percent share, ${formatCompactCurrencyIraqiDinar(amount)} Iraqi dinars spent, ${changeSummaryText()}."

@Composable
private fun DistributionListHeader(sliceCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Category shares",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "$sliceCount slices in this view",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CategoryDistributionRow(
    slice: CategorySliceUi,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = categoryAccentColor(slice.colorHex, slice.categoryId)
    Surface(
        modifier = Modifier.semantics {
            contentDescription = slice.accessibilitySummary()
        },
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accent.copy(alpha = 0.62f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
        ),
        shadowElevation = if (selected) BudgetTheme.elevations.level2 else BudgetTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = accent.copy(alpha = 0.13f),
                shape = RoundedCornerShape(BudgetTheme.radii.md),
            ) {
                Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                    if (slice.isOther) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        CategoryIcon(
                            iconKey = "",
                            fallbackCategoryKey = slice.categoryId,
                            tint = accent,
                            size = 24.dp,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = slice.rowSummaryText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                BudgetValueText(
                    text = formatCompactCurrencyIraqiDinar(slice.amount),
                    tone = BudgetValueTone.Compact,
                    color = MaterialTheme.colorScheme.onSurface,
                    unitLabel = "IQD",
                )
                Text(
                    text = slice.changeSummaryText(),
                    style = MaterialTheme.typography.labelMedium,
                    color = deltaColor(slice.amountDelta),
                )
            }
        }
    }
}

@Composable
private fun EmptyDistributionCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(BudgetTheme.radii.md),
            ) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "No spending in this view",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Try another period or category filter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangeSheet(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    onDismissRequest: () -> Unit,
    onApply: (Pair<LocalDate, LocalDate>) -> Unit,
) {
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate.toEpochMillis(),
        initialSelectedEndDateMillis = initialEndDate.toEpochMillis(),
    )
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxHeight(),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BudgetTheme.spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Select date range",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            DateRangePicker(
                state = pickerState,
                modifier = Modifier.weight(1f),
                showModeToggle = false,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BudgetTheme.spacing.lg, vertical = BudgetTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm, Alignment.End),
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
                Button(
                    enabled = pickerState.selectedStartDateMillis != null && pickerState.selectedEndDateMillis != null,
                    onClick = {
                        val start = pickerState.selectedStartDateMillis?.toLocalDate() ?: return@Button
                        val end = pickerState.selectedEndDateMillis?.toLocalDate() ?: return@Button
                        onApply(start to end)
                    },
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomCategorySheet(
    categories: List<DistributionCategoryUi>,
    currentFilter: CategoryFilter,
    onDismissRequest: () -> Unit,
    onApply: (Set<String>) -> Unit,
) {
    var selectedIds by remember(categories, currentFilter) {
        mutableStateOf(
            when (currentFilter) {
                is CategoryFilter.Custom -> currentFilter.categoryIds
                else -> categories.map { it.categoryId }.toSet()
            }
        )
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxHeight(),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(
                start = BudgetTheme.spacing.lg,
                end = BudgetTheme.spacing.lg,
                top = BudgetTheme.spacing.sm,
                bottom = BudgetTheme.spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            item {
                Text(
                    text = "Choose categories",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(categories, key = { it.categoryId }) { category ->
                val checked = category.categoryId in selectedIds
                val accent = categoryAccentColor(category.colorHex, category.categoryId)
                Surface(
                    onClick = {
                        selectedIds = if (checked) {
                            selectedIds - category.categoryId
                        } else {
                            selectedIds + category.categoryId
                        }
                    },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                    shadowElevation = BudgetTheme.elevations.level1,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = BudgetTheme.spacing.md, vertical = BudgetTheme.spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            color = accent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(BudgetTheme.radii.md),
                        ) {
                            Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                                CategoryIcon(
                                    iconKey = category.iconKey,
                                    fallbackCategoryKey = category.categoryId,
                                    tint = accent,
                                    size = 22.dp,
                                )
                            }
                        }
                        Text(
                            text = category.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = { onApply(selectedIds) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BudgetTheme.spacing.md),
                ) {
                    Text("Apply categories")
                }
            }
        }
    }
}

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun createDistributionTransactionsRoute(
    startDate: LocalDate,
    endDate: LocalDate,
    categoryIds: List<String>,
): String {
    val encodedCategories = Uri.encode(categoryIds.joinToString(","))
    return "${SpendingDistributionTransactionsDestination.route}/$startDate/$endDate/$encodedCategories"
}

@Preview(showBackground = true)
@Composable
private fun SpendingDistributionPreview() {
    MyBudgetAppTheme {
        SpendingDistributionContent(
            uiState = SpendingDistributionUiState(
                selectedPeriod = PeriodFilter.ThisMonth,
                categoryFilter = CategoryFilter.TopN(),
                selectedCategoryId = "food",
                totalAmount = 3_240_000.0,
                periodLabel = "October 2023",
                comparisonLabel = "vs Sep 1, 2023 - Sep 30, 2023",
                slices = listOf(
                    CategorySliceUi("food", "Food", 1_240_000.0, 38f, 1_100_000.0, 33f, 140_000.0, 5f, "#5EBB4A"),
                    CategorySliceUi("shopping", "Shopping", 850_000.0, 26f, 920_000.0, 28f, -70_000.0, -2f, "#60A5FA"),
                    CategorySliceUi("housing", "Housing", 750_000.0, 23f, 750_000.0, 22f, 0.0, 1f, "#F59E0B"),
                    CategorySliceUi("transportation", "Transportation", 400_000.0, 12f, 360_000.0, 11f, 40_000.0, 1f, "#2D9CDB"),
                ),
            ),
            onSelectPeriod = {},
            onOpenCustomRange = {},
            onSelectAllCategories = {},
            onSelectTopCategories = {},
            onOpenCustomCategories = {},
            onSelectCustomCategories = {},
            onSelectCategory = {},
            onOpenTransactions = {},
        )
    }
}
