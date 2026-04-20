package com.example.mybudgetapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.AppThemeMode
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.CloudBackupUiState
import com.example.mybudgetapp.ui.viewmodels.CloudBackupViewModel
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.SectionHeading

object CloudBackupDestination : NavigationDestination {
    override val route = "CloudBackup"
    override val titleRes = R.string.cloud_backup
}

@Composable
fun CloudBackupScreen() {
    val viewModel: CloudBackupViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val systemDarkTheme = isSystemInDarkTheme()
    var pendingRestoreUri by rememberSaveable { mutableStateOf<String?>(null) }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        pendingRestoreUri = uri?.toString()
    }

    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    Scaffold(
        containerColor = Color.Transparent,
    ) { innerPadding ->
        BudgetBackdrop(modifier = Modifier.padding(innerPadding)) {
            CloudBackupBody(
                uiState = uiState,
                systemDarkTheme = systemDarkTheme,
                onThemeSelected = viewModel::selectDarkMode,
                onExportJson = viewModel::exportJsonBackup,
                onRestoreJson = { restoreLauncher.launch(arrayOf("application/json", "text/plain")) },
                onExportSpreadsheet = viewModel::exportSpreadsheet,
            )
        }
    }

    pendingRestoreUri?.let { uriString ->
        ConfirmCloudDeleteDialog(
            title = "Restore offline backup?",
            body = "This replaces the current local categories and transactions with the contents of the selected JSON backup.",
            confirmLabel = "Restore backup",
            onConfirm = {
                viewModel.restoreJsonBackup(Uri.parse(uriString))
                pendingRestoreUri = null
            },
            onDismiss = { pendingRestoreUri = null },
        )
    }
}

@Composable
private fun CloudBackupBody(
    modifier: Modifier = Modifier,
    uiState: CloudBackupUiState,
    systemDarkTheme: Boolean,
    onThemeSelected: (Boolean) -> Unit,
    onExportJson: () -> Unit,
    onRestoreJson: () -> Unit,
    onExportSpreadsheet: () -> Unit,
) {
    val spacing = BudgetTheme.spacing

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = spacing.lg,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            VaultHeroCard()
        }
        item {
            AppearanceCard(
                selectedMode = uiState.themeMode,
                systemDarkTheme = systemDarkTheme,
                enabled = !uiState.isBusy,
                onThemeSelected = onThemeSelected,
            )
        }
        item {
            LocalExportCard(
                isBusy = uiState.isBusy,
                onExportSpreadsheet = onExportSpreadsheet,
            )
        }
        item {
            OfflineBackupCard(
                isBusy = uiState.isBusy,
                onExportJson = onExportJson,
                onRestoreJson = onRestoreJson,
            )
        }
        uiState.statusMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                VaultStatusBanner(message = message)
            }
        }
    }
}

@Composable
private fun AppearanceCard(
    selectedMode: AppThemeMode,
    systemDarkTheme: Boolean,
    enabled: Boolean,
    onThemeSelected: (Boolean) -> Unit,
) {
    val isDarkSelected = when (selectedMode) {
        AppThemeMode.System -> systemDarkTheme
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            SectionHeading(
                title = "Appearance",
                subtitle = "Use a darker surface palette across the app.",
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = BudgetTheme.spacing.lg,
                            vertical = BudgetTheme.spacing.md,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "Dark mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (isDarkSelected) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isDarkSelected,
                        onCheckedChange = { onThemeSelected(it) },
                        enabled = enabled,
                    )
                }
            }
            if (selectedMode == AppThemeMode.System) {
                Text(
                    text = "The app was previously following the system setting. This switch now controls it directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LocalExportCard(
    isBusy: Boolean,
    onExportSpreadsheet: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            SectionHeading(
                title = "Local spreadsheet export",
                subtitle = "Save an Excel-compatible workbook with Categories and Transactions directly to Downloads.",
            )
            VaultActionButton(
                label = "Export Excel",
                onClick = onExportSpreadsheet,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "This works without signing in. The file is written locally on your device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OfflineBackupCard(
    isBusy: Boolean,
    onExportJson: () -> Unit,
    onRestoreJson: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            SectionHeading(
                title = "Offline backup",
                subtitle = "Create a native JSON backup or restore one from local storage.",
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            ) {
                VaultActionButton(
                    label = "Export JSON",
                    onClick = onExportJson,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                )
                VaultActionButton(
                    label = "Restore JSON",
                    onClick = onRestoreJson,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "Restore replaces your current local data with the selected offline backup.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VaultHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f),
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ) {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Cloud Vault",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Cloud Vault is ready",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Text(
                text = "Create local snapshots, restore them from storage.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VaultStatusBanner(
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun VaultActionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
    ) {
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmCloudDeleteDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
