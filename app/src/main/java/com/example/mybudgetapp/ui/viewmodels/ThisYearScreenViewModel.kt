package com.example.mybudgetapp.ui.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.ui.widgets.DropDownItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.Month

class ThisYearScreenViewModel(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val date: LocalDate = LocalDate.now()
    private val selectedYear = MutableStateFlow(date.year)
    private val availableYears = itemRepository.getAllYears()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activeYear = combine(availableYears, selectedYear) { years, selected ->
        val normalizedYears = years.withCurrentYear(date.year)
        if (normalizedYears.isEmpty()) selected else normalizedYears.firstOrNull { it == selected } ?: normalizedYears.last()
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ThisYearScreenUiState> = combine(availableYears, activeYear) { years, active ->
        val normalizedYears = years.withCurrentYear(date.year).ifEmpty { listOf(active) }
        normalizedYears to active
    }.flatMapLatest { (years, currentYear) ->
        combine(
            itemRepository.getTotalSpendingOverallForYear(year = currentYear),
            itemRepository.getTotalIncomeOverallForYear(year = currentYear),
            itemRepository.getTotalSpendingOnCategoryForYear(year = currentYear, category = "food"),
            itemRepository.getTotalSpendingOnCategoryForYear(year = currentYear, category = "transportation"),
            itemRepository.getTotalSpendingOnCategoryForYear(year = currentYear, category = "others")
        ) { totalSpending, totalIncome, totalFood, totalTrans, totalOther ->
            val selectedIndex = years.indexOf(currentYear)
            ThisYearScreenUiState(
                totalSpendingForYear = formatCurrencyIraqiDinar(totalSpending),
                totalIncomeForYear = formatCurrencyIraqiDinar(totalIncome),
                totalSpendingOnFoodForYear = formatCurrencyIraqiDinar(totalFood),
                totalSpendingOnOthersForYear = formatCurrencyIraqiDinar(totalOther),
                totalSpendingOnTransportationForYear = formatCurrencyIraqiDinar(totalTrans),
                currentMonth = Month.of(date.monthValue).toString().capitalized(),
                selectedYear = currentYear,
                currentYear = currentYear.toString(),
                years = years.map {
                    DropDownItem(
                        title = it.toString(),
                        number = it
                    )
                },
                canNavigatePrevious = selectedIndex > 0,
                canNavigateNext = selectedIndex in 0 until years.lastIndex,
                isCurrentYear = currentYear == date.year,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ThisYearScreenUiState(
            currentMonth = Month.of(date.monthValue).toString().capitalized(),
            selectedYear = date.year,
            currentYear = date.year.toString(),
            isCurrentYear = true,
        )
    )

    fun selectYear(year: Int) {
        selectedYear.value = year
    }

    fun selectPreviousYear() {
        val years = uiState.value.years
        val currentIndex = years.indexOfFirst { it.number == uiState.value.selectedYear }
        if (currentIndex > 0) {
            selectYear(years[currentIndex - 1].number)
        }
    }

    fun selectNextYear() {
        val years = uiState.value.years
        val currentIndex = years.indexOfFirst { it.number == uiState.value.selectedYear }
        if (currentIndex in 0 until years.lastIndex) {
            selectYear(years[currentIndex + 1].number)
        }
    }

    fun jumpToCurrentYear() {
        selectedYear.value = date.year
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

private fun List<Int>.withCurrentYear(currentYear: Int): List<Int> {
    return (this + currentYear).distinct().sorted()
}

data class ThisYearScreenUiState(
    val currentMonth: String = "",
    val selectedYear: Int = 0,
    val currentYear: String = "",
    val totalSpendingForYear: String = "",
    val totalIncomeForYear: String = "",
    val totalSpendingOnFoodForYear: String = "",
    val totalSpendingOnTransportationForYear: String = "",
    val totalSpendingOnOthersForYear: String = "",
    val years: List<DropDownItem> = listOf(),
    val canNavigatePrevious: Boolean = false,
    val canNavigateNext: Boolean = false,
    val isCurrentYear: Boolean = false,
    val screenItemsUiState: MutableState<ScreenItemsUiState> = mutableStateOf(ScreenItemsUiState())
)
