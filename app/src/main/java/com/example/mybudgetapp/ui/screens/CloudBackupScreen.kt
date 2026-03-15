package com.example.mybudgetapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.dmSans
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.CloudBackupUiState
import com.example.mybudgetapp.ui.viewmodels.CloudBackupViewModel
import com.example.mybudgetapp.ui.widgets.BottomNavigationBar

object CloudBackupDestination : NavigationDestination {
    override val route = "CloudBackup"
    override val titleRes = R.string.cloud_backup
}

@Composable
fun CloudBackupScreen(
    navigateToThisMonthScreen: () -> Unit,
    navigateToThisYearScreen: () -> Unit,
) {
    val viewModel: CloudBackupViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navigateToThisMonthScreen = navigateToThisMonthScreen,
                navigateToThisYearScreen = navigateToThisYearScreen,
                navigateToCloudBackupScreen = {},
                selectedItemIndex = 2,
            )
        }
    ) { innerPadding ->
        CloudBackupBody(
            modifier = Modifier.padding(innerPadding),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .then(modifier),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Cloud Backup",
            fontFamily = dmSans,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Keep this app local-first and only sync manual snapshots to Supabase.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (!uiState.isConfigured) {
            SetupCard()
        } else if (!uiState.isSignedIn) {
            AuthCard(
                email = email,
                password = password,
                isBusy = uiState.isBusy,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onSignIn = { onSignIn(email, password) },
                onSignUp = { onSignUp(email, password) },
            )
        } else {
            SignedInCard(
                uiState = uiState,
                onUpload = onUpload,
                onRestore = onRestore,
                onRefresh = onRefresh,
                onDeleteBackup = { pendingAction = CloudDeleteAction.Backup },
                onDeleteAccount = { pendingAction = CloudDeleteAction.Account },
                onSignOut = onSignOut,
            )
        }

        uiState.statusMessage?.takeIf { it.isNotBlank() }?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
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
}

@Composable
private fun SetupCard() {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Cloud backup is not configured.", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Add these keys to local.properties:")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "SUPABASE_URL=...")
            Text(text = "SUPABASE_ANON_KEY=...")
        }
    }
}

@Composable
private fun AuthCard(
    email: String,
    password: String,
    isBusy: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Sign in to your backup account", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isBusy,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !isBusy,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSignIn,
                    enabled = !isBusy && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Sign in")
                    }
                }
                Button(
                    onClick = onSignUp,
                    enabled = !isBusy && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Create account")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "For the simplest setup, disable email confirmation in Supabase Auth while you test this.",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SignedInCard(
    uiState: CloudBackupUiState,
    onUpload: () -> Unit,
    onRestore: () -> Unit,
    onRefresh: () -> Unit,
    onDeleteBackup: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = uiState.email, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            BackupStatusRow(label = "Cloud backup", value = uiState.remoteBackupAt)
            BackupStatusRow(label = "Last upload from this device", value = uiState.lastUploadedAt)
            BackupStatusRow(label = "Last restore on this device", value = uiState.lastRestoredAt)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onUpload,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Backup now")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRestore,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Restore latest backup")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRefresh,
                    enabled = !uiState.isBusy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Refresh")
                }
                Button(
                    onClick = onSignOut,
                    enabled = !uiState.isBusy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Sign out")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Danger zone",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDeleteBackup,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Delete cloud backup")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDeleteAccount,
                enabled = !uiState.isBusy && uiState.canDeleteAccount,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Delete cloud account")
            }
            if (!uiState.canDeleteAccount) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Deploy the provided Edge Function and set SUPABASE_DELETE_ACCOUNT_URL to enable account deletion.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BackupStatusRow(label: String, value: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value ?: "Not available")
        Spacer(modifier = Modifier.height(8.dp))
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
    androidx.compose.material3.AlertDialog(
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
