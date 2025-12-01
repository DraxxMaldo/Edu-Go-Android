package com.example.edu_go.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.data.UserSession // 👈 Importante: Importar esto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Llamamos al servicio
            val result = supabaseService.signIn(email, pass)

            if (result.isSuccess) {
                // Obtenemos los datos que nos devolvió el servicio (ID y Token)
                val loginData = result.getOrNull()

                if (loginData != null) {
                    // 👇 AQUÍ ES DONDE GUARDAMOS EL TOKEN (El paso clave)
                    UserSession.token = loginData.token
                    UserSession.userId = loginData.userId

                    // Ahora sí, avisamos a la vista que todo salió bien
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