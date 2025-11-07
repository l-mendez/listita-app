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

    // Show error dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = onClearError,
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(uiState.error) },
            confirmButton = {
                TextButton(onClick = onClearError) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    // Show success dialog (for resend)
    if (uiState.successMessage != null) {
        AlertDialog(
            onDismissRequest = onClearSuccess,
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            title = { Text("Success") },
            text = { Text(uiState.successMessage) },
            confirmButton = {
                TextButton(onClick = onClearSuccess) {
                    Text(stringResource(R.string.ok))
                }
            }
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
                text = "Hemos enviado un código de verificación a:",
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
                text = "Revisa tu correo electrónico y la consola del API para obtener el código.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Verification code field
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it.trim()
                    codeError = null
                },
                label = { Text(stringResource(R.string.verification_code)) },
                placeholder = { Text("Ingresa el código") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                isError = codeError != null,
                supportingText = codeError?.let { { Text(it) } },
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
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Verify button
            Button(
                onClick = {
                    if (validateCode(code.trim(), { codeError = it })) {
                        onVerify(code.trim())
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Verificar Cuenta")
                }
            }

            // Resend code button
            TextButton(
                onClick = onResendCode,
                enabled = !uiState.isLoading
            ) {
                Text("¿No recibiste el código? Reenviar")
            }

            // Back to login
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading
            ) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}

// Code validation
private fun validateCode(
    code: String,
    setCodeError: (String?) -> Unit
): Boolean {
    if (code.isBlank()) {
        setCodeError("El código es requerido")
        return false
    }

    if (code.length < 6) {
        setCodeError("El código debe tener al menos 6 caracteres")
        return false
    }

    return true
}
