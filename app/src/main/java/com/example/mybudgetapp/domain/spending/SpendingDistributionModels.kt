package com.example.mybudgetapp.domain.spending

import java.time.LocalDate

sealed interface PeriodFilter {
    data object ThisMonth : PeriodFilter
    data object LastMonth : PeriodFilter
    data object LastThreeMonths : PeriodFilter
    data object ThisYear : PeriodFilter
    data class Custom(
        val startDate: LocalDate,
        val endDate: LocalDate,
    ) : PeriodFilter
}

sealed interface CategoryFilter {
    data object All : CategoryFilter
    data class TopN(val limit: Int = 5) : CategoryFilter
    data class Custom(val categoryIds: Set<String>) : CategoryFilter
}

data class SpendingDistributionUiState(
    val selectedPeriod: PeriodFilter = PeriodFilter.ThisMonth,
    val categoryFilter: CategoryFilter = CategoryFilter.TopN(),
    val selectedCategoryId: String? = null,
    val totalAmount: Double = 0.0,
    val slices: List<CategorySliceUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val periodLabel: String = "",
    val comparisonLabel: String = "",
    val rangeStartDate: LocalDate = LocalDate.now(),
    val rangeEndDate: LocalDate = LocalDate.now(),
    val categories: List<DistributionCategoryUi> = emptyList(),
)

data class CategorySliceUi(
    val categoryId: String,
    val label: String,
    val amount: Double,
    val percentage: Float,
    val previousAmount: Double?,
    val previousPercentage: Float?,
    val amountDelta: Double?,
    val percentageDelta: Float?,
    val colorHex: String,
    val isOther: Boolean = false,
    val sourceCategoryIds: List<String> = listOf(categoryId),
)

data class DistributionCategoryUi(
    val categoryId: String,
    val label: String,
    val iconKey: String,
    val colorHex: String,
)

data class SpendingDistributionRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    init {
        require(!endDate.isBefore(startDate)) { "End date cannot be before start date." }
    }

    fun previousEquivalent(): SpendingDistributionRange {
        val dayCount = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
        val previousEnd = startDate.minusDays(1)
        return SpendingDistributionRange(
            startDate = previousEnd.minusDays(dayCount - 1),
            endDate = previousEnd,
        )
    }
}

const val OTHER_CATEGORY_ID = "__other__"
