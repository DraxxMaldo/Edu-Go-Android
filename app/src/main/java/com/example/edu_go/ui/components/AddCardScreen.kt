package com.example.edu_go.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.edu_go.data.UserSession
import com.example.edu_go.ui.viewmodel.ProfileViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    navController: NavController
) {
    val viewModel: ProfileViewModel = viewModel()

    // Estados del formulario
    var cardNumber by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") } // MM/YY
    var cvv by remember { mutableStateOf("") }

    // Estado de Error para la fecha
    var isDateError by remember { mutableStateOf(false) }

    val token = UserSession.token
    val userId = UserSession.userId

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agregar Tarjeta") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ingresa los datos de tu tarjeta")

            // --- NÚMERO DE TARJETA ---
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { input ->
                    // Solo acepta números y máximo 16
                    if (input.all { it.isDigit() } && input.length <= 16) {
                        cardNumber = input
                    }
                },
                label = { Text("Número de tarjeta") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                // Muestra error si escribió algo pero no llegó a 16 dígitos
                isError = cardNumber.isNotEmpty() && cardNumber.length < 16,
                supportingText = {
                    if (cardNumber.isNotEmpty() && cardNumber.length < 16) {
                        Text("Debe tener 16 dígitos")
                    }
                }
            )

            // --- TITULAR ---
            OutlinedTextField(
                value = holderName,
                onValueChange = { holderName = it },
                label = { Text("Titular de la tarjeta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                // --- FECHA DE VENCIMIENTO (Con lógica inteligente) ---
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { input ->
                        if (input.length <= 5) {
                            var formatted = input

                            // Auto-agregar la barra '/' si escribe el segundo número
                            if (input.length == 2 && !input.contains("/")) {
                                formatted = "$input/"
                            }
                            // Permitir borrar la barra
                            else if (input.length == 2 && expiryDate.length == 3) {
                                formatted = input.substring(0, 1)
                            }

                            expiryDate = formatted
                            isDateError = false // Reseteamos error al escribir
                        }
                    },
                    label = { Text("Vencimiento (MM/YY)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("MM/YY") },
                    isError = isDateError,
                    supportingText = {
                        if (isDateError) Text("Fecha inválida o vencida")
                    }
                )

                // --- CVV ---
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 3) {
                            cvv = input
                        }
                    },
                    label = { Text("CVV") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // --- BOTÓN GUARDAR ---
            Button(
                onClick = {
                    // Validar la fecha antes de enviar
                    if (!isExpiryDateValid(expiryDate)) {
                        isDateError = true
                        return@Button
                    }

                    if (token != null && userId != null) {
                        // Determinamos tipo simple (Visa/Master)
                        val tipo = if (cardNumber.startsWith("4")) "Visa" else "Mastercard"

                        viewModel.addNewCard(token, userId, cardNumber, holderName, expiryDate, cvv, tipo) {
                            // Al guardar con éxito, volvemos atrás
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                // El botón solo se activa si todo tiene la longitud correcta
                enabled = cardNumber.length == 16 &&
                        holderName.isNotEmpty() &&
                        expiryDate.length == 5 &&
                        cvv.length == 3
            ) {
                Text("Guardar Tarjeta")
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors()
            ) {
                Text("Cancelar")
            }
        }
    }
}

fun isExpiryDateValid(date: String): Boolean {
    if (date.length != 5) return false
    if (!date.contains("/")) return false

    return try {
        val parts = date.split("/")
        val inputMonth = parts[0].toInt()
        val inputYear = parts[1].toInt() + 2000 // Convertir '25' a '2025'

        // Validar mes lógico
        if (inputMonth < 1 || inputMonth > 12) return false

        // Obtener fecha actual
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Enero es 0

        // Comparar
        if (inputYear > currentYear) {
            true // Año futuro es válido
        } else if (inputYear == currentYear) {
            inputMonth >= currentMonth // Mismo año, mes debe ser igual o futuro
        } else {
            false // Año pasado
        }
    } catch (e: Exception) {
        false
    }
}