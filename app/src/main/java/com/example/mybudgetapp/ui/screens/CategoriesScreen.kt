package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.TRANSACTION_TYPE_INCOME
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.CategoriesUiState
import com.example.mybudgetapp.ui.viewmodels.CategoriesViewModel
import com.example.mybudgetapp.ui.viewmodels.CategoryActionResult
import com.example.mybudgetapp.ui.viewmodels.CategoryDraft
import com.example.mybudgetapp.ui.viewmodels.CategorySaveResult
import com.example.mybudgetapp.ui.widgets.AnimatedSegmentedControl
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.CategoryIcon
import com.example.mybudgetapp.ui.widgets.SegmentedTextLabel
import com.example.mybudgetapp.ui.widgets.categoryAccentColor
import com.example.mybudgetapp.ui.widgets.CategoryColorOption
import com.example.mybudgetapp.ui.widgets.CategoryIconChoice
import com.example.mybudgetapp.ui.widgets.categoryColorCatalog
import com.example.mybudgetapp.ui.widgets.categoryIconChoices
import com.example.mybudgetapp.ui.widgets.defaultCategoryColorHex
import com.example.mybudgetapp.ui.widgets.defaultCategoryIconKey
import com.example.mybudgetapp.ui.widgets.matchesCategoryColorOption
import com.example.mybudgetapp.ui.widgets.matchesCategoryIconChoice
import com.example.mybudgetapp.ui.widgets.resolveCategoryColorOption
import com.example.mybudgetapp.ui.widgets.resolveCategoryIconChoice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object CategoriesDestination : NavigationDestination {
    override val route = "Categories"
    override val titleRes = com.example.mybudgetapp.R.string.spending_on_category_screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: CategoriesViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = "Categories",
                navigateBack = navigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier.padding(BudgetTheme.spacing.lg),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    ) { paddingValues ->
        BudgetBackdrop(modifier = Modifier.padding(paddingValues)) {
            CategoriesBody(
                uiState = uiState,
                onArchiveCategory = { category ->
                    viewModel.archiveCategory(category) { result ->
                        when (result) {
                            is CategoryActionResult.Invalid -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            }

                            is CategoryActionResult.Success -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
            )
        }

        if (showAddSheet) {
            AddCategoryBottomSheet(
                onDismissRequest = { showAddSheet = false },
                onSaveCategory = { draft ->
                    viewModel.addCategory(draft) { result ->
                        when (result) {
                            is CategorySaveResult.Invalid -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            }

                            CategorySaveResult.Saved -> {
                                Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
                                showAddSheet = false
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun CategoriesBody(
    uiState: CategoriesUiState,
    onArchiveCategory: (BudgetCategory) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = BudgetTheme.spacing.lg,
            end = BudgetTheme.spacing.lg,
            top = BudgetTheme.spacing.lg,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
    ) {
        item {
            CategoriesHero(
                totalCount = uiState.expenseCategories.size + uiState.incomeCategories.size,
                expenseCount = uiState.expenseCategories.size,
                incomeCount = uiState.incomeCategories.size,
            )
        }
        item {
            DetailListHeader(
                title = "Expense categories",
                subtitle = "These drive your spending breakdowns, quick add, and insights.",
            )
        }
        if (uiState.expenseCategories.isEmpty()) {
            item {
                DetailEmptyStateCard(
                    title = "No expense categories yet",
                    message = "Add one to start organizing spending with your own structure.",
                    accent = MaterialTheme.colorScheme.primary,
                    iconKey = "misc",
                    fallbackCategoryKey = "others",
                )
            }
        } else {
            items(uiState.expenseCategories, key = { it.categoryKey }) { category ->
                CategoryRow(
                    category = category,
                    onArchive = if (category.isDefault) {
                        null
                    } else {
                        { onArchiveCategory(category) }
                    },
                )
            }
        }
        item {
            DetailListHeader(
                title = "Income categories",
                subtitle = "Keep income grouped with the same icon and color system.",
            )
        }
        if (uiState.incomeCategories.isEmpty()) {
            item {
                DetailEmptyStateCard(
                    title = "No income categories yet",
                    message = "Add one when you want more than a single income bucket.",
                    accent = MaterialTheme.colorScheme.primary,
                    iconKey = "income",
                    fallbackCategoryKey = "income",
                )
            }
        } else {
            items(uiState.incomeCategories, key = { it.categoryKey }) { category ->
                CategoryRow(
                    category = category,
                    onArchive = if (category.isDefault) {
                        null
                    } else {
                        { onArchiveCategory(category) }
                    },
                )
            }
        }
        if (uiState.archivedCategories.isNotEmpty()) {
            item {
                DetailListHeader(
                    title = "Archived categories",
                    subtitle = "These stay visible in history, but they no longer appear in add-entry category pickers.",
                )
            }
            items(uiState.archivedCategories, key = { it.categoryKey }) { category ->
                CategoryRow(category = category)
            }
        }
    }
}

@Composable
private fun CategoriesHero(
    totalCount: Int,
    expenseCount: Int,
    incomeCount: Int,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BudgetTheme.extendedColors.heroStart.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Text(
                text = "Category library",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "All category metadata now lives here, not in hardcoded UI lists.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                CategoryStatChip(label = "Total", value = totalCount.toString())
                CategoryStatChip(label = "Expense", value = expenseCount.toString())
                CategoryStatChip(label = "Income", value = incomeCount.toString())
            }
        }
    }
}

@Composable
private fun CategoryStatChip(
    label: String,
    value: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: BudgetCategory,
    onArchive: (() -> Unit)? = null,
) {
    val accent = categoryAccentColor(category.colorHex, category.categoryKey)
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = accent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(BudgetTheme.radii.md),
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CategoryIcon(
                        iconKey = category.iconKey,
                        fallbackCategoryKey = category.categoryKey,
                        tint = accent,
                        size = 24.dp,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (category.type == TRANSACTION_TYPE_INCOME) "Income category" else "Expense category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                color = accent.copy(alpha = 0.14f),
                shape = RoundedCornerShape(BudgetTheme.radii.pill),
            ) {
                Text(
                    text = category.categoryKey,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                )
            }
            if (onArchive != null) {
                Surface(
                    onClick = onArchive,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.84f),
                    shape = CircleShape,
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.mybudgetapp.R.drawable.baseline_settings_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private enum class AddCategoryPickerNav {
    Main,
    Icons,
    Colors,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategoryBottomSheet(
    onDismissRequest: () -> Unit,
    onSaveCategory: (CategoryDraft) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetContentVisible by remember { mutableStateOf(false) }
    var draft by remember {
        mutableStateOf(
            CategoryDraft(
                type = TRANSACTION_TYPE_EXPENSE,
                iconKey = defaultCategoryIconKey,
                colorHex = defaultCategoryColorHex,
            )
        )
    }
    var nav by rememberSaveable { mutableStateOf(AddCategoryPickerNav.Main) }
    var iconQuery by rememberSaveable { mutableStateOf("") }
    var colorQuery by rememberSaveable { mutableStateOf("") }

    fun dismissSheet(action: () -> Unit = {}) {
        coroutineScope.launch {
            sheetContentVisible = false
            delay(120)
            sheetState.hide()
            action()
            onDismissRequest()
        }
    }

    LaunchedEffect(Unit) {
        sheetContentVisible = true
        sheetState.show()
    }

    BackHandler(enabled = nav != AddCategoryPickerNav.Main) {
        nav = AddCategoryPickerNav.Main
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSheet() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        AnimatedVisibility(
            visible = sheetContentVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 260),
                    initialOffsetY = { it / 8 },
                ),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                slideOutVertically(
                    animationSpec = tween(durationMillis = 180),
                    targetOffsetY = { it / 8 },
                ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AddCategorySheetHeader(
                    nav = nav,
                    onBack = { nav = AddCategoryPickerNav.Main },
                    onClose = { dismissSheet() },
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                AnimatedContent(
                    targetState = nav,
                    label = "addCategoryPickerNav",
                    transitionSpec = {
                        val isForward = targetState != AddCategoryPickerNav.Main
                        if (initialState == targetState) {
                            EnterTransition.None togetherWith ExitTransition.None using SizeTransform(clip = false)
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { if (isForward) it else -it },
                                animationSpec = tween(220),
                            ).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { if (isForward) -it else it },
                                    animationSpec = tween(220),
                                )
                            ).using(SizeTransform(clip = false))
                        }
                    },
                    contentKey = {
                        when (it) {
                            AddCategoryPickerNav.Main -> "main"
                            AddCategoryPickerNav.Icons -> "icons"
                            AddCategoryPickerNav.Colors -> "colors"
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { targetNav ->
                    when (targetNav) {
                        AddCategoryPickerNav.Main -> {
                            AddCategoryMainContent(
                                draft = draft,
                                onDraftChange = { draft = it },
                                onOpenIcons = { nav = AddCategoryPickerNav.Icons },
                                onOpenColors = { nav = AddCategoryPickerNav.Colors },
                            )
                        }

                        AddCategoryPickerNav.Icons -> {
                            IconPickerSubSheet(
                                selectedIconKey = draft.iconKey,
                                accent = categoryAccentColor(draft.colorHex),
                                query = iconQuery,
                                onQueryChange = { iconQuery = it },
                                onIconSelected = {
                                    draft = draft.copy(iconKey = it)
                                },
                            )
                        }

                        AddCategoryPickerNav.Colors -> {
                            ColorPickerSubSheet(
                                selectedColorHex = draft.colorHex,
                                query = colorQuery,
                                onQueryChange = { colorQuery = it },
                                onColorSelected = {
                                    draft = draft.copy(colorHex = it)
                                },
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                if (nav == AddCategoryPickerNav.Main) {
                    Button(
                        onClick = { onSaveCategory(draft) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = BudgetTheme.spacing.xl, vertical = BudgetTheme.spacing.md)
                            .height(52.dp),
                        shape = RoundedCornerShape(BudgetTheme.radii.lg),
                    ) {
                        Text("Save category")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCategorySheetHeader(
    nav: AddCategoryPickerNav,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BudgetTheme.spacing.md, vertical = BudgetTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.xs),
        ) {
            if (nav != AddCategoryPickerNav.Main) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            }
            Text(
                text = when (nav) {
                    AddCategoryPickerNav.Main -> "Add category"
                    AddCategoryPickerNav.Icons -> "Choose icon"
                    AddCategoryPickerNav.Colors -> "Choose color"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCategoryMainContent(
    draft: CategoryDraft,
    onDraftChange: (CategoryDraft) -> Unit,
    onOpenIcons: () -> Unit,
    onOpenColors: () -> Unit,
) {
    val accent = categoryAccentColor(draft.colorHex)
    val quickIcons = remember { categoryIconChoices.take(10) }
    val quickColors = remember { categoryColorCatalog.take(16) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BudgetTheme.spacing.xl, vertical = BudgetTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
    ) {
        Text(
            text = "Choose a type, icon, and color. The key will be generated automatically from the name.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = draft.name,
            onValueChange = { onDraftChange(draft.copy(name = it)) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(BudgetTheme.radii.lg),
        )
        AnimatedSegmentedControl(
            selectedIndex = if (draft.type == TRANSACTION_TYPE_EXPENSE) 0 else 1,
            itemCount = 2,
            onItemSelected = { index ->
                onDraftChange(
                    draft.copy(type = if (index == 0) TRANSACTION_TYPE_EXPENSE else TRANSACTION_TYPE_INCOME)
                )
            },
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            indicatorColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(BudgetTheme.radii.pill),
            itemShape = RoundedCornerShape(BudgetTheme.radii.pill),
            shadowElevation = BudgetTheme.elevations.level2,
            itemSpacing = 6.dp,
            itemMinHeight = 46.dp,
        ) { index, selected ->
            SegmentedTextLabel(
                text = if (index == 0) "Expense" else "Income",
                selected = selected,
            )
        }

        SelectionSectionCard(
            title = "Icon",
            selectedLabel = resolveCategoryIconChoice(draft.iconKey).label,
            onSeeAll = onOpenIcons,
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                quickIcons.forEach { choice ->
                    QuickIconOption(
                        choice = choice,
                        selected = draft.iconKey == choice.key,
                        accent = accent,
                        onClick = { onDraftChange(draft.copy(iconKey = choice.key)) },
                    )
                }
            }
        }

        SelectionSectionCard(
            title = "Color",
            selectedLabel = resolveCategoryColorOption(draft.colorHex)?.label ?: draft.colorHex,
            onSeeAll = onOpenColors,
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                quickColors.forEach { option ->
                    QuickColorOption(
                        option = option,
                        selected = draft.colorHex.equals(option.hex, ignoreCase = true),
                        onClick = { onDraftChange(draft.copy(colorHex = option.hex)) },
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(BudgetTheme.radii.lg),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BudgetTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(BudgetTheme.radii.md),
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CategoryIcon(
                            iconKey = draft.iconKey,
                            tint = accent,
                            size = 24.dp,
                        )
                    }
                }
                Column {
                    Text(
                        text = draft.name.ifBlank { "Preview category" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (draft.type == TRANSACTION_TYPE_EXPENSE) "Expense category" else "Income category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionSectionCard(
    title: String,
    selectedLabel: String,
    onSeeAll: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    onClick = onSeeAll,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f),
                    shape = RoundedCornerShape(BudgetTheme.radii.pill),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun QuickIconOption(
    choice: CategoryIconChoice,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(BudgetTheme.radii.md),
        border = if (selected) BorderStroke(1.dp, accent.copy(alpha = 0.34f)) else null,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            CategoryIcon(
                iconKey = choice.key,
                tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 20.dp,
            )
        }
    }
}

@Composable
private fun QuickColorOption(
    option: CategoryColorOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ColorSwatch(
        option = option,
        selected = selected,
        size = 34.dp,
        onClick = onClick,
    )
}

@Composable
private fun QuickIconOptionSubSheet(
    choice: CategoryIconChoice,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(BudgetTheme.radii.md),
        border = if (selected) BorderStroke(1.dp, accent.copy(alpha = 0.34f)) else null,
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            CategoryIcon(
                iconKey = choice.key,
                tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 24.dp,
            )
        }
    }
}

@Composable
private fun QuickColorOptionSubSheet(
    option: CategoryColorOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ColorSwatch(
        option = option,
        selected = selected,
        size = 42.dp,
        onClick = onClick,
    )
}

@Composable
private fun ColorSwatch(
    option: CategoryColorOption,
    selected: Boolean,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val fillColor = categoryAccentColor(option.hex)

    val strokeColor = if (selected) {
        Color.LightGray
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(fillColor)
            .border(0.5.dp, strokeColor, CircleShape)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun IconPickerSubSheet(
    selectedIconKey: String,
    accent: Color,
    query: String,
    onQueryChange: (String) -> Unit,
    onIconSelected: (String) -> Unit,
) {
    val filteredChoices = remember(query) {
        categoryIconChoices.filter { matchesCategoryIconChoice(it, query) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BudgetTheme.spacing.xl, vertical = BudgetTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search icons") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(BudgetTheme.radii.lg),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 46.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            items(filteredChoices, key = { it.key }) { choice ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    QuickIconOptionSubSheet(
                        choice = choice,
                        selected = selectedIconKey == choice.key,
                        accent = accent,
                        onClick = { onIconSelected(choice.key) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPickerSubSheet(
    selectedColorHex: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onColorSelected: (String) -> Unit,
) {
    val filteredOptions = remember(query) {
        categoryColorCatalog.filter { matchesCategoryColorOption(it, query) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BudgetTheme.spacing.xl, vertical = BudgetTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search colors") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(BudgetTheme.radii.lg),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 48.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            items(filteredOptions, key = { it.hex }) { option ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    QuickColorOptionSubSheet(
                        option = option,
                        selected = selectedColorHex.equals(option.hex, ignoreCase = true),
                        onClick = { onColorSelected(option.hex) },
                    )
                }
            }
        }
    }
}
