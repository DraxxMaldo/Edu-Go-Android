package com.example.edu_go.ui.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.data.remote.SupabaseService.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val token = UserSession.token
            val userId = UserSession.userId

            if (token != null && userId != null) {
                val result = supabaseService.getMyFavorites(token, userId)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        courses = result.getOrNull() ?: emptyList(),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudieron cargar tus favoritos"
                    )
                }
            }
        }
    }
}

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val courses: List<Curso> = emptyList(),
    val error: String? = null
)