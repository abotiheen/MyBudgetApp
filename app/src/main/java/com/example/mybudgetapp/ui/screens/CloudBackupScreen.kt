package com.example.mybudgetapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.navigation.NavigationDestination
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

    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    Scaffold(
        containerColor = Color.Transparent,
    ) { innerPadding ->
        BudgetBackdrop(modifier = Modifier.padding(innerPadding)) {
            CloudBackupBody(
                uiState = uiState,
                onSignIn = viewModel::signIn,
                onSignUp = viewModel::signUp,
                onUpload = viewModel::uploadBackup,
                onRestore = viewModel::restoreBackup,
                onRefresh = viewModel::refreshStatus,
                onDeleteBackup = viewModel::deleteBackup,
                onDeleteAccount = viewModel::deleteAccount,
                onSignOut = viewModel::signOut,
            )
        }
    }
}

@Composable
private fun CloudBackupBody(
    modifier: Modifier = Modifier,
    uiState: CloudBackupUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onUpload: () -> Unit,
    onRestore: () -> Unit,
    onRefresh: () -> Unit,
    onDeleteBackup: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var pendingAction by rememberSaveable { mutableStateOf<CloudDeleteAction?>(null) }
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
            VaultHeroCard(uiState = uiState)
        }

        uiState.statusMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                VaultStatusBanner(message = message)
            }
        }

        if (!uiState.isConfigured) {
            item {
                SetupVaultCard()
            }
        } else if (!uiState.isSignedIn) {
            item {
                VaultAuthCard(
                    email = email,
                    password = password,
                    isBusy = uiState.isBusy,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onSignIn = { onSignIn(email, password) },
                    onSignUp = { onSignUp(email, password) },
                )
            }
        } else {
            item {
                VaultAccountCard(
                    uiState = uiState,
                    onUpload = onUpload,
                    onRestore = onRestore,
                    onRefresh = onRefresh,
                    onSignOut = onSignOut,
                )
            }
            item {
                VaultTimelineCard(uiState = uiState)
            }
            item {
                DangerZoneCard(
                    uiState = uiState,
                    onDeleteBackup = { pendingAction = CloudDeleteAction.Backup },
                    onDeleteAccount = { pendingAction = CloudDeleteAction.Account },
                )
            }
        }
    }

    pendingAction?.let { action ->
        ConfirmCloudDeleteDialog(
            title = if (action == CloudDeleteAction.Backup) "Delete cloud backup?" else "Delete cloud account?",
            body = if (action == CloudDeleteAction.Backup) {
                "This removes the saved snapshot from Supabase. Your local data on this device stays untouched."
            } else {
                "This deletes your Supabase auth account through a secure server endpoint. The cloud backup will also disappear."
            },
            confirmLabel = if (action == CloudDeleteAction.Backup) "Delete backup" else "Delete account",
            onConfirm = {
                if (action == CloudDeleteAction.Backup) {
                    onDeleteBackup()
                } else {
                    onDeleteAccount()
                }
                pendingAction = null
            },
            onDismiss = { pendingAction = null },
        )
    }
}

@Composable
private fun VaultHeroCard(
    uiState: CloudBackupUiState,
) {
    val icon = when {
        !uiState.isConfigured -> Icons.Filled.Lock
        uiState.isSignedIn -> Icons.Filled.Check
        else -> Icons.Filled.Lock
    }
    val title = when {
        !uiState.isConfigured -> "Cloud Vault needs setup"
        uiState.isSignedIn -> "Cloud Vault is active"
        else -> "Cloud Vault is ready"
    }
    val subtitle = when {
        !uiState.isConfigured -> "Add your Supabase keys first, then the vault can store manual snapshots."
        uiState.isSignedIn -> "Local-first by default. Sync only the snapshots you choose."
        else -> "Sign in to start backing up your budget data to Supabase."
    }

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
                            imageVector = icon,
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
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Text(
                text = subtitle,
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
private fun SetupVaultCard() {
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
                title = "Setup required",
                subtitle = "Cloud backup is disabled until your Supabase keys exist locally.",
            )
            VaultKeyRow(label = "SUPABASE_URL=...")
            VaultKeyRow(label = "SUPABASE_ANON_KEY=...")
        }
    }
}

@Composable
private fun VaultKeyRow(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        shape = RoundedCornerShape(BudgetTheme.radii.md),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = BudgetTheme.spacing.md, vertical = 14.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun VaultAuthCard(
    email: String,
    password: String,
    isBusy: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
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
                title = "Connect your vault account",
                subtitle = "Sign in or create an account before you upload your first snapshot.",
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isBusy,
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !isBusy,
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                Button(
                    onClick = onSignIn,
                    enabled = !isBusy && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Sign in")
                    }
                }
                Button(
                    onClick = onSignUp,
                    enabled = !isBusy && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text("Create account")
                }
            }
            Text(
                text = "For the simplest test flow, disable email confirmation in Supabase Auth.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VaultAccountCard(
    uiState: CloudBackupUiState,
    onUpload: () -> Unit,
    onRestore: () -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
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
                title = "Vault account",
                subtitle = uiState.email,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            ) {
                VaultActionButton(
                    label = "Backup now",
                    onClick = onUpload,
                    enabled = !uiState.isBusy,
                    modifier = Modifier.weight(1f),
                )
                VaultActionButton(
                    label = "Restore latest",
                    onClick = onRestore,
                    enabled = !uiState.isBusy,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            ) {
                VaultSecondaryButton(
                    label = "Refresh",
                    onClick = onRefresh,
                    enabled = !uiState.isBusy,
                    modifier = Modifier.weight(1f),
                )
                VaultSecondaryButton(
                    label = "Sign out",
                    onClick = onSignOut,
                    enabled = !uiState.isBusy,
                    modifier = Modifier.weight(1f),
                )
            }
        }
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

@Composable
private fun VaultSecondaryButton(
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
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(label)
    }
}

@Composable
private fun VaultTimelineCard(
    uiState: CloudBackupUiState,
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
                title = "Vault timeline",
                subtitle = "Recent cloud and device backup moments.",
            )
            VaultStatusRow(
                title = "Cloud backup",
                value = uiState.remoteBackupAt ?: "Not available",
                tint = MaterialTheme.colorScheme.primary,
            )
            VaultStatusRow(
                title = "Last upload from this device",
                value = uiState.lastUploadedAt ?: "Not available",
                tint = BudgetTheme.extendedColors.income,
            )
            VaultStatusRow(
                title = "Last restore on this device",
                value = uiState.lastRestoredAt ?: "Not available",
                tint = BudgetTheme.extendedColors.warning,
            )
        }
    }
}

@Composable
private fun VaultStatusRow(
    title: String,
    value: String,
    tint: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(tint, CircleShape)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DangerZoneCard(
    uiState: CloudBackupUiState,
    onDeleteBackup: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            SectionHeading(
                title = "Danger zone",
                subtitle = "These actions affect remote cloud data. Local device data stays untouched unless you restore over it.",
            )
            Button(
                onClick = onDeleteBackup,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("Delete cloud backup")
            }
            Button(
                onClick = onDeleteAccount,
                enabled = !uiState.isBusy && uiState.canDeleteAccount,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("Delete cloud account")
            }
            if (!uiState.canDeleteAccount) {
                Text(
                    text = "Deploy the provided Edge Function and set SUPABASE_DELETE_ACCOUNT_URL to enable account deletion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

private enum class CloudDeleteAction {
    Backup,
    Account,
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
