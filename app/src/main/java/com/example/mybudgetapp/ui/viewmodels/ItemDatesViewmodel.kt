package com.example.mybudgetapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.data.usableImagePath
import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_INCOME
import com.example.mybudgetapp.database.resolvedTransactionTitle
import com.example.mybudgetapp.ui.screens.ItemDatesScreenNavigationDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ItemDatesViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val title: String = Uri.decode(checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.title]))
    private val initialCategory: String = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.category])
    private val type: String = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.type])
    private val year: Int = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.year])
    private val month: Int = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.month])

    private val selectedCategoryKey = MutableStateFlow(initialCategory)
    private val isUpdatingCategory = MutableStateFlow(false)

    private val transactionsFlow = selectedCategoryKey.flatMapLatest { categoryKey ->
        if (month == 0) {
            itemRepository.getTransactionsForItemInYear(
                title = title,
                category = categoryKey,
                type = type,
                year = year,
            )
        } else {
            itemRepository.getTransactionsForItemInMonth(
                title = title,
                category = categoryKey,
                type = type,
                year = year,
                month = month,
            )
        }
    }

    val availableCategories: StateFlow<List<BudgetCategory>> = itemRepository
        .getAllCategories(includeArchived = true)
        .map { categories ->
            categories.filter { it.type == type }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList(),
        )

    val uiState: StateFlow<ItemDatesUiState> = combine(
        transactionsFlow,
        selectedCategoryKey.flatMapLatest { itemRepository.getCategory(it) },
        isUpdatingCategory,
    ) { transactions, categoryDetails, updatingCategory ->
        val latestTransaction = transactions.firstOrNull()
        val totalAmount = transactions.sumOf { it.amount }
        val resolvedCategoryKey = latestTransaction?.category ?: selectedCategoryKey.value
        val displayTitle = latestTransaction?.let {
            resolvedTransactionTitle(it.title, it.category, it.type)
        } ?: title
        val history = transactions.map { it.toItemWithDates() }
        val latestDate = latestTransaction?.transactionDate.orEmpty()

        ItemDatesUiState(
            itemDatesList = history,
            category = resolvedCategoryKey,
            categoryLabel = categoryDetails?.name ?: categoryLabel(resolvedCategoryKey),
            name = displayTitle,
            date = latestDate,
            amount = formatCurrencyIraqiDinar(totalAmount),
            typeLabel = if ((latestTransaction?.type ?: type) == TRANSACTION_TYPE_INCOME) "Income" else "Expense",
            picturePath = usableImagePath(
                latestTransaction?.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath }
            ),
            historyCount = history.size,
            categoryIconKey = categoryDetails?.iconKey.orEmpty(),
            categoryColorHex = categoryDetails?.colorHex.orEmpty(),
            isUpdatingCategory = updatingCategory,
            canChangeCategory = history.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ItemDatesUiState()
    )

    fun changeCategory(newCategory: String, onResult: (CategoryChangeResult) -> Unit) {
        val currentCategory = selectedCategoryKey.value
        if (newCategory.isBlank()) {
            onResult(CategoryChangeResult.Invalid("Choose a category first."))
            return
        }
        if (newCategory == currentCategory) {
            onResult(CategoryChangeResult.Invalid("This item is already using that category."))
            return
        }

        viewModelScope.launch {
            isUpdatingCategory.value = true
            runCatching {
                if (month == 0) {
                    itemRepository.updateTransactionCategoryForItemInYear(
                        title = title,
                        oldCategory = currentCategory,
                        newCategory = newCategory,
                        type = type,
                        year = year,
                    )
                } else {
                    itemRepository.updateTransactionCategoryForItemInMonth(
                        title = title,
                        oldCategory = currentCategory,
                        newCategory = newCategory,
                        type = type,
                        year = year,
                        month = month,
                    )
                }
                selectedCategoryKey.value = newCategory
            }.onSuccess {
                onResult(CategoryChangeResult.Success("Category updated."))
            }.onFailure { error ->
                onResult(
                    CategoryChangeResult.Invalid(
                        error.message ?: "Unable to update the category right now."
                    )
                )
            }
            isUpdatingCategory.value = false
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ItemDatesUiState(
    val itemDatesList: List<ItemWIthDates> = listOf(),
    val category: String = "",
    val categoryLabel: String = "",
    val picturePath: String? = null,
    val date: String = "",
    val amount: String = "",
    val typeLabel: String = "",
    val name: String = "",
    val historyCount: Int = 0,
    val categoryIconKey: String = "",
    val categoryColorHex: String = "",
    val isUpdatingCategory: Boolean = false,
    val canChangeCategory: Boolean = false,
)

data class ItemWIthDates(
    val cost: String = "",
    val date: String = "",
)

sealed interface CategoryChangeResult {
    data class Success(val message: String) : CategoryChangeResult
    data class Invalid(val message: String) : CategoryChangeResult
}

fun BudgetTransaction.toItemWithDates(): ItemWIthDates =
    ItemWIthDates(
        cost = formatCurrencyIraqiDinar(amount),
        date = transactionDate
    )
