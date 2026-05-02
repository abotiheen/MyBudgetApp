package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.domain.spending.CategoryFilter
import com.example.mybudgetapp.domain.spending.DistributionCategoryUi
import com.example.mybudgetapp.domain.spending.PeriodFilter
import com.example.mybudgetapp.domain.spending.SpendingDistributionEngine
import com.example.mybudgetapp.domain.spending.SpendingDistributionRange
import com.example.mybudgetapp.domain.spending.SpendingDistributionUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class SpendingDistributionViewModel(
    private val itemRepository: ItemRepository,
    private val engine: SpendingDistributionEngine = SpendingDistributionEngine(),
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val selectedPeriod = MutableStateFlow<PeriodFilter>(PeriodFilter.ThisMonth)
    private val categoryFilter = MutableStateFlow<CategoryFilter>(CategoryFilter.TopN())
    private val selectedCategoryId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = combine(selectedPeriod, categoryFilter) { period, filter ->
        period to filter
    }.flatMapLatest { (period, filter) ->
        val range = period.toRange(today)
        val previousRange = range.previousEquivalent()
        combine(
            itemRepository.getExpenseTransactionsInRange(
                startDate = range.startDate.toString(),
                endDate = range.endDate.toString(),
            ),
            itemRepository.getExpenseTransactionsInRange(
                startDate = previousRange.startDate.toString(),
                endDate = previousRange.endDate.toString(),
            ),
            itemRepository.getCategoriesByType(
                type = TRANSACTION_TYPE_EXPENSE,
                includeArchived = true,
            ),
            selectedCategoryId,
        ) { currentTransactions, previousTransactions, categories, selectedId ->
            val slices = engine.compute(
                currentTransactions = currentTransactions,
                previousTransactions = previousTransactions,
                categoryFilter = filter,
                categories = categories,
            )
            val normalizedSelectedId = selectedId?.takeIf { id ->
                slices.any { it.categoryId == id }
            } ?: slices.firstOrNull()?.categoryId

            SpendingDistributionUiState(
                selectedPeriod = period,
                categoryFilter = filter,
                selectedCategoryId = normalizedSelectedId,
                totalAmount = slices.sumOf { it.amount },
                slices = slices,
                isLoading = false,
                error = null,
                periodLabel = period.toLabel(today),
                comparisonLabel = previousRange.toComparisonLabel(),
                rangeStartDate = range.startDate,
                rangeEndDate = range.endDate,
                categories = categories.toDistributionCategoryUi(),
            )
        }
    }.catch { throwable ->
        emit(
            SpendingDistributionUiState(
                selectedPeriod = selectedPeriod.value,
                categoryFilter = categoryFilter.value,
                isLoading = false,
                error = throwable.message ?: "Could not load spending distribution.",
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = SpendingDistributionUiState(
            selectedPeriod = selectedPeriod.value,
            categoryFilter = categoryFilter.value,
            isLoading = true,
            periodLabel = selectedPeriod.value.toLabel(today),
            rangeStartDate = selectedPeriod.value.toRange(today).startDate,
            rangeEndDate = selectedPeriod.value.toRange(today).endDate,
        ),
    )

    fun selectPeriod(periodFilter: PeriodFilter) {
        selectedPeriod.value = periodFilter.normalized()
    }

    fun selectAllCategories() {
        categoryFilter.value = CategoryFilter.All
    }

    fun selectTopCategories(limit: Int = 5) {
        categoryFilter.value = CategoryFilter.TopN(limit)
    }

    fun selectCustomCategories(categoryIds: Set<String>) {
        categoryFilter.value = CategoryFilter.Custom(categoryIds)
    }

    fun selectCategory(categoryId: String) {
        selectedCategoryId.value = categoryId
    }

    private fun List<BudgetCategory>.toDistributionCategoryUi(): List<DistributionCategoryUi> =
        map {
            DistributionCategoryUi(
                categoryId = it.categoryKey,
                label = it.name,
                iconKey = it.iconKey,
                colorHex = it.colorHex,
            )
        }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

fun PeriodFilter.toRange(today: LocalDate): SpendingDistributionRange = when (this) {
    PeriodFilter.ThisMonth -> {
        val month = YearMonth.of(today.year, today.monthValue)
        SpendingDistributionRange(month.atDay(1), month.atEndOfMonth())
    }

    PeriodFilter.LastMonth -> {
        val month = YearMonth.of(today.year, today.monthValue).minusMonths(1)
        SpendingDistributionRange(month.atDay(1), month.atEndOfMonth())
    }

    PeriodFilter.LastThreeMonths -> SpendingDistributionRange(
        startDate = today.minusMonths(3),
        endDate = today,
    )

    PeriodFilter.ThisYear -> SpendingDistributionRange(
        startDate = LocalDate.of(today.year, 1, 1),
        endDate = LocalDate.of(today.year, 12, 31),
    )

    is PeriodFilter.Custom -> {
        val start = minOf(startDate, endDate)
        val end = maxOf(startDate, endDate)
        SpendingDistributionRange(start, end)
    }
}

private fun PeriodFilter.normalized(): PeriodFilter = when (this) {
    is PeriodFilter.Custom -> {
        val start = minOf(startDate, endDate)
        val end = maxOf(startDate, endDate)
        copy(startDate = start, endDate = end)
    }

    else -> this
}

private fun PeriodFilter.toLabel(today: LocalDate): String = when (this) {
    PeriodFilter.ThisMonth -> YearMonth.of(today.year, today.monthValue).toMonthLabel()
    PeriodFilter.LastMonth -> YearMonth.of(today.year, today.monthValue).minusMonths(1).toMonthLabel()
    PeriodFilter.LastThreeMonths -> "Last 3 months"
    PeriodFilter.ThisYear -> today.year.toString()
    is PeriodFilter.Custom -> "${startDate.toShortLabel()} - ${endDate.toShortLabel()}"
}

private fun SpendingDistributionRange.toComparisonLabel(): String =
    "vs ${startDate.toShortLabel()} - ${endDate.toShortLabel()}"

private fun YearMonth.toMonthLabel(): String =
    "${month.name.lowercase(Locale.ROOT).replaceFirstChar { it.titlecase() }} $year"

private fun LocalDate.toShortLabel(): String =
    format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
