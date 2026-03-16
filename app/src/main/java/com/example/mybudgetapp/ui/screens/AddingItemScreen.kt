package com.example.mybudgetapp.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
import kotlinx.coroutines.delay
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

private enum class EntrySurfaceMode {
    Fullscreen,
    Sheet,
}

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
    var sheetContentVisible by remember { mutableStateOf(false) }

    fun dismissSheet() {
        coroutineScope.launch {
            sheetContentVisible = false
            delay(140)
            sheetState.hide()
            viewModel.resetEntry()
            onDismissRequest()
        }
    }

    LaunchedEffect(Unit) {
        sheetContentVisible = true
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSheet() },
        modifier = modifier,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        AnimatedVisibility(
            visible = sheetContentVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 280),
                    initialOffsetY = { it / 10 },
                ),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)) +
                slideOutVertically(
                    animationSpec = tween(durationMillis = 200),
                    targetOffsetY = { it / 10 },
                ),
        ) {
            AddingItemBody(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(bottom = 12.dp),
                surfaceMode = EntrySurfaceMode.Sheet,
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
                                dismissSheet()
                            }

                            SaveEntryResult.SavedAndReadyForNext -> {
                                Toast.makeText(context, R.string.item_saved_ready_for_next, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onItemValueChange = viewModel::updateUiState,
                isEntryValid = uiState.isEntryValid,
                previousCategory = uiState.previousCategory,
            )
        }
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
        containerColor = Color.Transparent,
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
                surfaceMode = EntrySurfaceMode.Fullscreen,
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
                isEntryValid = uiState.isEntryValid,
                previousCategory = uiState.previousCategory,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddingItemBody(
    modifier: Modifier = Modifier,
    surfaceMode: EntrySurfaceMode,
    onImageSelected: (context: Context, uri: Uri?) -> Unit,
    onItemValueChange: (ItemDetails) -> Unit,
    itemDetails: ItemDetails,
    recentTemplates: List<RecentTemplateUiModel>,
    onTemplateSelected: (RecentTemplateUiModel) -> Unit,
    isEntryValid: Boolean,
    saveItem: (Boolean) -> Unit,
    previousCategory: String,
) {
    val context = LocalContext.current
    val amountFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> onImageSelected(context, uri) },
    )

    val quickCategories = remember(previousCategory) {
        when (previousCategory) {
            "income" -> listOf(
                QuickCategoryChip(
                    label = "Income",
                    value = "income",
                    iconRes = R.drawable.baseline_attach_money_25,
                )
            )

            "food" -> listOf(
                QuickCategoryChip(
                    label = "Food",
                    value = "food",
                    iconRes = R.drawable.baseline_fastfood_24,
                )
            )

            "transportation" -> listOf(
                QuickCategoryChip(
                    label = "Transit",
                    value = "transportation",
                    iconRes = R.drawable.baseline_directions_transit_24,
                )
            )

            "others" -> listOf(
                QuickCategoryChip(
                    label = "Others",
                    value = "others",
                    iconRes = R.drawable.baseline_cookie_24,
                )
            )

            else -> listOf(
                QuickCategoryChip(
                    label = "Food",
                    value = "food",
                    iconRes = R.drawable.baseline_fastfood_24,
                ),
                QuickCategoryChip(
                    label = "Transit",
                    value = "transportation",
                    iconRes = R.drawable.baseline_directions_transit_24,
                ),
                QuickCategoryChip(
                    label = "Others",
                    value = "others",
                    iconRes = R.drawable.baseline_cookie_24,
                ),
                QuickCategoryChip(
                    label = "Income",
                    value = "income",
                    iconRes = R.drawable.baseline_attach_money_25,
                ),
            )
        }
    }

    val activeCategory = itemDetails.category.ifBlank {
        if (previousCategory == "all") "" else previousCategory
    }
    val accentColor = when (activeCategory) {
        "food" -> BudgetTheme.extendedColors.food
        "transportation" -> BudgetTheme.extendedColors.transit
        "others" -> BudgetTheme.extendedColors.others
        "income" -> BudgetTheme.extendedColors.income
        else -> MaterialTheme.colorScheme.primary
    }
    val animatedAccentColor = animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 260),
        label = "entryAccent",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .animateContentSize(animationSpec = tween(durationMillis = 240))
            .padding(horizontal = 18.dp, vertical = if (surfaceMode == EntrySurfaceMode.Sheet) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 30)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = 320, delayMillis = 30),
                        initialOffsetY = { it / 8 },
                    ),
            ) {
                EntryHeroCard(
                    surfaceMode = surfaceMode,
                    accentColor = animatedAccentColor.value,
                    itemDetails = itemDetails,
                    amountFocusRequester = amountFocusRequester,
                    onItemValueChange = onItemValueChange,
                    activeCategory = activeCategory,
                )
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 90)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = 320, delayMillis = 90),
                        initialOffsetY = { it / 10 },
                    ),
            ) {
                EntryUtilityTray(
                    categories = quickCategories,
                    selectedCategory = itemDetails.category,
                    accentColor = animatedAccentColor.value,
                    recentTemplates = recentTemplates,
                    showPhotoAction = activeCategory != "income",
                    imagePath = itemDetails.imagePath,
                    onCategorySelected = { category ->
                        onItemValueChange(itemDetails.copy(category = category))
                    },
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    onTemplateSelected = onTemplateSelected,
                )
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 140)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 300, delayMillis = 140),
                    initialOffsetY = { it / 12 },
                ),
        ) {
            SaveDock(
                isEntryValid = isEntryValid,
                saveItem = saveItem,
                isSheet = surfaceMode == EntrySurfaceMode.Sheet,
            )
        }
    }
}

