package com.example.mybudgetapp.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AddingItemScreenViewModel
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.ItemDetails
import com.example.mybudgetapp.ui.viewmodels.RecentTemplateUiModel
import com.example.mybudgetapp.ui.viewmodels.SaveEntryResult
import com.example.mybudgetapp.ui.viewmodels.SpendingItemDetailsUiState
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.SectionHeading
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
    val iconRes: Int,
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
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetEntry()
            onDismissRequest()
        },
        modifier = modifier,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        AddingItemBody(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 12.dp),
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = stringResource(id = R.string.adding_item_screen_title, "Entry"),
                navigateBack = navigateBack,
            )
        },
    ) { paddingValues ->
        BudgetBackdrop(modifier = Modifier.padding(paddingValues)) {
            AddingItemBody(
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
        onResult = { uri: Uri? -> onImageSelected(context, uri) },
    )

    val quickCategories = remember(previousCategory) {
        when (previousCategory) {
            "income" -> listOf(QuickCategoryChip(label = "Income", value = "income", iconRes = R.drawable.baseline_attach_money_25))
            "food" -> listOf(QuickCategoryChip(label = "Food", value = "food", iconRes = R.drawable.baseline_fastfood_24))
            "transportation" -> listOf(QuickCategoryChip(label = "Transit", value = "transportation", iconRes = R.drawable.baseline_directions_transit_24))
            "others" -> listOf(QuickCategoryChip(label = "Others", value = "others", iconRes = R.drawable.baseline_cookie_24))
            else -> listOf(
                QuickCategoryChip(label = "Food", value = "food", iconRes = R.drawable.baseline_fastfood_24),
                QuickCategoryChip(label = "Transit", value = "transportation", iconRes = R.drawable.baseline_directions_transit_24),
                QuickCategoryChip(label = "Others", value = "others", iconRes = R.drawable.baseline_cookie_24),
                QuickCategoryChip(label = "Income", value = "income", iconRes = R.drawable.baseline_attach_money_25),
            )
        }
    }

    val accentColor = when (itemDetails.category.ifBlank { previousCategory }) {
        "food" -> BudgetTheme.extendedColors.food
        "transportation" -> BudgetTheme.extendedColors.transit
        "others" -> BudgetTheme.extendedColors.others
        "income" -> BudgetTheme.extendedColors.income
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
    ) {
        EntryHeroCard(
            accentColor = accentColor,
            itemDetails = itemDetails,
            amountFocusRequester = amountFocusRequester,
            onItemValueChange = onItemValueChange,
            previousCategory = previousCategory,
            isUploadSuccessful = isUploadSuccessful,
        )

        SectionHeading(
            title = "Choose a lane",
            subtitle = "Make the entry feel obvious before you type anything else.",
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            quickCategories.forEach { quickCategory ->
                QuickCategoryCard(
                    label = quickCategory.label,
                    iconRes = quickCategory.iconRes,
                    selected = itemDetails.category == quickCategory.value,
                    accentColor = accentColor,
                    onClick = { onItemValueChange(itemDetails.copy(category = quickCategory.value)) },
                    modifier = Modifier.fillMaxWidth(0.48f),
                )
            }
        }

        if (recentTemplates.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm)) {
                SectionHeading(
                    title = "Use a recent pattern",
                    subtitle = "One tap can preload title, amount, and category.",
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
                ) {
                    recentTemplates.forEach { template ->
                        RecentTemplateCard(
                            template = template,
                            onClick = { onTemplateSelected(template) },
                        )
                    }
                }
            }
        }

        if (previousCategory != "income") {
            ReceiptAttachmentCard(
                accentColor = accentColor,
                isUploadSuccessful = isUploadSuccessful,
                onPickPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )
        }

        SaveActionsCard(
            isEntryValid = isEntryValid,
            saveItem = saveItem,
        )
    }
}

@Composable
private fun EntryHeroCard(
    accentColor: androidx.compose.ui.graphics.Color,
    itemDetails: ItemDetails,
    amountFocusRequester: FocusRequester,
    onItemValueChange: (ItemDetails) -> Unit,
    previousCategory: String,
    isUploadSuccessful: Boolean,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level3),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
        ) {
            SectionHeading(
                title = "New entry",
                subtitle = "Lead with the amount, then add just enough context.",
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.xs),
            ) {
                EntryStatusChip(
                    label = if (previousCategory == "all") "Flexible context" else previousCategory.replaceFirstChar { it.uppercase() },
                    accentColor = accentColor,
                )
                if (isUploadSuccessful) {
                    EntryStatusChip(
                        label = "Photo ready",
                        accentColor = BudgetTheme.extendedColors.income,
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            ) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = itemDetails.cost,
                    onValueChange = { onItemValueChange(itemDetails.copy(cost = it)) },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.quick_add_amount_placeholder),
                            style = MaterialTheme.typography.displayLarge.copy(textAlign = TextAlign.Center),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(amountFocusRequester),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.displayLarge.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                )
                OutlinedTextField(
                    value = itemDetails.name,
                    onValueChange = { onItemValueChange(itemDetails.copy(name = it)) },
                    label = { Text(text = stringResource(id = R.string.quick_add_title_label)) },
                    placeholder = { Text(text = stringResource(id = R.string.quick_add_title_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                )
            }
        }
    }
}

@Composable
private fun EntryStatusChip(
    label: String,
    accentColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        color = accentColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = accentColor,
        )
    }
}

@Composable
private fun QuickCategoryCard(
    label: String,
    iconRes: Int,
    selected: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) accentColor else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            Surface(
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
                } else {
                    accentColor.copy(alpha = 0.12f)
                },
                shape = CircleShape,
            ) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else accentColor,
                    )
                }
            }
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun RecentTemplateCard(
    template: RecentTemplateUiModel,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${template.cost} • ${template.category.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReceiptAttachmentCard(
    accentColor: androidx.compose.ui.graphics.Color,
    isUploadSuccessful: Boolean,
    onPickPhoto: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = if (isUploadSuccessful) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = accentColor,
                    )
                }
                Column {
                    Text(
                        text = stringResource(id = R.string.quick_add_photo_label),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = if (isUploadSuccessful) {
                            stringResource(id = R.string.quick_add_photo_selected)
                        } else {
                            "Optional, but useful for receipts and reminders."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedButton(
                onClick = onPickPhoto,
                shape = RoundedCornerShape(BudgetTheme.radii.pill),
            ) {
                Text(text = stringResource(id = R.string.quick_add_photo_action))
            }
        }
    }
}

@Composable
private fun SaveActionsCard(
    isEntryValid: Boolean,
    saveItem: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            Button(
                onClick = { saveItem(false) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEntryValid,
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Text(text = stringResource(id = R.string.quick_add_save))
            }
            OutlinedButton(
                onClick = { saveItem(true) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEntryValid,
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Text(text = stringResource(id = R.string.quick_add_save_and_continue))
            }
        }
    }
}
