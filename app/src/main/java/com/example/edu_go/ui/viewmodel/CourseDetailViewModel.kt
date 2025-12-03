package com.example.edu_go.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.data.remote.SupabaseService.Curso
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseDetailViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val token = UserSession.token
            val userId = UserSession.userId

            if (token != null && userId != null) {

                val courseDeferred = async { supabaseService.getCourseById(token, courseId) }
                val enrollmentDeferred = async { supabaseService.isUserEnrolled(token, userId, courseId) }
                // 1. Verificamos si ya es favorito
                val favoriteDeferred = async { supabaseService.checkIsFavorite(token, userId, courseId) }

                // Esperamos todos los resultados
                val courseResult = courseDeferred.await()
                val enrollmentResult = enrollmentDeferred.await()
                val favoriteResult = favoriteDeferred.await()

                if (courseResult.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        course = courseResult.getOrNull(),
                        isEnrolled = enrollmentResult.getOrDefault(false),
                        // 2. Guardamos el estado del favorito
                        isFavorite = favoriteResult.getOrDefault(false),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se encontró el curso"
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Sesión expirada. Por favor, vuelve a iniciar sesión."
                )
            }
        }
    }

    //  FUNCIÓN PARA DAR/QUITAR LIKE
    fun toggleFavorite(courseId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val token = UserSession.token ?: return@launch
            val userId = UserSession.userId ?: return@launch

            // Actualización Optimista: Cambiamos la UI inmediatamente
            val newFavState = !currentState.isFavorite
            _uiState.value = currentState.copy(isFavorite = newFavState)

            // Llamada a la API en segundo plano
            if (newFavState) {
                // Si ahora es true, lo agregamos
                supabaseService.addToFavorites(token, userId, courseId)
            } else {
                // Si ahora es false, lo quitamos
                supabaseService.removeFromFavorites(token, userId, courseId)
            }
        }
    }
}

//  ESTADO ACTUALIZADO
data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val course: Curso? = null,
    val error: String? = null,
    val isEnrolled: Boolean = false,
    val isFavorite: Boolean = false
)