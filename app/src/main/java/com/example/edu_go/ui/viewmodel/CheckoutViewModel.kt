package com.example.edu_go.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.data.remote.SupabaseService.Curso
import com.example.edu_go.data.remote.SupabaseService.TarjetaSimulada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState

    // Cargar datos del curso y las tarjetas del usuario
    fun loadCheckoutData(courseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val token = UserSession.token ?: return@launch
            val userId = UserSession.userId ?: return@launch

            // 1. Obtener curso
            val courseResult = supabaseService.getCourseById(token, courseId)
            // 2. Obtener tarjetas
            val cardsResult = supabaseService.getUserCards(userId, token)

            if (courseResult.isSuccess && cardsResult.isSuccess) {
                val cards = cardsResult.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    course = courseResult.getOrNull(),
                    userCards = cards,
                    // Seleccionar la primera tarjeta por defecto si existe
                    selectedCard = cards.firstOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error cargando datos")
            }
        }
    }

    fun selectCard(card: TarjetaSimulada) {
        _uiState.value = _uiState.value.copy(selectedCard = card)
    }


    fun processPurchase(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val course = currentState.course
            val card = currentState.selectedCard
            val token = UserSession.token
            val userId = UserSession.userId // 1. Necesitamos el ID del usuario

            if (course == null || card == null || token == null || userId == null) return@launch

            _uiState.value = currentState.copy(isProcessing = true)

            // 2. Verificación local rápida (UI): ¿Tiene saldo suficiente?
            if (card.saldo_simulado < course.precio) {
                _uiState.value = currentState.copy(
                    isProcessing = false,
                    error = "Saldo insuficiente en la tarjeta"
                )
                return@launch
            }

            // 3. EJECUTAR COMPRA (Insertar Inscripción)

            val result = supabaseService.enrollCourse(
                token = token,
                userId = userId,
                courseId = course.id,
                precio = course.precio
            )

            if (result.isSuccess) {
                _uiState.value = currentState.copy(isProcessing = false, purchaseSuccess = true)
                onSuccess()
            } else {
                // Si falla, mostramos el error
                val errorMsg = result.exceptionOrNull()?.message ?: "Error al procesar la compra"
                _uiState.value = currentState.copy(
                    isProcessing = false,
                    error = errorMsg
                )
            }
        }
    }
}

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val course: Curso? = null,
    val userCards: List<TarjetaSimulada> = emptyList(),
    val selectedCard: TarjetaSimulada? = null,
    val error: String? = null,
    val purchaseSuccess: Boolean = false
)