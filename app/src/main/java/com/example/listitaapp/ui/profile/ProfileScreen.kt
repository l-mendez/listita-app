package com.example.listitaapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import com.example.listitaapp.R
import com.example.listitaapp.ui.components.AppTopBar
import com.example.listitaapp.ui.components.StandardCard
import com.example.listitaapp.ui.components.AppConfirmDialog
import com.example.listitaapp.ui.components.AppDialogType
import com.example.listitaapp.ui.components.AppMessageDialog
import com.example.listitaapp.ui.components.AppFormDialog
import com.example.listitaapp.ui.components.AppSnackbarHost
import com.example.listitaapp.ui.components.rememberAppSnackbarState
import com.example.listitaapp.ui.components.appSnackTypeFromMessage
import com.example.listitaapp.ui.components.show
import com.example.listitaapp.ui.components.AppTextField
import com.example.listitaapp.ui.components.AppPasswordField
import com.example.listitaapp.ui.common.asString


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    val appSnackbar = rememberAppSnackbarState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Error dialog (standardized)
    uiState.error?.let {
        AppMessageDialog(
            type = AppDialogType.Error,
            message = it,
            onDismiss = onClearError
        )
    }

    // Success snackbar (standardized)
    uiState.successMessage?.let { message ->
        val localizedMessage = message.asString()
        LaunchedEffect(message) {
            if (localizedMessage.isNotBlank()) {
                appSnackbar.show(localizedMessage, appSnackTypeFromMessage(localizedMessage))
            }
            onClearSuccess()
        }
    }

    // Logout confirmation dialog (standardized, non-destructive style)
    if (showLogoutDialog) {
        AppConfirmDialog(
            message = stringResource(R.string.confirm_logout),
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false },
            destructive = false,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            confirmLabel = stringResource(R.string.logout)
        )
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.profile))
        },
        snackbarHost = { AppSnackbarHost(state = appSnackbar) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.user?.let { user ->
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        StandardCard(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${user.name} ${user.surname}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Edit,
                                title = stringResource(R.string.edit_profile),
                                subtitle = stringResource(R.string.profile_edit_description),
                                onClick = onEditProfile,
                                isLandscape = true
                            )

                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Lock,
                                title = stringResource(R.string.change_password),
                                subtitle = stringResource(R.string.profile_password_description),
                                onClick = onChangePassword,
                                isLandscape = true
                            )

                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                title = stringResource(R.string.logout),
                                subtitle = stringResource(R.string.profile_logout_description),
                                onClick = { showLogoutDialog = true },
                                tint = MaterialTheme.colorScheme.error,
                                isLandscape = true
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        StandardCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "${user.name} ${user.surname}",
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        SettingsItem(
                            icon = Icons.Default.Edit,
                            title = stringResource(R.string.edit_profile),
                            subtitle = stringResource(R.string.profile_edit_description),
                            onClick = onEditProfile
                        )

                        SettingsItem(
                            icon = Icons.Default.Lock,
                            title = stringResource(R.string.change_password),
                            subtitle = stringResource(R.string.profile_password_description),
                            onClick = onChangePassword
                        )

                        SettingsItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = stringResource(R.string.logout),
                            subtitle = stringResource(R.string.profile_logout_description),
                            onClick = { showLogoutDialog = true },
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    isLandscape: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isLandscape) {
                    Modifier
                } else {
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                }
            )
    ) {
        StandardCard(
            modifier = Modifier.fillMaxSize(),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentName: String,
    currentSurname: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var surname by remember { mutableStateOf(currentSurname) }

    AppFormDialog(
        title = stringResource(R.string.edit_profile),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.save),
        confirmEnabled = name.isNotBlank() && surname.isNotBlank(),
        onConfirm = {
            if (name.isNotBlank() && surname.isNotBlank()) {
                onSave(name, surname)
                onDismiss()
            }
        }
    ) {
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.name),
            leadingIcon = Icons.Default.Person
        )
        AppTextField(
            value = surname,
            onValueChange = { surname = it },
            label = stringResource(R.string.surname),
            leadingIcon = Icons.Default.Person
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChange: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AppFormDialog(
        title = stringResource(R.string.change_password),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.change_password),
        confirmEnabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank(),
        onConfirm = {
            when {
                currentPassword.isBlank() -> error = "Current password required"
                newPassword.isBlank() -> error = "New password required"
                newPassword.length < 6 -> error = "Password must be at least 6 characters"
                newPassword != confirmPassword -> error = "Passwords don't match"
                else -> {
                    onChange(currentPassword, newPassword)
                    onDismiss()
                }
            }
        }
    ) {
        AppPasswordField(
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                error = null
            },
            label = stringResource(R.string.current_password)
        )

        AppPasswordField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                error = null
            },
            label = stringResource(R.string.new_password)
        )

        AppPasswordField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                error = null
            },
            label = stringResource(R.string.confirm_password),
            isError = error != null,
            errorMessage = error
        )
    }
}
