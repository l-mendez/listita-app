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
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.ui.components.AppDialogType
import com.example.listitaapp.ui.components.AppMessageDialog
import com.example.listitaapp.ui.components.AppTextField
import com.example.listitaapp.ui.components.AppPasswordField
import com.example.listitaapp.ui.components.AppButton
import com.example.listitaapp.ui.components.AppTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var surnameError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Navigate to verify screen after successful registration
    LaunchedEffect(uiState.registrationComplete) {
        if (uiState.registrationComplete) {
            onNavigateToVerify(uiState.email)
        }
    }

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
                title = { Text(stringResource(R.string.register)) },
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
            // App branding
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.register),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name field
            AppTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = stringResource(R.string.name),
                leadingIcon = Icons.Default.Person,
                isError = nameError != null,
                errorMessage = nameError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Surname field
            AppTextField(
                value = surname,
                onValueChange = {
                    surname = it
                    surnameError = null
                },
                label = stringResource(R.string.surname),
                leadingIcon = Icons.Default.Person,
                isError = surnameError != null,
                errorMessage = surnameError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Email field
            AppTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = stringResource(R.string.email),
                placeholder = stringResource(R.string.email_placeholder),
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
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Confirm Password field
            AppPasswordField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = stringResource(R.string.confirm_password),
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (validateInput(
                                name, surname, email, password, confirmPassword,
                                { nameError = it },
                                { surnameError = it },
                                { emailError = it },
                                { passwordError = it },
                                { confirmPasswordError = it }
                            )) {
                            onRegister(email, password, name, surname)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Register button
            AppButton(
                onClick = {
                    if (validateInput(
                            name, surname, email, password, confirmPassword,
                            { nameError = it },
                            { surnameError = it },
                            { emailError = it },
                            { passwordError = it },
                            { confirmPasswordError = it }
                        )) {
                        onRegister(email, password, name, surname)
                    }
                },
                text = stringResource(R.string.register),
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                fullWidth = true
            )

            // Login link
            AppTextButton(
                onClick = onNavigateToLogin,
                text = "${stringResource(R.string.already_have_account)} ${stringResource(R.string.login)}",
                enabled = !uiState.isLoading
            )
        }
    }
}

// Input validation
private fun validateInput(
    name: String,
    surname: String,
    email: String,
    password: String,
    confirmPassword: String,
    setNameError: (String?) -> Unit,
    setSurnameError: (String?) -> Unit,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit,
    setConfirmPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (name.isBlank()) {
        setNameError("First name is required")
        isValid = false
    }

    if (surname.isBlank()) {
        setSurnameError("Last name is required")
        isValid = false
    }

    if (email.isBlank()) {
        setEmailError("Email is required")
        isValid = false
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        setEmailError("Invalid email address")
        isValid = false
    }

    if (password.isBlank()) {
        setPasswordError("Password is required")
        isValid = false
    } else if (password.length < 6) {
        setPasswordError("Password must be at least 6 characters")
        isValid = false
    }

    if (confirmPassword.isBlank()) {
        setConfirmPasswordError("Confirm your password")
        isValid = false
    } else if (password != confirmPassword) {
        setConfirmPasswordError("Passwords don't match")
        isValid = false
    }

    return isValid
}
