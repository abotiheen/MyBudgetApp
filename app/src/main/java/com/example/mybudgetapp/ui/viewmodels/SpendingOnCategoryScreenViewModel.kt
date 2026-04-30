package com.example.mybudgetapp.ui.viewmodels

import android.content.Context
import android.widget.Toast
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
import com.example.mybudgetapp.ui.screens.SpendingOnCategoryDestination
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

class SpendingOnCategoryScreenViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentMonthValue: Int = checkNotNull(savedStateHandle[SpendingOnCategoryDestination.month])
    private val currentYear: Int = checkNotNull(savedStateHandle[SpendingOnCategoryDestination.year])
    private val category: String = checkNotNull(savedStateHandle[SpendingOnCategoryDestination.category])
    private val date: LocalDate = LocalDate.now()
    private var isDeleteDialogVisible = MutableStateFlow(false)

    var uiState: StateFlow<SpendingOnCategoryUiState> = combine(
        itemRepository.getTransactionsByCategory(month = currentMonthValue, year = currentYear, category = category),
        itemRepository.getTotalSpendingOnCategory(
            month = currentMonthValue,
            year = currentYear,
            category = category
        ),
        itemRepository.getTotalSpendingOverall(
            month = currentMonthValue,
            year = currentYear,
        ),
        itemRepository.getCategory(category),
    ) { itemList, totalCategory, totalSpending, categoryDetails ->
        val groupResult = itemList.toGroupedSpendingOnCategorySections(
            year = currentYear,
            month = currentMonthValue,
            granularity = DetailGroupGranularity.DAY,
            totalLabel = "Spending",
        )
        val mappedItems = groupResult.items
        val averageAmount = if (mappedItems.isEmpty()) 0.0 else totalCategory / mappedItems.size
        val biggestAmount = itemList.maxOfOrNull { it.amount } ?: 0.0
        SpendingOnCategoryUiState(
            totalSpending = formatCompactCurrencyIraqiDinar(totalSpending),
            totalCategory = formatCompactCurrencyIraqiDinar(totalCategory),
            spendingRatio = if (totalSpending == 0.0) 0f else totalCategory.toFloat() / totalSpending.toFloat(),
            itemList = mappedItems,
            isThisMonthCurrent = currentYear == date.year && currentMonthValue == date.monthValue,
            category = categoryDetails?.name ?: category.capitalized(),
            sentCategory = category,
            periodLabel = "${Month.of(currentMonthValue).name.capitalized()} $currentYear",
            transactionCount = mappedItems.size,
            averageTransaction = formatCompactCurrencyIraqiDinar(averageAmount),
            biggestTransaction = formatCompactCurrencyIraqiDinar(biggestAmount),
            isDeleteDialogVisible = isDeleteDialogVisible.value,
            categoryIconKey = categoryDetails?.iconKey.orEmpty(),
            categoryColorHex = categoryDetails?.colorHex.orEmpty(),
            groups = groupResult.groups,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = SpendingOnCategoryUiState()
    )

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            itemRepository.deleteTransactionWithId(itemId)
        }
    }

    fun displayConfirmDelete(isIt: Boolean, context: Context) {
        isDeleteDialogVisible.value = isIt
        Toast.makeText(context, "this is ${uiState.value.isDeleteDialogVisible} but $isIt", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class SpendingOnCategoryUiState(
    val isDeleteDialogVisible: Boolean = false,
    val isThisMonthCurrent: Boolean = true,
    val category: String = "",
    val sentCategory: String = "",
    val periodLabel: String = "",
    val totalSpending: String = "",
    val totalCategory: String = "",
    val spendingRatio: Float = 0f,
    val transactionCount: Int = 0,
    val averageTransaction: String = "",
    val biggestTransaction: String = "",
    val itemList: List<SpendingOnCategoryItem> = listOf(),
    val categoryIconKey: String = "",
    val categoryColorHex: String = "",
    val groups: List<DetailGroupUi<SpendingOnCategoryItem>> = listOf(),
)

data class SpendingOnCategoryItem(
    val imagePath: String? = null,
    val name: String = "",
    val date: String = "",
    val totalCost: String = "",
    val amountValue: Double = 0.0,
    val itemId: Long = 0,
    val category: String = "",
    val type: String = "",
    val year: Int = 0,
    val month: Int = 0,
)

data class SpendingOnCategoryGroupResult(
    val items: List<SpendingOnCategoryItem>,
    val groups: List<DetailGroupUi<SpendingOnCategoryItem>>,
)

private data class SpendingOnCategoryBucketGroupKey(
    val bucketDate: LocalDate,
    val title: String,
    val category: String,
    val type: String,
)

fun List<BudgetTransaction>.toGroupedSpendingOnCategorySections(
    year: Int,
    month: Int,
    granularity: DetailGroupGranularity,
    totalLabel: String,
): SpendingOnCategoryGroupResult {
    val groupedItems = this
        .groupBy { transaction ->
            val transactionDate = parseCategoryTransactionDate(transaction.transactionDate)
            SpendingOnCategoryBucketGroupKey(
                bucketDate = categoryBucketDateFor(transactionDate, granularity),
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
            SpendingOnCategoryItem(
                itemId = latestTransaction.transactionId,
                imagePath = usableImagePath(
                    latestTransaction.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath }
                ),
                name = resolvedTransactionTitle(latestTransaction.title, latestTransaction.category, latestTransaction.type),
                date = latestTransaction.transactionDate,
                totalCost = formatCompactCurrencyIraqiDinar(transactions.sumOf { it.amount }),
                amountValue = transactions.sumOf { it.amount },
                category = latestTransaction.category,
                type = latestTransaction.type,
                year = year,
                month = month,
            )
        }
        .sortedWith(compareByDescending<SpendingOnCategoryItem> { it.date }.thenByDescending { it.itemId })

    val groups = groupedItems
        .groupBy { categoryBucketDateFor(parseCategoryTransactionDate(it.date), granularity) }
        .toList()
        .sortedByDescending { (bucketDate, _) -> bucketDate }
        .map { (bucketDate, items) ->
            DetailGroupUi(
                key = "${granularity.name.lowercase(Locale.ROOT)}-$bucketDate",
                label = bucketDate.toCategoryGroupLabel(granularity),
                displayTotal = formatCompactCurrencyIraqiDinar(items.sumOf { it.amountValue }),
                totalLabel = totalLabel,
                items = items.sortedWith(compareByDescending<SpendingOnCategoryItem> { it.date }.thenByDescending { it.itemId }),
            )
        }

    return SpendingOnCategoryGroupResult(
        items = groupedItems,
        groups = groups,
    )
}

fun List<BudgetTransaction>.toGroupedSpendingOnCategoryItems(
    year: Int,
    month: Int,
): List<SpendingOnCategoryItem> = this
    .groupBy { transaction ->
        normalizedTransactionTitleKey(transaction.title, transaction.category, transaction.type)
    }
    .values
    .map { transactions ->
        val latestTransaction = transactions.maxWithOrNull(
            compareBy<BudgetTransaction> { it.transactionDate }.thenBy { it.transactionId }
        ) ?: transactions.first()
        SpendingOnCategoryItem(
            itemId = latestTransaction.transactionId,
            imagePath = usableImagePath(
                latestTransaction.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath }
            ),
            name = resolvedTransactionTitle(latestTransaction.title, latestTransaction.category, latestTransaction.type),
            date = latestTransaction.transactionDate,
            totalCost = formatCompactCurrencyIraqiDinar(transactions.sumOf { it.amount }),
            amountValue = transactions.sumOf { it.amount },
            category = latestTransaction.category,
            type = latestTransaction.type,
            year = year,
            month = month,
        )
    }
    .sortedWith(compareByDescending<SpendingOnCategoryItem> { it.date }.thenByDescending { it.itemId })

private fun parseCategoryTransactionDate(value: String): LocalDate =
    LocalDate.parse(value)

private fun categoryBucketDateFor(
    transactionDate: LocalDate,
    granularity: DetailGroupGranularity,
): LocalDate = when (granularity) {
    DetailGroupGranularity.DAY -> transactionDate
    DetailGroupGranularity.MONTH -> transactionDate.withDayOfMonth(1)
}

private fun LocalDate.toCategoryGroupLabel(granularity: DetailGroupGranularity): String = when (granularity) {
    DetailGroupGranularity.DAY -> format(CATEGORY_DAY_GROUP_FORMATTER)
    DetailGroupGranularity.MONTH -> format(CATEGORY_MONTH_GROUP_FORMATTER)
}

private val CATEGORY_DAY_GROUP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

private val CATEGORY_MONTH_GROUP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
