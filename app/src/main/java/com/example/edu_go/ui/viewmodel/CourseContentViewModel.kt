package com.example.edu_go.ui.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.data.remote.SupabaseService.Curso
import com.example.edu_go.data.remote.SupabaseService.Tarea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseContentViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(CourseContentUiState())
    val uiState: StateFlow<CourseContentUiState> = _uiState

    fun loadCourseContent(courseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val token = UserSession.token

            if (token != null) {
                // Reutilizamos getCourseById porque ahora trae TODO (instrucciones y recursos)
                val result = supabaseService.getCourseById(token, courseId)

                if (result.isSuccess) {
                    val curso = result.getOrNull()

                    // Lógica para seleccionar automáticamente la primera clase
                    val primeraTarea = curso?.secciones?.firstOrNull()?.tareas?.firstOrNull()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        course = curso,
                        selectedTask = primeraTarea, // Auto-seleccionar la primera
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo cargar el contenido del curso"
                    )
                }
            }
        }
    }

    // Función para cambiar de clase al hacer clic en el menú
    fun selectTask(tarea: Tarea) {
        _uiState.value = _uiState.value.copy(selectedTask = tarea)
    }
}

data class CourseContentUiState(
    val isLoading: Boolean = false,
    val course: Curso? = null,
    val selectedTask: Tarea? = null, // La clase que se está viendo actualmente
    val error: String? = null
)