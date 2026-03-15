package com.example.mybudgetapp.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.dmSans
import com.example.mybudgetapp.ui.viewmodels.AddingItemScreenViewModel
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.ItemDetails
import com.example.mybudgetapp.ui.viewmodels.RecentTemplateUiModel
import com.example.mybudgetapp.ui.viewmodels.SaveEntryResult
import com.example.mybudgetapp.ui.viewmodels.SpendingItemDetailsUiState
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import kotlinx.coroutines.launch

object AddingItemDestination : NavigationDestination {
    override val route = "AddingItem"
    override val titleRes = R.string.adding_item_screen
    const val category = "category"
    val routeWithArgs = "${AddingItemDestination.route}/{$category}"
}

private data class QuickCategoryChip(
    val label: String,
    val value: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    modifier: Modifier = Modifier,
    viewModelKey: String,
    onDismissRequest: () -> Unit,
) {
    val viewModel: AddingItemScreenViewModel = viewModel(
        key = viewModelKey,
        factory = AppViewModelProvider.Factory,
    )
    val uiState: SpendingItemDetailsUiState = viewModel.uiState
    val recentTemplates by viewModel.recentTemplates.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetEntry()
            onDismissRequest()
        },
        modifier = modifier,
        sheetState = sheetState,
    ) {
        AddingItemBody(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 16.dp),
            onImageSelected = { entryContext: Context, uri: Uri? ->
                viewModel.onImageSelected(context = entryContext, uri = uri)
            },
            itemDetails = uiState.itemDetails,
            recentTemplates = recentTemplates,
            onTemplateSelected = viewModel::applyTemplate,
            saveItem = { stayOnScreen ->
                coroutineScope.launch {
                    when (viewModel.saveEntry(stayOnScreen = stayOnScreen)) {
                        SaveEntryResult.Invalid -> {
                            Toast.makeText(context, R.string.quick_add_validation_error, Toast.LENGTH_SHORT).show()
                        }

                        SaveEntryResult.SavedAndClose -> {
                            Toast.makeText(context, R.string.item_added, Toast.LENGTH_SHORT).show()
                            viewModel.resetEntry()
                            onDismissRequest()
                        }

                        SaveEntryResult.SavedAndReadyForNext -> {
                            Toast.makeText(context, R.string.item_saved_ready_for_next, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onItemValueChange = viewModel::updateUiState,
            isUploadSuccessful = uiState.isUploadSuccessful,
            isEntryValid = uiState.isEntryValid,
            previousCategory = uiState.previousCategory,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingItem(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    val viewModel: AddingItemScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState: SpendingItemDetailsUiState = viewModel.uiState
    val recentTemplates by viewModel.recentTemplates.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = stringResource(
                    id = R.string.adding_item_screen_title,
                    when (uiState.previousCategory) {
                        "food" -> "Food"
                        "transportation" -> "Transit"
                        "others" -> "Others"
                        "income" -> "Income"
                        else -> "Quick"
                    }
                ),
                navigateBack = navigateBack,
            )
        }
    ) { paddingValues ->
        AddingItemBody(
            modifier = Modifier.padding(paddingValues),
            onImageSelected = { entryContext: Context, uri: Uri? ->
                viewModel.onImageSelected(context = entryContext, uri = uri)
            },
            itemDetails = uiState.itemDetails,
            recentTemplates = recentTemplates,
            onTemplateSelected = viewModel::applyTemplate,
            saveItem = { stayOnScreen ->
                coroutineScope.launch {
                    when (viewModel.saveEntry(stayOnScreen = stayOnScreen)) {
                        SaveEntryResult.Invalid -> {
                            Toast.makeText(context, R.string.quick_add_validation_error, Toast.LENGTH_SHORT).show()
                        }

                        SaveEntryResult.SavedAndClose -> {
                            Toast.makeText(context, R.string.item_added, Toast.LENGTH_SHORT).show()
                            navigateBack()
                        }

                        SaveEntryResult.SavedAndReadyForNext -> {
                            Toast.makeText(context, R.string.item_saved_ready_for_next, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onItemValueChange = viewModel::updateUiState,
            isUploadSuccessful = uiState.isUploadSuccessful,
            isEntryValid = uiState.isEntryValid,
            previousCategory = uiState.previousCategory,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddingItemBody(
    modifier: Modifier = Modifier,
    onImageSelected: (context: Context, uri: Uri?) -> Unit,
    onItemValueChange: (ItemDetails) -> Unit,
    itemDetails: ItemDetails,
    recentTemplates: List<RecentTemplateUiModel>,
    onTemplateSelected: (RecentTemplateUiModel) -> Unit,
    isUploadSuccessful: Boolean,
    isEntryValid: Boolean,
    saveItem: (Boolean) -> Unit,
    previousCategory: String,
) {
    val context = LocalContext.current
    val amountFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> onImageSelected(context, uri) }
    )

    val quickCategories = remember(previousCategory) {
        when (previousCategory) {
            "income" -> listOf(QuickCategoryChip(label = "Income", value = "income"))
            "food" -> listOf(QuickCategoryChip(label = "Food", value = "food"))
            "transportation" -> listOf(QuickCategoryChip(label = "Transit", value = "transportation"))
            "others" -> listOf(QuickCategoryChip(label = "Others", value = "others"))
            else -> listOf(
                QuickCategoryChip(label = "Food", value = "food"),
                QuickCategoryChip(label = "Transit", value = "transportation"),
                QuickCategoryChip(label = "Others", value = "others"),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .then(modifier),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.quick_add_title),
            fontFamily = dmSans,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(id = R.string.quick_add_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 20.dp),
        )

        OutlinedTextField(
            value = itemDetails.name,
            onValueChange = { onItemValueChange(itemDetails.copy(name = it)) },
            label = { Text(text = stringResource(id = R.string.quick_add_title_label)) },
            placeholder = { Text(text = stringResource(id = R.string.quick_add_title_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            value = itemDetails.cost,
            onValueChange = { onItemValueChange(itemDetails.copy(cost = it)) },
            label = { Text(text = stringResource(id = R.string.quick_add_amount_label)) },
            placeholder = { Text(text = stringResource(id = R.string.quick_add_amount_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(amountFocusRequester)
                .padding(top = 14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(id = R.string.quick_add_category_label),
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                quickCategories.forEach { quickCategory ->
                    FilterChip(
                        selected = itemDetails.category == quickCategory.value,
                        onClick = {
                            onItemValueChange(itemDetails.copy(category = quickCategory.value))
                        },
                        label = { Text(quickCategory.label) },
                    )
                }
            }
        }

        if (recentTemplates.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.quick_add_recent_label),
                    fontWeight = FontWeight.Bold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    recentTemplates.forEach { template ->
                        AssistChip(
                            onClick = { onTemplateSelected(template) },
                            label = { Text(text = "${template.name} • ${template.cost}") },
                        )
                    }
                }
            }
        }

        if (previousCategory != "income") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.quick_add_photo_label),
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = if (isUploadSuccessful) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = null,
                            tint = if (isUploadSuccessful) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = if (isUploadSuccessful) {
                                stringResource(id = R.string.quick_add_photo_selected)
                            } else {
                                stringResource(id = R.string.quick_add_photo_optional)
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text(text = stringResource(id = R.string.quick_add_photo_action))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { saveItem(false) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEntryValid,
            ) {
                Text(text = stringResource(id = R.string.quick_add_save))
            }
            Button(
                onClick = { saveItem(true) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEntryValid,
            ) {
                Text(text = stringResource(id = R.string.quick_add_save_and_continue))
            }
        }
    }
}
