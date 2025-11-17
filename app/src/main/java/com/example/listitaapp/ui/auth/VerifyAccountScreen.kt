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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.listitaapp.ui.components.isLandscape
import com.example.listitaapp.ui.common.asString

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
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp

    val isMobileHorizontal = isLandscape() && screenHeightDp < 500
    val isTabletLandscape = isLandscape() && screenHeightDp >= 500
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
    val formWidthModifier = if (isTabletLandscape) Modifier.widthIn(max = 420.dp) else Modifier
    val headerSpacerHeight = if (isTabletLandscape) 8.dp else 16.dp
    val fieldSpacerHeight = if (isTabletLandscape) 6.dp else 8.dp
    val codeRequiredMessage = stringResource(R.string.code_required)
    val codeInvalidMessage = stringResource(R.string.code_invalid)

    uiState.error?.let {
        AppMessageDialog(
            type = AppDialogType.Error,
            message = it,
            onDismiss = onClearError
        )
    }

    uiState.successMessage?.let { msg ->
        val localizedMessage = msg.asString()
        AppMessageDialog(
            type = AppDialogType.Success,
            message = localizedMessage,
            onDismiss = onClearSuccess
        )
    }

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
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.verify_account),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.verification_info),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.check_spam),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
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
                                    if (validateCode(
                                            code.trim(),
                                            codeRequiredMessage,
                                            codeInvalidMessage,
                                            { codeError = it }
                                        )
                                    ) {
                                        onVerify(code.trim())
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppButton(
                            onClick = {
                                if (validateCode(
                                        code.trim(),
                                        codeRequiredMessage,
                                        codeInvalidMessage,
                                        { codeError = it }
                                    )
                                ) {
                                    onVerify(code.trim())
                                }
                            },
                            text = stringResource(R.string.verify_account_title),
                            enabled = !uiState.isLoading,
                            loading = uiState.isLoading,
                            fullWidth = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextButton(
                            onClick = onResendCode,
                            text = stringResource(R.string.resend_code),
                            enabled = !uiState.isLoading
                        )

                        AppTextButton(
                            onClick = onNavigateToLogin,
                            text = stringResource(R.string.back_to_login),
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
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing, Alignment.CenterVertically                    )
                ) {
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
                    Spacer(modifier = Modifier.height(headerSpacerHeight))

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
                                if (validateCode(
                                        code.trim(),
                                        codeRequiredMessage,
                                        codeInvalidMessage,
                                        { codeError = it }
                                    )
                                ) {
                                    onVerify(code.trim())
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(fieldSpacerHeight))

                    AppButton(
                        onClick = {
                            if (validateCode(
                                    code.trim(),
                                    codeRequiredMessage,
                                    codeInvalidMessage,
                                    { codeError = it }
                                )
                            ) {
                                onVerify(code.trim())
                            }
                        },
                        text = stringResource(R.string.verify_account_title),
                        enabled = !uiState.isLoading,
                        loading = uiState.isLoading,
                        fullWidth = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppTextButton(
                        onClick = onResendCode,
                        text = stringResource(R.string.resend_code),
                        enabled = !uiState.isLoading
                    )

                    AppTextButton(
                        onClick = onNavigateToLogin,
                        text = stringResource(R.string.back_to_login),
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
    }
}

private fun validateCode(
    code: String,
    codeRequiredMessage: String,
    codeInvalidMessage: String,
    setCodeError: (String?) -> Unit
): Boolean {
    if (code.isBlank()) {
        setCodeError(codeRequiredMessage)
        return false
    }

    if (code.length < 6) {
        setCodeError(codeInvalidMessage)
        return false
    }

    return true
}
