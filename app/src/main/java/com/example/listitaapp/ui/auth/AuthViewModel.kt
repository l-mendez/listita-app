package com.example.listitaapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.R
import com.example.listitaapp.data.repository.AuthRepository
import com.example.listitaapp.ui.common.UiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isInitialAuthCheckComplete: Boolean = false,
    val error: String? = null,
    val successMessage: UiMessage? = null,
    val email: String = "",
    val password: String = "",
    val registrationComplete: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthentication()
        observeAuthState()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            val isAuth = repository.isAuthenticated()
            _uiState.update { 
                it.copy(
                    isAuthenticated = isAuth,
                    isInitialAuthCheckComplete = true
                )
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            repository.authState().collect { isAuth ->
                _uiState.update { it.copy(isAuthenticated = isAuth) }
            }
        }
    }
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.login(email, password).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Login failed"
                        )
                    }
                }
            )
        }
    }

    fun register(email: String, password: String, name: String, surname: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.register(email, password, name, surname).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            email = email,
                            password = password,
                            registrationComplete = true,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Registration failed"
                        )
                    }
                }
            )
        }
    }

    fun verifyAccount(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.verifyAccount(code).fold(
                onSuccess = { user ->
                    val email = _uiState.value.email
                    val password = _uiState.value.password

                    if (email.isNotBlank() && password.isNotBlank()) {
                        repository.login(email, password).fold(
                            onSuccess = { token ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isAuthenticated = true,
                                        password = "",
                                        error = null
                                    )
                                }
                            },
                            onFailure = { loginException ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        password = "",
                                        error = "Account verified but login failed: ${loginException.message}"
                                    )
                                }
                            }
                        )
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = UiMessage(resId = R.string.account_verified_login),
                                error = null
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Verification failed"
                        )
                    }
                }
            )
        }
    }

    fun resendVerificationCode() {
        viewModelScope.launch {
            val currentEmail = _uiState.value.email
            if (currentEmail.isBlank()) {
                _uiState.update {
                    it.copy(error = "Email not found. Please register again.")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.resendVerification(currentEmail).fold(
                onSuccess = { code ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = UiMessage(
                                resId = R.string.new_verification_code,
                                formatArgs = listOf(code)
                            ),
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to resend code"
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                it.copy(
                    isAuthenticated = false,
                    email = "",
                    password = "",
                    registrationComplete = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun resetRegistrationState() {
        _uiState.update {
            it.copy(
                registrationComplete = false,
                error = null,
                successMessage = null
            )
        }
    }
}
