package com.example.listitaapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.ui.components.AppDialogType
import com.example.listitaapp.ui.components.AppMessageDialog
import com.example.listitaapp.ui.components.AppTextField
import com.example.listitaapp.ui.components.AppButton
import com.example.listitaapp.ui.components.AppTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyAccountScreen(
    email: String,
    uiState: AuthUiState,
    onVerify: (String) -> Unit,
    onResendCode: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit = {}
) {
    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Navigation handled by MainActivity based on auth state
    // When isAuthenticated becomes true, NavHost will automatically navigate to main screen

    // Show error dialog (standardized)
    uiState.error?.let {
        AppMessageDialog(
            type = AppDialogType.Error,
            message = it,
            onDismiss = onClearError
        )
    }

    // Show success dialog (standardized, for resend)
    uiState.successMessage?.let { msg ->
        AppMessageDialog(
            type = AppDialogType.Success,
            message = msg,
            onDismiss = onClearSuccess
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.verify_account)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.verify_account),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Info text
            Text(
                text = stringResource(R.string.verification_info),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.check_spam),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Verification code field
            AppTextField(
                value = code,
                onValueChange = {
                    code = it.trim()
                    codeError = null
                },
                label = stringResource(R.string.verification_code),
                placeholder = stringResource(R.string.code_placeholder),
                leadingIcon = Icons.Default.Lock,
                isError = codeError != null,
                errorMessage = codeError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (validateCode(code.trim(), { codeError = it })) {
                            onVerify(code.trim())
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Verify button
            AppButton(
                onClick = {
                    if (validateCode(code.trim(), { codeError = it })) {
                        onVerify(code.trim())
                    }
                },
                text = stringResource(R.string.verify_account_title),
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                fullWidth = true
            )

            // Resend code button
            AppTextButton(
                onClick = onResendCode,
                text = stringResource(R.string.resend_code),
                enabled = !uiState.isLoading
            )

            // Back to login
            AppTextButton(
                onClick = onNavigateToLogin,
                text = stringResource(R.string.back_to_login),
                enabled = !uiState.isLoading
            )
        }
    }
}

// Code validation
private fun validateCode(
    code: String,
    setCodeError: (String?) -> Unit
): Boolean {
    if (code.isBlank()) {
        setCodeError("Verification code is required")
        return false
    }

    if (code.length < 6) {
        setCodeError("Code must be at least 6 characters")
        return false
    }

    return true
}
