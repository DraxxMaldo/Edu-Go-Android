package com.example.edu_go.ui.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.data.remote.SupabaseService.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyCoursesViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(MyCoursesUiState())
    val uiState: StateFlow<MyCoursesUiState> = _uiState

    fun loadMyCourses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val token = UserSession.token
            val userId = UserSession.userId

            if (token != null && userId != null) {
                val result = supabaseService.getEnrolledCourses(token, userId)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        courses = result.getOrNull() ?: emptyList(),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudieron cargar tus cursos"
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Sesión inválida")
            }
        }
    }
}

data class MyCoursesUiState(
    val isLoading: Boolean = false,
    val courses: List<Curso> = emptyList(),
    val error: String? = null
)