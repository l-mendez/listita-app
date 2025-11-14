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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.ui.components.AppDialogType
import com.example.listitaapp.ui.components.AppMessageDialog
import com.example.listitaapp.ui.components.AppTextField
import com.example.listitaapp.ui.components.AppPasswordField
import com.example.listitaapp.ui.components.AppButton
import com.example.listitaapp.ui.components.AppTextButton

/**
 * Login Screen
 * Demonstrates HCI principles:
 * - Visibility of system status (loading indicators, error messages)
 * - Error prevention (input validation)
 * - Recognition over recall (clear labels, placeholders)
 * - Aesthetic and minimalist design
 * - Consistency (Material 3 design system)
 * - Gestalt principles (proximity, similarity, continuity)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Show error dialog (standardized)
    uiState.error?.let {
        AppMessageDialog(
            type = AppDialogType.Error,
            message = it,
            onDismiss = onClearError
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.login)) }
            )
        }
    ) { padding ->
        // Gestalt: Proximity - Related elements grouped together
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // App branding
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email field (Nielsen: Recognition over recall)
            AppTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null // Clear error on input
                },
                label = stringResource(R.string.email),
                placeholder = "ejemplo@correo.com",
                leadingIcon = Icons.Default.Email,
                isError = emailError != null,
                errorMessage = emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Password field
            AppPasswordField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = stringResource(R.string.password),
                isError = passwordError != null,
                errorMessage = passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (validateInput(email, password,
                                { emailError = it },
                                { passwordError = it })) {
                            onLogin(email, password)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Login button (Nielsen: Visibility of system status)
            AppButton(
                onClick = {
                    if (validateInput(email, password,
                            { emailError = it },
                            { passwordError = it })) {
                        onLogin(email, password)
                    }
                },
                text = stringResource(R.string.login),
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                fullWidth = true
            )

            // Register link (Gestalt: Continuity)
            AppTextButton(
                onClick = onNavigateToRegister,
                text = "¿No tienes cuenta? ${stringResource(R.string.register)}",
                enabled = !uiState.isLoading
            )
        }
    }
}

// Error prevention: Input validation
private fun validateInput(
    email: String,
    password: String,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (email.isBlank()) {
        setEmailError("El correo es requerido")
        isValid = false
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        setEmailError("Correo electrónico inválido")
        isValid = false
    }

    if (password.isBlank()) {
        setPasswordError("La contraseña es requerida")
        isValid = false
    } else if (password.length < 6) {
        setPasswordError("La contraseña debe tener al menos 6 caracteres")
        isValid = false
    }

    return isValid
}
