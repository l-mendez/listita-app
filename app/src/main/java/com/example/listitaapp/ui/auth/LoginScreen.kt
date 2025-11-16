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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.listitaapp.ui.components.WindowSizeClass
import com.example.listitaapp.ui.components.isLandscape
import com.example.listitaapp.ui.components.rememberWindowSize


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
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val windowSize = rememberWindowSize()

    val isMobileHorizontal = isLandscape() && screenHeightDp < 500
    val isTabletLandscape = isLandscape() && screenHeightDp >= 500
    val formWidthModifier = if (isTabletLandscape) Modifier.widthIn(max = 420.dp) else Modifier
    val horizontalPadding = when {
        isMobileHorizontal -> 16.dp
        isTabletLandscape -> 48.dp
        else -> 24.dp
    }
    val verticalSpacing = when {
        isMobileHorizontal -> 12.dp
        isTabletLandscape -> 12.dp
        else -> 16.dp
    }
    val headerSpacerHeight = if (isTabletLandscape) 8.dp else 16.dp
    val buttonSpacerHeight = if (isTabletLandscape) 4.dp else 8.dp

    // Show error dialog (standardized)
    uiState.error?.let {
        AppMessageDialog(
            type = AppDialogType.Error,
            message = it,
            onDismiss = onClearError
        )
    }

    val emailRequiredMessage = stringResource(R.string.email_required)
    val invalidEmailMessage = stringResource(R.string.invalid_email)
    val passwordRequiredMessage = stringResource(R.string.password_required)
    val passwordTooShortMessage = stringResource(R.string.password_too_short)

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isMobileHorizontal) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = horizontalPadding)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 24.dp, end = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(verticalSpacing, Alignment.CenterVertically)
                    ) {
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
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

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
                                    if (validateInput(
                                            email,
                                            password,
                                            emailRequiredMessage,
                                            invalidEmailMessage,
                                            passwordRequiredMessage,
                                            passwordTooShortMessage,
                                            { emailError = it },
                                            { passwordError = it }
                                        )) {
                                        onLogin(email, password)
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(buttonSpacerHeight))

                        AppButton(
                            onClick = {
                                if (validateInput(
                                        email,
                                        password,
                                        emailRequiredMessage,
                                        invalidEmailMessage,
                                        passwordRequiredMessage,
                                        passwordTooShortMessage,
                                        { emailError = it },
                                        { passwordError = it }
                                    )) {
                                    onLogin(email, password)
                                }
                            },
                            text = stringResource(R.string.login),
                            enabled = !uiState.isLoading,
                            loading = uiState.isLoading,
                            fullWidth = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextButton(
                            onClick = onNavigateToRegister,
                            text = "${stringResource(R.string.no_account)} ${stringResource(R.string.register)}",
                            enabled = !uiState.isLoading
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = horizontalPadding)
                        .then(formWidthModifier)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing, Alignment.CenterVertically)
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

                    Spacer(modifier = Modifier.height(headerSpacerHeight))

                    // Email field (Nielsen: Recognition over recall)
                    AppTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null // Clear error on input
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
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                                if (validateInput(
                                        email,
                                        password,
                                        emailRequiredMessage,
                                        invalidEmailMessage,
                                        passwordRequiredMessage,
                                        passwordTooShortMessage,
                                        { emailError = it },
                                        { passwordError = it }
                                    )) {
                                    onLogin(email, password)
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(buttonSpacerHeight))

                    // Login button (Nielsen: Visibility of system status)
                    AppButton(
                        onClick = {
                            if (validateInput(
                                    email,
                                    password,
                                    emailRequiredMessage,
                                    invalidEmailMessage,
                                    passwordRequiredMessage,
                                    passwordTooShortMessage,
                                    { emailError = it },
                                    { passwordError = it }
                                )) {
                                onLogin(email, password)
                            }
                        },
                        text = stringResource(R.string.login),
                        enabled = !uiState.isLoading,
                        loading = uiState.isLoading,
                        fullWidth = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Register link (Gestalt: Continuity)
                    AppTextButton(
                        onClick = onNavigateToRegister,
                        text = "${stringResource(R.string.no_account)} ${stringResource(R.string.register)}",
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
    }
}

// Error prevention: Input validation
private fun validateInput(
    email: String,
    password: String,
    emailRequiredMessage: String,
    invalidEmailMessage: String,
    passwordRequiredMessage: String,
    passwordTooShortMessage: String,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (email.isBlank()) {
        setEmailError(emailRequiredMessage)
        isValid = false
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        setEmailError(invalidEmailMessage)
        isValid = false
    }

    if (password.isBlank()) {
        setPasswordError(passwordRequiredMessage)
        isValid = false
    } else if (password.length < 6) {
        setPasswordError(passwordTooShortMessage)
        isValid = false
    }

    return isValid
}