@Composable
private fun EntryHeroCard(
    surfaceMode: EntrySurfaceMode,
    accentColor: Color,
    itemDetails: ItemDetails,
    amountFocusRequester: FocusRequester,
    onItemValueChange: (ItemDetails) -> Unit,
    activeCategory: String,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.96f),
                            accentColor.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EntryBadge(
                    label = if (surfaceMode == EntrySurfaceMode.Sheet) "Quick entry" else "New entry",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    filled = false,
                )
                EntryBadge(
                    label = if (activeCategory.isBlank()) "Pick category" else activeCategory.replaceFirstChar { it.uppercase() },
                    tint = if (activeCategory.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else accentColor,
                    filled = activeCategory.isNotBlank(),
                )
            }

            Text(
                text = "Amount first. Everything else stays compact below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Column(
                    modifier = Modifier.padding(BudgetTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        EntryBadge(
                            label = "IQD",
                            tint = accentColor,
                            filled = true,
                        )
                    }
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
                        label = { Text(text = "Title") },
                        placeholder = { Text(text = "Optional note") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(BudgetTheme.radii.lg),
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryBadge(
    label: String,
    tint: Color,
    filled: Boolean,
) {
    Surface(
        color = if (filled) tint.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = tint,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EntryUtilityTray(
    categories: List<QuickCategoryChip>,
    selectedCategory: String,
    accentColor: Color,
    recentTemplates: List<RecentTemplateUiModel>,
    showPhotoAction: Boolean,
    imagePath: String?,
    onCategorySelected: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onTemplateSelected: (RecentTemplateUiModel) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier
                .padding(BudgetTheme.spacing.lg)
                .animateContentSize(animationSpec = tween(durationMillis = 220)),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            Text(
                text = "Category and extras",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                categories.forEach { category ->
                    CompactCategoryChip(
                        label = category.label,
                        iconRes = category.iconRes,
                        selected = selectedCategory == category.value,
                        accentColor = if (selectedCategory == category.value) accentColor else laneColor(category.value),
                        onClick = { onCategorySelected(category.value) },
                    )
                }

                if (showPhotoAction) {
                    UtilityActionChip(
                        label = if (imagePath != null) "Photo ready" else "Add photo",
                        tint = if (imagePath != null) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        filled = imagePath != null,
                        onClick = onPickPhoto,
                    )
                }
            }

            AnimatedVisibility(
                visible = imagePath != null && showPhotoAction,
                enter = fadeIn(animationSpec = tween(durationMillis = 180)) +
                    expandVertically(animationSpec = tween(durationMillis = 240)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 180)),
            ) {
                if (imagePath != null) {
                    AttachedPhotoPreview(
                        imagePath = imagePath,
                        accentColor = accentColor,
                    )
                }
            }

            if (recentTemplates.isNotEmpty()) {
                Text(
                    text = "Recent shortcuts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
                ) {
                    recentTemplates.forEach { template ->
                        TemplateShortcutChip(
                            template = template,
                            onClick = { onTemplateSelected(template) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactCategoryChip(
    label: String,
    iconRes: Int,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val backgroundColor = animateColorAsState(
        targetValue = if (selected) {
            accentColor.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 180),
        label = "categoryChipBackground",
    )
    val borderColor = animateColorAsState(
        targetValue = if (selected) {
            accentColor.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(durationMillis = 180),
        label = "categoryChipBorder",
    )
    val contentColor = animateColorAsState(
        targetValue = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 180),
        label = "categoryChipContent",
    )

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = backgroundColor.value,
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor.value,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor.value,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.value,
            )
        }
    }
}

@Composable
private fun UtilityActionChip(
    label: String,
    tint: Color,
    filled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (filled) tint.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
        border = BorderStroke(
            width = 1.dp,
            color = if (filled) tint.copy(alpha = 0.26f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (filled) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = tint,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = tint,
            )
        }
    }
}

@Composable
private fun TemplateShortcutChip(
    template: RecentTemplateUiModel,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = BudgetTheme.extendedColors.mist,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = template.cost,
                style = MaterialTheme.typography.labelMedium,
                color = laneColor(template.category),
            )
        }
    }
}

@Composable
private fun AttachedPhotoPreview(
    imagePath: String,
    accentColor: Color,
) {
    Surface(
        color = accentColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(BudgetTheme.radii.md)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Photo attached",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Saved with this entry",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            EntryBadge(
                label = "Ready",
                tint = accentColor,
                filled = true,
            )
        }
    }
}

@Composable
private fun SaveDock(
    isEntryValid: Boolean,
    saveItem: (Boolean) -> Unit,
    isSheet: Boolean,
) {
    val statusColor = animateColorAsState(
        targetValue = if (isEntryValid) {
            BudgetTheme.extendedColors.income
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 180),
        label = "saveDockStatusColor",
    )

    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level3),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            Text(
                text = if (isEntryValid) "Ready to save" else "Amount and category are required",
                style = MaterialTheme.typography.labelLarge,
                color = statusColor.value,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                OutlinedButton(
                    onClick = { saveItem(true) },
                    modifier = Modifier.weight(1f),
                    enabled = isEntryValid,
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    Text(text = if (isSheet) "Next" else "Keep adding")
                }
                Button(
                    onClick = { saveItem(false) },
                    modifier = Modifier.weight(1f),
                    enabled = isEntryValid,
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = if (isSheet) "Save" else "Save entry")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun laneColor(category: String): Color = when (category) {
    "food" -> BudgetTheme.extendedColors.food
    "transportation" -> BudgetTheme.extendedColors.transit
    "others" -> BudgetTheme.extendedColors.others
    "income" -> BudgetTheme.extendedColors.income
    else -> MaterialTheme.colorScheme.primary
}
