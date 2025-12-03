package com.example.edu_go.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_go.data.remote.SupabaseService
// Importamos las clases de datos para que el código sea más limpio
import com.example.edu_go.data.remote.SupabaseService.TarjetaRequest
import com.example.edu_go.data.remote.SupabaseService.TarjetaSimulada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val supabaseService = SupabaseService()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    // 1. CARGAR PERFIL
    fun loadUserProfile(userId: String, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

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

    // 2. ACTUALIZAR DATOS DE TEXTO
    fun updateProfile(userId: String, token: String, nombre: String, apellido: String) {
        viewModelScope.launch {
            val result = supabaseService.updateUserProfile(userId, token, nombre, apellido)
            if (result.isSuccess) {
                loadUserProfile(userId, token)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "No se pudo actualizar el perfil: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    // 3. SUBIR FOTO DE PERFIL
    fun uploadProfileImage(context: Context, userId: String, token: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val fileName = "$userId-${System.currentTimeMillis()}.jpg"
                    val uploadResult = supabaseService.uploadAvatar(token, bytes, fileName)

                    if (uploadResult.isSuccess) {
                        val publicUrl = uploadResult.getOrNull() ?: ""
                        val updateDbResult = supabaseService.updateUserPhoto(userId, token, publicUrl)

                        if (updateDbResult.isSuccess) {
                            loadUserProfile(userId, token)
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al guardar URL")
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al subir imagen")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error: ${e.message}")
            }
        }
    }

    // 4. CARGAR TARJETAS DEL USUARIO
    fun loadUserCards(userId: String, token: String) {
        viewModelScope.launch {
            // Llamamos al servicio
            val result = supabaseService.getUserCards(userId, token)

            if (result.isSuccess) {
                val cardsList = result.getOrNull() ?: emptyList()
                // Actualizamos la lista de tarjetas en el estado
                _uiState.value = _uiState.value.copy(
                    cards = cardsList
                )
            }
        }
    }

    // 5. AGREGAR NUEVA TARJETA
    fun addNewCard(token: String, userId: String, number: String, holder: String, expiry: String, cvv: String, type: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val newCard = TarjetaRequest(
                usuario_id = userId,
                numero_tarjeta = number,
                titular = holder,
                fecha_vencimiento = expiry,
                cvv = cvv,
                tipo = type,
                saldo_simulado = 500.00
            )

            val result = supabaseService.addCard(token, newCard)

            if (result.isSuccess) {
                loadUserCards(userId, token)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al guardar tarjeta")
            }
        }
    }

    //  6. ELIMINAR TARJETA
    fun deleteCard(token: String, userId: String, cardId: String) {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = supabaseService.deleteCard(token, cardId)

            if (result.isSuccess) {
                // Si se borró correctamente, recargamos la lista para que desaparezca de la UI
                loadUserCards(userId, token)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo eliminar la tarjeta"
                )
            }
        }
    }
}

// ESTADO UI
data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: com.example.edu_go.data.remote.SupabaseService.UserProfile? = null,
    val cards: List<TarjetaSimulada> = emptyList(),
    val error: String? = null
)