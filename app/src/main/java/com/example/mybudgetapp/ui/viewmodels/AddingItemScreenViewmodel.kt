package com.example.mybudgetapp.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.RecentEntryTemplate
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.TRANSACTION_TYPE_INCOME
import com.example.mybudgetapp.database.defaultTransactionTitle
import com.example.mybudgetapp.ui.screens.AddingItemDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class AddingItemScreenViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val previousCategory: String = savedStateHandle[AddingItemDestination.category] ?: "all"
    private var localIsUploadSuccessful = false

    var uiState by mutableStateOf(
        SpendingItemDetailsUiState(
            previousCategory = previousCategory,
            itemDetails = if (previousCategory != "all") {
                ItemDetails(category = previousCategory)
            } else {
                ItemDetails()
            }
        )
    )
        private set

    val recentTemplates: StateFlow<List<RecentTemplateUiModel>> = itemRepository
        .getRecentEntryTemplates(6)
        .map { templates ->
            templates
                .filter(::matchesCurrentEntryContext)
                .distinctBy { (it.title ?: defaultTransactionTitle(it.category, it.type)).lowercase(Locale.ROOT) to it.category }
                .map { it.toUiModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList(),
        )

    fun updateUiState(itemDetails: ItemDetails) {
        val normalizedCost = itemDetails.cost.replace(regex = Regex("[ ,-]"), replacement = "")
        uiState = SpendingItemDetailsUiState(
            itemDetails = itemDetails.copy(cost = normalizedCost),
            isEntryValid = validateInput(itemDetails.copy(cost = normalizedCost)),
            isUploadSuccessful = localIsUploadSuccessful,
            previousCategory = previousCategory,
        )
    }

    fun applyTemplate(template: RecentTemplateUiModel) {
        updateUiState(
            uiState.itemDetails.copy(
                name = template.name,
                category = template.category,
                cost = template.cost,
            )
        )
    }

    fun onImageSelected(context: Context, uri: Uri?) {
        val imagePath = getImagePath(context, uri) { isSuccess ->
            localIsUploadSuccessful = isSuccess
        }

        updateUiState(uiState.itemDetails.copy(imagePath = imagePath))
        uiState = uiState.copy(isUploadSuccessful = localIsUploadSuccessful)
    }

    suspend fun saveEntry(stayOnScreen: Boolean): SaveEntryResult {
        val currentDate = LocalDate.now()
        val resolvedCategory = resolveCategory(uiState.itemDetails.category)
        val detailsToSave = uiState.itemDetails.copy(category = resolvedCategory)

        if (!validateInput(detailsToSave)) {
            return SaveEntryResult.Invalid
        }

        itemRepository.insertTransaction(detailsToSave.toTransaction(currentDate.toString()))

        if (stayOnScreen) {
            resetForNextEntry(detailsToSave.category)
            return SaveEntryResult.SavedAndReadyForNext
        }

        return SaveEntryResult.SavedAndClose
    }

    fun resetEntry() {
        localIsUploadSuccessful = false
        uiState = SpendingItemDetailsUiState(
            previousCategory = previousCategory,
            itemDetails = if (previousCategory != "all") {
                ItemDetails(category = previousCategory)
            } else {
                ItemDetails()
            },
            isEntryValid = false,
            isUploadSuccessful = false,
        )
    }

    private fun resetForNextEntry(lastUsedCategory: String) {
        localIsUploadSuccessful = false
        uiState = SpendingItemDetailsUiState(
            previousCategory = previousCategory,
            itemDetails = ItemDetails(
                category = when (previousCategory) {
                    "all" -> lastUsedCategory
                    else -> previousCategory
                }
            ),
            isEntryValid = false,
            isUploadSuccessful = false,
        )
    }

    private fun resolveCategory(currentCategory: String): String {
        return when {
            previousCategory != "all" -> previousCategory
            currentCategory.isBlank() -> recentTemplates.value.firstOrNull()?.category ?: "food"
            else -> currentCategory
        }
    }

    private fun matchesCurrentEntryContext(template: RecentEntryTemplate): Boolean {
        return when (previousCategory) {
            "all" -> template.type != TRANSACTION_TYPE_INCOME
            else -> template.category == previousCategory
        }
    }

    private fun getImagePath(context: Context, uri: Uri?, callback: (Boolean) -> Unit): String? {
        if (uri == null) {
            callback(false)
            return null
        }
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val externalStorageDirectory = context.getExternalFilesDir(null)
            if (externalStorageDirectory == null) {
                callback(false)
                return null
            }
            val appSpecificDirectory = File(externalStorageDirectory, "YourAppImages")
            appSpecificDirectory.mkdirs()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "image_$timeStamp.jpg"
            val imageFile = File(appSpecificDirectory, fileName)
            FileOutputStream(imageFile).use { fileOutputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream)
            }
            inputStream?.close()
            callback(true)
            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            callback(false)
            null
        }
    }

    private fun validateInput(itemDetails: ItemDetails = uiState.itemDetails): Boolean {
        return itemDetails.cost.isNotBlank() && itemDetails.category.isNotBlank()
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class SpendingItemDetailsUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false,
    val previousCategory: String = "",
    val isUploadSuccessful: Boolean = false,
)

data class ItemDetails(
    val id: Long = 0,
    val imagePath: String? = null,
    val name: String = "",
    val cost: String = "",
    val category: String = "",
)

data class RecentTemplateUiModel(
    val name: String,
    val category: String,
    val cost: String,
)

enum class SaveEntryResult {
    Invalid,
    SavedAndClose,
    SavedAndReadyForNext,
}

fun ItemDetails.toTransaction(date: String): BudgetTransaction = BudgetTransaction(
    transactionId = id,
    title = name.trim().takeIf { it.isNotEmpty() },
    amount = cost.toDouble(),
    category = category,
    type = if (category == "income") TRANSACTION_TYPE_INCOME else TRANSACTION_TYPE_EXPENSE,
    transactionDate = date,
    picturePath = imagePath,
)

private fun RecentEntryTemplate.toUiModel(): RecentTemplateUiModel = RecentTemplateUiModel(
    name = title?.takeIf { it.isNotBlank() } ?: defaultTransactionTitle(category, type),
    category = category,
    cost = amount.toString(),
)
