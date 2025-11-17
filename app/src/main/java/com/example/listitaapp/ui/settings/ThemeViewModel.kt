package com.example.listitaapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.data.repository.ThemePreferencesRepository
import com.example.listitaapp.domain.model.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemeUiState(
    val preference: ThemePreference = ThemePreference.SYSTEM
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: ThemePreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.themePreference.collect { preference ->
                _uiState.update { ThemeUiState(preference) }
            }
        }
    }

    fun updatePreference(preference: ThemePreference) {
        viewModelScope.launch {
            repository.setThemePreference(preference)
        }
    }
}
