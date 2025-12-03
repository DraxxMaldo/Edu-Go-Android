package com.example.edu_go.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.edu_go.R
import com.example.edu_go.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val miColorRosado = Color(0xFFFF6B8E)
    val viewModel: LoginViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Error Handling
    val isErrorLogin = uiState.error != null
    val errorMessage = if (isErrorLogin) {
        if (uiState.error!!.contains("Invalid login credentials", ignoreCase = true)) {
            "Correo o contraseña incorrectos"
        } else {
            "Error: ${uiState.error}"
        }
    } else null

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            Toast.makeText(context, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
            onNavigateToHome()
        }
    }

    // Usamos Box para tener el fondo de color detrás
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(miColorRosado)
    ) {
        // --- 1. BANNER / HEADER SUPERIOR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 60.dp), // Espacio desde arriba
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fondo blanco circular detrás del logo para que resalte
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color.White, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_edugopng),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¡Hola de nuevo!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "Ingresa para continuar aprendiendo",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                )
            )
        }

        // --- 2. TARJETA DEL FORMULARIO (BOTTOM SHEET STYLE) ---
        // Esta superficie blanca ocupa la parte inferior y tiene esquinas redondeadas arriba
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f) // Ocupa el 65% inferior de la pantalla
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            // Hacemos el contenido scrolleable por si pantallas pequeñas
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 24.dp).align(Alignment.Start)
                )

                // --- INPUT EMAIL ---
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), // Bordes redondeados en el input
                    isError = isErrorLogin,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = miColorRosado) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = miColorRosado,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = miColorRosado,
                        cursorColor = miColorRosado,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- INPUT PASSWORD ---
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = isErrorLogin,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.login(email, password) }),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = miColorRosado) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle password", tint = if (isErrorLogin) MaterialTheme.colorScheme.error else Color.Gray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = miColorRosado,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = miColorRosado,
                        cursorColor = miColorRosado,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )

                // ERROR MESSAGE
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- BOTÓN LOGIN (PILL SHAPE) ---
                Button(
                    onClick = { focusManager.clearFocus(); viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50), // Botón completamente redondo
                    enabled = !uiState.isLoading && email.isNotEmpty() && password.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = miColorRosado,
                        contentColor = Color.White,
                        disabledContainerColor = miColorRosado.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("INGRESAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- DIVIDER ---
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(" o ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BOTÓN REGISTRO (OUTLINED PILL) ---
                OutlinedButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, miColorRosado),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = miColorRosado)
                ) {
                    Text("CREAR CUENTA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}