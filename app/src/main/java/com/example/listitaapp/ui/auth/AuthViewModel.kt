package com.example.listitaapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.data.repository.AuthRepository
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
    val error: String? = null,
    val successMessage: String? = null,
    val email: String = "",
    val password: String = "",
    val registrationComplete: Boolean = false,
    val verificationComplete: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            val isAuth = repository.isAuthenticated()
            _uiState.update { it.copy(isAuthenticated = isAuth) }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.login(email, password).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null
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
                    // Registration successful - verification code sent via email
                    // Store email and password temporarily for auto-login after verification
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
                    // Verification successful - now automatically login
                    val email = _uiState.value.email
                    val password = _uiState.value.password

                    if (email.isNotBlank() && password.isNotBlank()) {
                        // Auto-login after successful verification
                        repository.login(email, password).fold(
                            onSuccess = { token ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isAuthenticated = true,
                                        verificationComplete = true,
                                        password = "", // Clear password after successful login
                                        error = null
                                    )
                                }
                            },
                            onFailure = { loginException ->
                                // Verification succeeded but auto-login failed
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        verificationComplete = true,
                                        password = "", // Clear password
                                        error = "Account verified but login failed: ${loginException.message}"
                                    )
                                }
                            }
                        )
                    } else {
                        // No credentials stored, just mark as verified
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                verificationComplete = true,
                                successMessage = "Account verified successfully! Please login.",
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
                            successMessage = "New verification code: $code\n\nCheck the API console as well.",
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
                    password = "", // Clear stored credentials
                    registrationComplete = false,
                    verificationComplete = false
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
                verificationComplete = false,
                error = null,
                successMessage = null
            )
        }
    }
}
