package com.example.edu_go.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.remote.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {
    private val authService = AuthService()

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState

    fun loadCurrentUser(sessionToken: String) {
        viewModelScope.launch {
            _sessionState.value = _sessionState.value.copy(isLoading = true, error = null)

            val userResult = authService.getCurrentUser(sessionToken)

            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                if (user != null) {
                    _sessionState.value = _sessionState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        error = null
                    )
                } else {
                    _sessionState.value = _sessionState.value.copy(
                        isLoading = false,
                        error = "No se pudo obtener el usuario"
                    )
                }
            } else {
                _sessionState.value = _sessionState.value.copy(
                    isLoading = false,
                    error = userResult.exceptionOrNull()?.message ?: "Error al cargar usuario"
                )
            }
        }
    }
}

data class SessionState(
    val isLoading: Boolean = false,
    val currentUser: com.example.edu_go.data.remote.AuthService.CurrentUser? = null,
    val error: String? = null
)