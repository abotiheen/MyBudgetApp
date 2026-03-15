package com.example.mybudgetapp.ui.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.displayTitle
import com.example.mybudgetapp.ui.screens.SpendingOnCategoryDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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
        )
    ) { itemList, totalCategory, totalSpending ->
        SpendingOnCategoryUiState(
            totalSpending = formatCurrencyIraqiDinar(totalSpending),
            totalCategory = formatCurrencyIraqiDinar(totalCategory),
            spendingRatio = if (totalSpending == 0.0) 0f else totalCategory.toFloat() / totalSpending.toFloat(),
            itemList = itemList.map { it.toSpendingOnCategoryItem() },
            isThisMonthCurrent = currentYear == date.year && currentMonthValue == date.monthValue,
            category = category.capitalized(),
            sentCategory = category,
            isDeleteDialogVisible = isDeleteDialogVisible.value
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
    val totalSpending: String = "",
    val totalCategory: String = "",
    val spendingRatio: Float = 0f,
    val itemList: List<SpendingOnCategoryItem> = listOf()
)

data class SpendingOnCategoryItem(
    val imagePath: String? = null,
    val name: String = "",
    val date: String = "",
    val totalCost: String = "",
    val itemId: Long = 0
)

fun BudgetTransaction.toSpendingOnCategoryItem(): SpendingOnCategoryItem =
    SpendingOnCategoryItem(
        itemId = transactionId,
        imagePath = picturePath,
        name = displayTitle(),
        date = transactionDate,
        totalCost = formatCurrencyIraqiDinar(amount)
    )
