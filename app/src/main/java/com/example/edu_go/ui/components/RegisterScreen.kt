package com.example.edu_go.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edu_go.R
import com.example.edu_go.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {}
) {
    // --- COLORES CORPORATIVOS ---
    val colorPink = Color(0xFFFF4D63)
    val colorOrange = Color(0xFF673AB7)
    val colorText = Color(0xFF5A5A5A)
    val colorBg = Color.White

    val viewModel: RegisterViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Estados
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Errores
    var nameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            Toast.makeText(context, "¡Cuenta creada! Verifica tu correo.", Toast.LENGTH_LONG).show()
            onNavigateToLogin()
        }
    }

    fun validateForm(): Boolean {
        var isValid = true
        // Limpiar errores previos
        nameError = null; lastNameError = null; emailError = null; passwordError = null; confirmPasswordError = null

        if (name.isBlank()) { nameError = "Obligatorio"; isValid = false }
        if (lastName.isBlank()) { lastNameError = "Obligatorio"; isValid = false }

        if (email.isBlank()) { emailError = "Obligatorio"; isValid = false }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailError = "Correo no válido"; isValid = false }

        if (password.isBlank()) { passwordError = "Obligatoria"; isValid = false }
        else if (password.length < 8) { passwordError = "Mín. 8 caracteres"; isValid = false }

        if (confirmPassword.isBlank()) { confirmPasswordError = "Confirma contraseña"; isValid = false }
        else if (password != confirmPassword) { confirmPasswordError = "No coinciden"; isValid = false }

        return isValid
    }

    // Estilos de Input Reutilizables
    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colorPink,
        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
        focusedLabelColor = colorPink,
        cursorColor = colorPink,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBg)
    ) {
        // 1. HEADER ONDULADO (Más alto en registro para que se vea bonito)
        WavyHeader(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.TopCenter),
            colorStart = colorOrange,
            colorEnd = colorPink
        )

        // 2. TÍTULO SUPERIOR (Sobre el color)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "Únete a nuestra comunidad educativa",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f))
            )
        }

        // 3. TARJETA FLOTANTE CON EL FORMULARIO
        // Usamos Surface para darle elevación (sombra)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp), // Empieza más abajo del header
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            shadowElevation = 8.dp // Sombra suave
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Logo pequeño centrado (opcional, o foto de perfil)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.logo_edugopng), contentDescription = null)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- FORMULARIO ---

                // Nombre y Apellido
                OutlinedTextField(
                    value = name, onValueChange = { name = it; nameError = null },
                    label = { Text("Nombre") }, isError = nameError != null,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = inputColors, leadingIcon = { Icon(Icons.Default.Person, null, tint = colorPink) },
                    singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    supportingText = { if (nameError != null) Text(nameError!!) }
                )

                OutlinedTextField(
                    value = lastName, onValueChange = { lastName = it; lastNameError = null },
                    label = { Text("Apellido") }, isError = lastNameError != null,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = inputColors, leadingIcon = { Icon(Icons.Default.Person, null, tint = colorPink) },
                    singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    supportingText = { if (lastNameError != null) Text(lastNameError!!) }
                )

                OutlinedTextField(
                    value = email, onValueChange = { email = it; emailError = null },
                    label = { Text("Correo Electrónico") }, isError = emailError != null,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = inputColors, leadingIcon = { Icon(Icons.Default.Email, null, tint = colorPink) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    supportingText = { if (emailError != null) Text(emailError!!) }
                )

                OutlinedTextField(
                    value = password, onValueChange = { password = it; passwordError = null },
                    label = { Text("Contraseña") }, isError = passwordError != null,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = inputColors, leadingIcon = { Icon(Icons.Default.Lock, null, tint = colorPink) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = Color.Gray)
                        }
                    },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    supportingText = { if (passwordError != null) Text(passwordError!!) }
                )

                OutlinedTextField(
                    value = confirmPassword, onValueChange = { confirmPassword = it; confirmPasswordError = null },
                    label = { Text("Confirmar Contraseña") }, isError = confirmPasswordError != null,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = inputColors, leadingIcon = { Icon(Icons.Default.Lock, null, tint = colorPink) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    supportingText = { if (confirmPasswordError != null) Text(confirmPasswordError!!) }
                )

                // Error General
                if (uiState.error != null) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BOTÓN REGISTRO ---
                Button(
                    onClick = { if (validateForm()) viewModel.register(name, lastName, email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(8.dp, RoundedCornerShape(50), spotColor = colorPink),
                    shape = RoundedCornerShape(50),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = colorPink, contentColor = Color.White)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("REGISTRARSE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- FOOTER VOLVER ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "¿Ya tienes cuenta? ", color = Color.Gray)
                    Text(
                        text = "Inicia Sesión",
                        color = colorOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(60.dp)) // Espacio final para scroll
            }
        }
    }
}




@Composable
fun WavyHeader(
    modifier: Modifier = Modifier,
    colorStart: Color,
    colorEnd: Color
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val gradient = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(colorStart, colorEnd),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(width, height)
        )

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, height * 0.75f)
            cubicTo(
                width * 0.4f, height * 0.95f,
                width * 0.6f, height * 0.50f,
                width, height * 0.70f
            )
            lineTo(width, 0f)
            close()
        }

        drawPath(path = path, brush = gradient)

        // Líneas decorativas (opcional)
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, height * 0.60f)
                cubicTo(
                    width * 0.3f, height * 0.80f,
                    width * 0.7f, height * 0.40f,
                    width, height * 0.60f
                )
                lineTo(width, 0f)
                close()
            },
            color = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}