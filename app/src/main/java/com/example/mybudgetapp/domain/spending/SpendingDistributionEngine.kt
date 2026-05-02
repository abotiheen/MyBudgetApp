package com.example.mybudgetapp.domain.spending

import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.BudgetTransaction
import kotlin.math.max

class SpendingDistributionEngine {

    fun compute(
        currentTransactions: List<BudgetTransaction>,
        previousTransactions: List<BudgetTransaction>,
        categoryFilter: CategoryFilter,
        topLimit: Int = 5,
        categories: List<BudgetCategory> = emptyList(),
    ): List<CategorySliceUi> {
        val categoryLookup = categories.associateBy { it.categoryKey }
        val currentTotals = currentTransactions
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .filterValues { it > 0.0 }
        if (currentTotals.isEmpty()) return emptyList()

        val previousTotals = previousTransactions
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .filterValues { it > 0.0 }

        val rankedCategoryIds = currentTotals.entries
            .sortedWith(compareByDescending<Map.Entry<String, Double>> { it.value }.thenBy { it.key })
            .map { it.key }

        val visibleCategoryIds = when (categoryFilter) {
            CategoryFilter.All -> rankedCategoryIds
            is CategoryFilter.Custom -> rankedCategoryIds.filter { it in categoryFilter.categoryIds }
            is CategoryFilter.TopN -> rankedCategoryIds.take(max(1, categoryFilter.limit))
        }
        val groupedCategoryIds = if (categoryFilter is CategoryFilter.TopN) {
            rankedCategoryIds.drop(max(1, categoryFilter.limit))
        } else {
            emptyList()
        }
        val includedCategoryIds = if (categoryFilter is CategoryFilter.TopN) {
            rankedCategoryIds
        } else {
            visibleCategoryIds
        }
        val currentTotal = includedCategoryIds.sumOf { currentTotals[it] ?: 0.0 }
        if (currentTotal <= 0.0) return emptyList()
        val previousTotal = includedCategoryIds.sumOf { previousTotals[it] ?: 0.0 }
        val hasPreviousData = previousTransactions.isNotEmpty() && previousTotal > 0.0

        val visibleSlices = visibleCategoryIds.mapNotNull { categoryId ->
            val currentAmount = currentTotals[categoryId] ?: return@mapNotNull null
            buildSlice(
                categoryId = categoryId,
                label = categoryLookup[categoryId]?.name ?: categoryId.toDisplayLabel(),
                amount = currentAmount,
                currentTotal = currentTotal,
                previousAmount = previousTotals[categoryId] ?: 0.0,
                previousTotal = previousTotal,
                hasPreviousData = hasPreviousData,
                colorHex = categoryLookup[categoryId]?.colorHex.orEmpty(),
                sourceCategoryIds = listOf(categoryId),
            )
        }

        if (categoryFilter !is CategoryFilter.TopN) {
            return visibleSlices
        }

        if (groupedCategoryIds.isEmpty()) return visibleSlices

        val otherAmount = groupedCategoryIds.sumOf { currentTotals[it] ?: 0.0 }
        if (otherAmount <= 0.0) return visibleSlices

        val otherPreviousAmount = groupedCategoryIds.sumOf { previousTotals[it] ?: 0.0 }
        return visibleSlices + buildSlice(
            categoryId = OTHER_CATEGORY_ID,
            label = "Other",
            amount = otherAmount,
            currentTotal = currentTotal,
            previousAmount = otherPreviousAmount,
            previousTotal = previousTotal,
            hasPreviousData = hasPreviousData,
            colorHex = OTHER_COLOR_HEX,
            isOther = true,
            sourceCategoryIds = groupedCategoryIds,
        )
    }

    private fun buildSlice(
        categoryId: String,
        label: String,
        amount: Double,
        currentTotal: Double,
        previousAmount: Double,
        previousTotal: Double,
        hasPreviousData: Boolean,
        colorHex: String,
        isOther: Boolean = false,
        sourceCategoryIds: List<String>,
    ): CategorySliceUi {
        val percentage = if (currentTotal <= 0.0) 0f else ((amount / currentTotal) * 100.0).toFloat()
        val previousPercentage = if (hasPreviousData) {
            ((previousAmount / previousTotal) * 100.0).toFloat()
        } else {
            null
        }
        return CategorySliceUi(
            categoryId = categoryId,
            label = label,
            amount = amount,
            percentage = percentage,
            previousAmount = if (hasPreviousData) previousAmount else null,
            previousPercentage = previousPercentage,
            amountDelta = if (hasPreviousData) amount - previousAmount else null,
            percentageDelta = previousPercentage?.let { percentage - it },
            colorHex = colorHex.ifBlank { FALLBACK_COLOR_HEX },
            isOther = isOther,
            sourceCategoryIds = sourceCategoryIds,
        )
    }

    private fun String.toDisplayLabel(): String =
        split('-', '_', ' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { segment ->
                segment.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase() else char.toString()
                }
            }
            .ifBlank { this }

    private companion object {
        const val FALLBACK_COLOR_HEX = "#63B83F"
        const val OTHER_COLOR_HEX = "#64748B"
    }
}
