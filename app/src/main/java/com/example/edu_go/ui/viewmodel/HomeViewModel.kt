package com.example.edu_go.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.data.remote.SupabaseService.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    // 👇 Guardamos una copia de TODOS los cursos para no volver a descargarlos al filtrar
    private var allCoursesBackup: List<Curso> = emptyList()

    // 👇 Variables para mantener el estado de los filtros
    private var currentSearchQuery = ""
    private var currentCategory = "Todos"

    // Función para cargar los cursos desde Supabase
    fun loadCourses(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = supabaseService.getAllCourses(token)

            if (result.isSuccess) {
                val courses = result.getOrNull() ?: emptyList()

                // 1. Guardamos la copia original completa
                allCoursesBackup = courses

                // 2. Aplicamos filtros (por si el usuario ya escribió algo mientras cargaba)
                applyFilters()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudieron cargar los cursos"
                )
            }
        }
    }

    //  Cuando el usuario escribe en el buscador
    fun onSearchQueryChanged(query: String) {
        currentSearchQuery = query
        // Actualizamos el texto en la UI inmediatamente
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // Ejecutamos el filtro
        applyFilters()
    }

    // Cuando el usuario toca una categoría (Chip)
    fun onCategorySelected(category: String) {
        currentCategory = category
        // Actualizamos la categoría seleccionada en la UI
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        // Ejecutamos el filtro
        applyFilters()
    }

    // LÓGICA DE FILTRADO
    private fun applyFilters() {

        val filteredList = allCoursesBackup.filter { curso ->


            val matchesSearch = curso.nombre_curso.contains(currentSearchQuery, ignoreCase = true)

            // 2. Filtro por Categoría
            val matchesCategory = if (currentCategory == "Todos") {
                true // Si es "Todos", pasan todos
            } else {
                // Comparamos ignorando mayúsculas/minúsculas
                curso.categoria.equals(currentCategory, ignoreCase = true)
            }

            // El curso debe cumplir AMBAS condiciones para mostrarse
            matchesSearch && matchesCategory
        }

        // Actualizamos la lista visible en la pantalla (uiState.courses)
        _uiState.value = _uiState.value.copy(courses = filteredList)
    }
}


data class HomeUiState(
    val isLoading: Boolean = false,
    val courses: List<Curso> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Todos",
    val error: String? = null
)