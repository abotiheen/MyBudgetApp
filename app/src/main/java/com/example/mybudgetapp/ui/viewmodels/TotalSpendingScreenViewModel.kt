package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.usableImagePath
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.normalizedTransactionTitleKey
import com.example.mybudgetapp.database.resolvedTransactionTitle
import com.example.mybudgetapp.ui.screens.TotalIncomeDestination
import com.example.mybudgetapp.ui.shared.models.DetailGroupGranularity
import com.example.mybudgetapp.ui.shared.models.DetailGroupUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale

class TotalSpendingScreenViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentMonthValue: Int = checkNotNull(savedStateHandle[TotalIncomeDestination.month])
    private val currentYear: Int = checkNotNull(savedStateHandle[TotalIncomeDestination.year])
    private val initialIsIncome: Boolean = checkNotNull(savedStateHandle[TotalIncomeDestination.isIncome])
    private val date: LocalDate = LocalDate.now()
    private val isDeleteDialogVisible = MutableStateFlow(false)
    private val selectedTransactionType = MutableStateFlow(initialIsIncome)
    private val screenMeta = combine(
        selectedTransactionType,
        isDeleteDialogVisible,
    ) { isIncome, isDeleteDialogVisible ->
        isIncome to isDeleteDialogVisible
    }
    private val transactionContent = combine(
        itemRepository.getTransactions(month = currentMonthValue, year = currentYear),
        itemRepository.getTotalSpendingOverall(year = currentYear, month = currentMonthValue),
        itemRepository.getTotalIncomeOverall(year = currentYear, month = currentMonthValue),
        itemRepository.getIncomeTransactions(year = currentYear, month = currentMonthValue),
        itemRepository.getAllCategories(includeArchived = true),
    ) { spendingItems, totalSpending, totalIncome, incomeItems, categories ->
        val categoryLookup = categories.associateBy { it.categoryKey }
        val spendingGroupResult = spendingItems.toGroupedSpendingItemSections(
            year = currentYear,
            month = currentMonthValue,
            categoryLookup = categoryLookup,
            granularity = DetailGroupGranularity.DAY,
            totalLabel = "Spending",
        )
        val incomeGroupResult = incomeItems.toGroupedSpendingItemSections(
            year = currentYear,
            month = currentMonthValue,
            categoryLookup = categoryLookup,
            granularity = DetailGroupGranularity.DAY,
            totalLabel = "Income",
        )
        TotalSpendingContent(
            totalSpending = formatCompactCurrencyIraqiDinar(totalSpending),
            spendingItemList = spendingGroupResult.items,
            spendingGroups = spendingGroupResult.groups,
            totalIncome = formatCompactCurrencyIraqiDinar(totalIncome),
            incomeItemList = incomeGroupResult.items,
            incomeGroups = incomeGroupResult.groups,
        )
    }

    val uiState: StateFlow<TotalSpendingUiState> = combine(
        transactionContent,
        screenMeta,
    ) { content, screenMeta ->
        val (isIncome, isDeleteDialogVisible) = screenMeta
        TotalSpendingUiState(
            totalSpending = content.totalSpending,
            spendingItemList = content.spendingItemList,
            month = "${Month.of(currentMonthValue).toString().capitalized()} $currentYear",
            isIncome = isIncome,
            totalIncome = content.totalIncome,
            incomeItemList = content.incomeItemList,
            spendingGroups = content.spendingGroups,
            incomeGroups = content.incomeGroups,
            isThisMonthCurrent = currentYear == date.year && currentMonthValue == date.monthValue,
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

data class TotalSpendingUiState(
    val isDeleteDialogVisible: Boolean = false,
    val isThisMonthCurrent: Boolean = true,
    val totalSpending: String = "",
    val month: String = "",
    val isIncome: Boolean = true,
    val totalIncome: String = "",
    val spendingItemList: List<SpendingItem> = listOf(),
    val incomeItemList: List<SpendingItem> = listOf(),
    val spendingGroups: List<DetailGroupUi<SpendingItem>> = listOf(),
    val incomeGroups: List<DetailGroupUi<SpendingItem>> = listOf(),
)

data class SpendingItem(
    val imagePath: String? = null,
    val name: String = "",
    val date: String = "",
    val totalCost: String = "",
    val amountValue: Double = 0.0,
    val type: String = "",
    val category: String = "",
    val categoryLabel: String = "",
    val categoryIconKey: String = "",
    val categoryColorHex: String = "",
    val itemId: Long = 0,
    val year: Int = 0,
    val month: Int = 0,
)

private data class SpendingItemGroupKey(
    val title: String,
    val category: String,
    val type: String,
)

private data class TotalSpendingContent(
    val totalSpending: String,
    val totalIncome: String,
    val spendingItemList: List<SpendingItem>,
    val incomeItemList: List<SpendingItem>,
    val spendingGroups: List<DetailGroupUi<SpendingItem>>,
    val incomeGroups: List<DetailGroupUi<SpendingItem>>,
)

data class SpendingItemGroupResult(
    val items: List<SpendingItem>,
    val groups: List<DetailGroupUi<SpendingItem>>,
)

private data class SpendingBucketGroupKey(
    val bucketDate: LocalDate,
    val title: String,
    val category: String,
    val type: String,
)

fun List<BudgetTransaction>.toGroupedSpendingItemSections(
    year: Int,
    month: Int,
    categoryLookup: Map<String, com.example.mybudgetapp.database.BudgetCategory>,
    granularity: DetailGroupGranularity,
    totalLabel: String,
): SpendingItemGroupResult {
    val groupedItems = this
        .groupBy { transaction ->
            val transactionDate = parseTransactionDate(transaction.transactionDate)
            SpendingBucketGroupKey(
                bucketDate = bucketDateFor(transactionDate, granularity),
                title = normalizedTransactionTitleKey(transaction.title, transaction.category, transaction.type),
                category = transaction.category,
                type = transaction.type,
            )
        }
        .values
        .map { transactions ->
            val latestTransaction = transactions.maxWithOrNull(
                compareBy<BudgetTransaction> { it.transactionDate }.thenBy { it.transactionId }
            ) ?: transactions.first()
            SpendingItem(
                imagePath = usableImagePath(
                    latestTransaction.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath }
                ),
                name = resolvedTransactionTitle(latestTransaction.title, latestTransaction.category, latestTransaction.type),
                date = latestTransaction.transactionDate,
                totalCost = formatCompactCurrencyIraqiDinar(transactions.sumOf { it.amount }),
                amountValue = transactions.sumOf { it.amount },
                type = latestTransaction.type,
                category = latestTransaction.category,
                categoryLabel = categoryLookup[latestTransaction.category]?.name ?: latestTransaction.category.capitalized(),
                categoryIconKey = categoryLookup[latestTransaction.category]?.iconKey.orEmpty(),
                categoryColorHex = categoryLookup[latestTransaction.category]?.colorHex.orEmpty(),
                itemId = latestTransaction.transactionId,
                year = year,
                month = month,
            )
        }
        .sortedWith(compareByDescending<SpendingItem> { it.date }.thenByDescending { it.itemId })

    val groups = groupedItems
        .groupBy { bucketDateFor(parseTransactionDate(it.date), granularity) }
        .toList()
        .sortedByDescending { (bucketDate, _) -> bucketDate }
        .map { (bucketDate, items) ->
            DetailGroupUi(
                key = "${granularity.name.lowercase(Locale.ROOT)}-$bucketDate",
                label = bucketDate.toGroupLabel(granularity),
                displayTotal = formatCompactCurrencyIraqiDinar(items.sumOf { it.amountValue }),
                totalLabel = totalLabel,
                items = items.sortedWith(compareByDescending<SpendingItem> { it.date }.thenByDescending { it.itemId }),
            )
        }

    return SpendingItemGroupResult(
        items = groupedItems,
        groups = groups,
    )
}

fun List<BudgetTransaction>.toGroupedSpendingItems(
    year: Int,
    month: Int,
    categoryLookup: Map<String, com.example.mybudgetapp.database.BudgetCategory>,
): List<SpendingItem> = this
    .groupBy { transaction ->
        SpendingItemGroupKey(
            title = normalizedTransactionTitleKey(transaction.title, transaction.category, transaction.type),
            category = transaction.category,
            type = transaction.type,
        )
    }
    .values
    .map { transactions ->
        val latestTransaction = transactions.maxWithOrNull(
            compareBy<BudgetTransaction> { it.transactionDate }.thenBy { it.transactionId }
        ) ?: transactions.first()
        SpendingItem(
            imagePath = usableImagePath(
                latestTransaction.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath }
            ),
            name = resolvedTransactionTitle(latestTransaction.title, latestTransaction.category, latestTransaction.type),
            date = latestTransaction.transactionDate,
            totalCost = formatCompactCurrencyIraqiDinar(transactions.sumOf { it.amount }),
            amountValue = transactions.sumOf { it.amount },
            type = latestTransaction.type,
            category = latestTransaction.category,
            categoryLabel = categoryLookup[latestTransaction.category]?.name ?: latestTransaction.category.capitalized(),
            categoryIconKey = categoryLookup[latestTransaction.category]?.iconKey.orEmpty(),
            categoryColorHex = categoryLookup[latestTransaction.category]?.colorHex.orEmpty(),
            itemId = latestTransaction.transactionId,
            year = year,
            month = month,
        )
    }
    .sortedWith(compareByDescending<SpendingItem> { it.date }.thenByDescending { it.itemId })

private fun parseTransactionDate(value: String): LocalDate =
    LocalDate.parse(value)

private fun bucketDateFor(
    transactionDate: LocalDate,
    granularity: DetailGroupGranularity,
): LocalDate = when (granularity) {
    DetailGroupGranularity.DAY -> transactionDate
    DetailGroupGranularity.MONTH -> transactionDate.withDayOfMonth(1)
}

private fun LocalDate.toGroupLabel(granularity: DetailGroupGranularity): String = when (granularity) {
    DetailGroupGranularity.DAY -> format(DAY_GROUP_FORMATTER)
    DetailGroupGranularity.MONTH -> format(MONTH_GROUP_FORMATTER)
}

private val DAY_GROUP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

private val MONTH_GROUP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
