package com.example.edu_go.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.remote.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    // 👇 Agrega 'token' como parámetro
    fun loadUserProfile(userId: String, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // 👇 Pasamos el token al servicio corregido
            val profileResult = supabaseService.getUserProfile(userId, token)

            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = profileResult.exceptionOrNull()?.message
                )
            }
        }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: com.example.edu_go.data.remote.SupabaseService.UserProfile? = null,
    val error: String? = null
)