package com.example.edu_go.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.example.edu_go.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {}
) {
    val viewModel: RegisterViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados para errores de validación
    var nameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Validación en tiempo real de contraseñas coincidentes
    var passwordsMatch by remember { mutableStateOf(true) }

    // Este efecto se ejecuta cuando uiState.success cambia a true
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            Toast.makeText(
                context,
                "¡Registro exitoso! Revisa tu correo para verificar tu cuenta.",
                Toast.LENGTH_LONG
            ).show()

            // Espera un momento para que el usuario vea el toast
            kotlinx.coroutines.delay(1500)

            // Navega al login
            onNavigateToLogin()
        }
    }

    // Función para validar el formulario
    fun validateForm(): Boolean {
        var isValid = true

        // Validar nombre
        if (name.isBlank()) {
            nameError = "El nombre es obligatorio"
            isValid = false
        } else {
            nameError = null
        }

        // Validar apellido
        if (lastName.isBlank()) {
            lastNameError = "El apellido es obligatorio"
            isValid = false
        } else {
            lastNameError = null
        }

        // Validar correo
        if (email.isBlank()) {
            emailError = "El correo es obligatorio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Correo no válido"
            isValid = false
        } else {
            emailError = null
        }

        // Validar contraseña
        if (password.isBlank()) {
            passwordError = "La contraseña es obligatoria"
            isValid = false
        } else if (password.length < 8) {
            passwordError = "La contraseña debe tener al menos 8 caracteres"
            isValid = false
        } else {
            passwordError = null
        }

        // Validar contraseña repetida
        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Confirma tu contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Las contraseñas no coinciden"
            passwordsMatch = false
            isValid = false
        } else {
            confirmPasswordError = null
            passwordsMatch = true
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (it.isNotBlank()) nameError = null // Limpiar error cuando se escribe
            },
            label = { Text("Nombre") },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                if (it.isNotBlank()) lastNameError = null // Limpiar error cuando se escribe
            },
            label = { Text("Apellido") },
            isError = lastNameError != null,
            supportingText = lastNameError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (it.isNotBlank()) emailError = null // Limpiar error cuando se escribe
            },
            label = { Text("Correo Electrónico") },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (it.isNotBlank()) passwordError = null // Limpiar error cuando se escribe
                // Validar contraseñas coincidentes en tiempo real
                if (confirmPassword.isNotBlank() && it != confirmPassword) {
                    confirmPasswordError = "Las contraseñas no coinciden"
                    passwordsMatch = false
                } else if (confirmPassword.isNotBlank() && it == confirmPassword) {
                    confirmPasswordError = null
                    passwordsMatch = true
                }
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                if (it.isNotBlank()) confirmPasswordError = null // Limpiar error cuando se escribe
                // Validar contraseñas coincidentes en tiempo real
                if (it != password) {
                    confirmPasswordError = "Las contraseñas no coinciden"
                    passwordsMatch = false
                } else if (it == password) {
                    confirmPasswordError = null
                    passwordsMatch = true
                }
            },
            label = { Text("Repetir Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmPasswordError != null || !passwordsMatch,
            supportingText = confirmPasswordError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                if (validateForm()) {
                    viewModel.register(name, lastName, email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Inscribirse")
            }
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}

