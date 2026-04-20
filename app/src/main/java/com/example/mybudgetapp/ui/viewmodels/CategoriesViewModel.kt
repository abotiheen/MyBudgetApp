package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.TRANSACTION_TYPE_INCOME
import com.example.mybudgetapp.ui.widgets.categoryColorChoices
import com.example.mybudgetapp.ui.widgets.categoryIconChoices
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val itemRepository: ItemRepository,
) : ViewModel() {

    private val allCategories: StateFlow<List<BudgetCategory>> = itemRepository
        .getAllCategories(includeArchived = true)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList(),
        )

    val uiState: StateFlow<CategoriesUiState> = allCategories
        .map { categories ->
            val activeCategories = categories.filterNot { it.isArchived }
            val archivedCategories = categories.filter { it.isArchived }
            CategoriesUiState(
                expenseCategories = activeCategories.filter { it.type == TRANSACTION_TYPE_EXPENSE },
                incomeCategories = activeCategories.filter { it.type == TRANSACTION_TYPE_INCOME },
                archivedCategories = archivedCategories,
            )
        }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = CategoriesUiState(),
    )

    fun addCategory(draft: CategoryDraft, onResult: (CategorySaveResult) -> Unit) {
        viewModelScope.launch {
            val normalizedName = draft.name.trim()
            if (normalizedName.isBlank()) {
                onResult(CategorySaveResult.Invalid("Name is required."))
                return@launch
            }

            val existingCategories = allCategories.value
            val normalizedNameKey = normalizedName.lowercase()
            if (existingCategories.any {
                    !it.isArchived &&
                        it.type == draft.type &&
                        it.name.trim().lowercase() == normalizedNameKey
                }
            ) {
                onResult(CategorySaveResult.Invalid("Category name already exists."))
                return@launch
            }

            val categoryKey = uniqueCategoryKey(normalizedName, existingCategories.map { it.categoryKey }.toSet())
            itemRepository.insertCategory(
                BudgetCategory(
                    categoryKey = categoryKey,
                    name = normalizedName,
                    type = draft.type,
                    iconKey = draft.iconKey.ifBlank { categoryIconChoices.first().key },
                    colorHex = draft.colorHex.ifBlank { categoryColorChoices.first() },
                    isDefault = false,
                    sortOrder = existingCategories.count { it.type == draft.type },
                )
            )
            onResult(CategorySaveResult.Saved)
        }
    }

    fun archiveCategory(category: BudgetCategory, onResult: (CategoryActionResult) -> Unit) {
        viewModelScope.launch {
            if (category.isDefault) {
                onResult(CategoryActionResult.Invalid("Default categories cannot be archived."))
                return@launch
            }
            itemRepository.archiveCategory(category.categoryKey)
            onResult(CategoryActionResult.Success("Category archived"))
        }
    }

    private fun uniqueCategoryKey(name: String, usedKeys: Set<String>): String {
        val baseKey = name
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "category" }

        if (baseKey !in usedKeys) return baseKey

        var suffix = 2
        while ("${baseKey}_$suffix" in usedKeys) {
            suffix += 1
        }
        return "${baseKey}_$suffix"
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class CategoriesUiState(
    val expenseCategories: List<BudgetCategory> = emptyList(),
    val incomeCategories: List<BudgetCategory> = emptyList(),
    val archivedCategories: List<BudgetCategory> = emptyList(),
)

data class CategoryDraft(
    val name: String = "",
    val type: String = TRANSACTION_TYPE_EXPENSE,
    val iconKey: String = "cookie",
    val colorHex: String = "#5EBB4A",
)

sealed interface CategorySaveResult {
    data object Saved : CategorySaveResult
    data class Invalid(val message: String) : CategorySaveResult
}

sealed interface CategoryActionResult {
    data class Success(val message: String) : CategoryActionResult
    data class Invalid(val message: String) : CategoryActionResult
}
