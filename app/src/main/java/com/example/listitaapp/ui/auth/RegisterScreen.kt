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
import com.example.listitaapp.ui.components.WindowSizeClass
import com.example.listitaapp.ui.components.isLandscape
import com.example.listitaapp.ui.components.rememberWindowSize

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
    val windowSize = rememberWindowSize()
    val isTabletLandscape = isLandscape() && windowSize.width != WindowSizeClass.Compact
    val formWidthModifier = if (isTabletLandscape) Modifier.widthIn(max = 420.dp) else Modifier
    val horizontalPadding = if (isTabletLandscape) 48.dp else 24.dp
    val verticalSpacing = if (isTabletLandscape) 12.dp else 16.dp
    val headerSpacerHeight = if (isTabletLandscape) 8.dp else 16.dp
    val buttonSpacerHeight = if (isTabletLandscape) 6.dp else 8.dp
    val linkSpacerHeight = if (isTabletLandscape) 2.dp else 6.dp

    LaunchedEffect(uiState.registrationComplete) {
        if (uiState.registrationComplete) {
            onNavigateToVerify(uiState.email)
        }
    }

    uiState.error?.let {
        AppMessageDialog(
            type = AppDialogType.Error,
            message = it,
            onDismiss = onClearError
        )
    }

    val nameRequiredMessage = stringResource(R.string.name_required)
    val surnameRequiredMessage = stringResource(R.string.surname_required)
    val emailRequiredMessage = stringResource(R.string.email_required)
    val invalidEmailMessage = stringResource(R.string.invalid_email)
    val passwordRequiredMessage = stringResource(R.string.password_required)
    val passwordTooShortMessage = stringResource(R.string.password_too_short)
    val confirmPasswordRequiredMessage = stringResource(R.string.confirm_password_required)
    val passwordsDontMatchMessage = stringResource(R.string.passwords_dont_match)

    Scaffold { padding ->
        val verificationEmail = uiState.email.trim()
        val columnArrangement = if (verificationEmail.isBlank()) {
            Arrangement.spacedBy(verticalSpacing, Alignment.CenterVertically)
        } else {
            Arrangement.spacedBy(verticalSpacing)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = horizontalPadding)
                    .then(formWidthModifier)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = columnArrangement
            ) {
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
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

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
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

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
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

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
                                name,
                                surname,
                                email,
                                password,
                                confirmPassword,
                                nameRequiredMessage,
                                surnameRequiredMessage,
                                emailRequiredMessage,
                                invalidEmailMessage,
                                passwordRequiredMessage,
                                passwordTooShortMessage,
                                confirmPasswordRequiredMessage,
                                passwordsDontMatchMessage,
                                { nameError = it },
                                { surnameError = it },
                                { emailError = it },
                                { passwordError = it },
                                { confirmPasswordError = it }
                            )
                        ) {
                            onRegister(email, password, name, surname)
                        }
                    }
                ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(buttonSpacerHeight))

                AppButton(
                    onClick = {
                    if (validateInput(
                            name,
                            surname,
                            email,
                            password,
                            confirmPassword,
                            nameRequiredMessage,
                            surnameRequiredMessage,
                            emailRequiredMessage,
                            invalidEmailMessage,
                            passwordRequiredMessage,
                            passwordTooShortMessage,
                            confirmPasswordRequiredMessage,
                            passwordsDontMatchMessage,
                            { nameError = it },
                            { surnameError = it },
                            { emailError = it },
                            { passwordError = it },
                            { confirmPasswordError = it }
                        )
                    ) {
                        onRegister(email, password, name, surname)
                    }
                },
                    text = stringResource(R.string.register),
                    enabled = !uiState.isLoading,
                    loading = uiState.isLoading,
                    fullWidth = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(linkSpacerHeight))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(linkSpacerHeight)
                ) {
                    if (verificationEmail.isNotBlank()) {
                        AppTextButton(
                            onClick = { onNavigateToVerify(verificationEmail) },
                            text = "${stringResource(R.string.already_have_verification_code)} ${stringResource(R.string.verify_now)}",
                            enabled = !uiState.isLoading
                        )
                    }

                    AppTextButton(
                        onClick = onNavigateToLogin,
                        text = "${stringResource(R.string.already_have_account)} ${stringResource(R.string.login)}",
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
    }

}

private fun validateInput(
    name: String,
    surname: String,
    email: String,
    password: String,
    confirmPassword: String,
    nameRequiredMessage: String,
    surnameRequiredMessage: String,
    emailRequiredMessage: String,
    invalidEmailMessage: String,
    passwordRequiredMessage: String,
    passwordTooShortMessage: String,
    confirmPasswordRequiredMessage: String,
    passwordsDontMatchMessage: String,
    setNameError: (String?) -> Unit,
    setSurnameError: (String?) -> Unit,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit,
    setConfirmPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (name.isBlank()) {
        setNameError(nameRequiredMessage)
        isValid = false
    }

    if (surname.isBlank()) {
        setSurnameError(surnameRequiredMessage)
        isValid = false
    }

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

    if (confirmPassword.isBlank()) {
        setConfirmPasswordError(confirmPasswordRequiredMessage)
        isValid = false
    } else if (password != confirmPassword) {
        setConfirmPasswordError(passwordsDontMatchMessage)
        isValid = false
    }

    return isValid
}
