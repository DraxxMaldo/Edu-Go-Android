package com.example.edu_go.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.UserStore
import com.example.edu_go.data.remote.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val supabaseService = SupabaseService()
    private val userStore = UserStore(application.applicationContext)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = supabaseService.signIn(email, pass)

            if (result.isSuccess) {
                val loginData = result.getOrNull()

                if (loginData != null) {
                    // 1. Guardar en RAM (UserSession)
                    UserSession.token = loginData.token
                    UserSession.userId = loginData.userId

                    // 2. Guardar en DISCO (DataStore) para persistencia 💾
                    userStore.saveUser(loginData.token, loginData.userId)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = true,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error: Datos de sesión vacíos"
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error desconocido"
                )
            }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)