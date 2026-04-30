package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.ui.screens.TotalIncomeDestinationForYear
import com.example.mybudgetapp.ui.shared.models.DetailGroupGranularity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TotalSpendingScreenForYearViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val date: LocalDate = LocalDate.now()
    private val currentYear: Int = checkNotNull(savedStateHandle[TotalIncomeDestinationForYear.year.toString()])
    private val initialIsIncome: Boolean = checkNotNull(savedStateHandle[TotalIncomeDestinationForYear.isIncome.toString()])
    private var isThisYearCurrent = true
    private var isDeleteDialogVisible = MutableStateFlow(false)
    private val selectedTransactionType = MutableStateFlow(initialIsIncome)
    private val screenMeta = combine(
        selectedTransactionType,
        isDeleteDialogVisible,
    ) { isIncome, isDeleteDialogVisible ->
        isIncome to isDeleteDialogVisible
    }
    private val transactionContent = combine(
        itemRepository.getTransactionsForYear(year = currentYear),
        itemRepository.getTotalSpendingOverallForYear(year = currentYear),
        itemRepository.getTotalIncomeOverallForYear(year = currentYear),
        itemRepository.getIncomeTransactionsForYear(year = currentYear),
        itemRepository.getAllCategories(includeArchived = true),
    ) { spendingItems, totalSpending, totalIncome, incomeItems, categories ->
        val categoryLookup = categories.associateBy { it.categoryKey }
        val spendingGroupResult = spendingItems.toGroupedSpendingItemSections(
            year = currentYear,
            month = 0,
            categoryLookup = categoryLookup,
            granularity = DetailGroupGranularity.MONTH,
            totalLabel = "Spending",
        )
        val incomeGroupResult = incomeItems.toGroupedSpendingItemSections(
            year = currentYear,
            month = 0,
            categoryLookup = categoryLookup,
            granularity = DetailGroupGranularity.MONTH,
            totalLabel = "Income",
        )
        YearSpendingContent(
            totalSpending = formatCompactCurrencyIraqiDinar(totalSpending),
            spendingItemList = spendingGroupResult.items,
            spendingGroups = spendingGroupResult.groups,
            totalIncome = formatCompactCurrencyIraqiDinar(totalIncome),
            incomeItemList = incomeGroupResult.items,
            incomeGroups = incomeGroupResult.groups,
        )
    }

    init {
        isThisYearCurrent = (date.year == currentYear)
    }

    val uiState: StateFlow<TotalSpendingUiState> = combine(
        transactionContent,
        screenMeta,
    ) { content, screenMeta ->
        val (isIncome, isDeleteDialogVisible) = screenMeta
        TotalSpendingUiState(
            totalSpending = content.totalSpending,
            spendingItemList = content.spendingItemList,
            month = currentYear.toString(),
            isIncome = isIncome,
            totalIncome = content.totalIncome,
            incomeItemList = content.incomeItemList,
            spendingGroups = content.spendingGroups,
            incomeGroups = content.incomeGroups,
            isThisMonthCurrent = isThisYearCurrent,
            isDeleteDialogVisible = isDeleteDialogVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = TotalSpendingUiState()
    )

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            itemRepository.deleteTransactionWithId(itemId)
        }
    }

    fun displayConfirmDelete(isIt: Boolean) {
        isDeleteDialogVisible.value = isIt
    }

    fun selectTransactionType(isIncome: Boolean) {
        selectedTransactionType.value = isIncome
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

private data class YearSpendingContent(
    val totalSpending: String,
    val totalIncome: String,
    val spendingItemList: List<SpendingItem>,
    val incomeItemList: List<SpendingItem>,
    val spendingGroups: List<com.example.mybudgetapp.ui.shared.models.DetailGroupUi<SpendingItem>>,
    val incomeGroups: List<com.example.mybudgetapp.ui.shared.models.DetailGroupUi<SpendingItem>>,
)
