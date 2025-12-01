package com.example.edu_go.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.edu_go.data.UserSession // 👈 Importante
import com.example.edu_go.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()

    // 1. Obtenemos el token y ID guardados en el Singleton (Memoria RAM)
    val currentToken = UserSession.token
    val currentUserId = UserSession.userId

    // 2. Cargamos el perfil automáticamente al entrar
    LaunchedEffect(Unit) {
        if (currentToken != null && currentUserId != null) {
            // ✅ Llamamos a la función pasando el token real
            profileViewModel.loadUserProfile(userId = currentUserId, token = currentToken)
        } else {
            // ⚠️ Si no hay sesión (ej: recargaste la app y se borró la RAM),
            // mandamos al usuario al login para que entre de nuevo.
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            EduGoBottomNavigation(navController = navController)
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFFF6B8E))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Cuenta",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "EduGo",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            // --- TÍTULO ---
            Text(
                text = "Acá podrás observar y editar tú perfil",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.Start)
            )

            // --- TARJETA DE PERFIL (Dinámica) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val profile = profileUiState.profile

                    // A) ESTADO CARGANDO
                    if (profileUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Cargando información...")
                    }
                    // B) ESTADO CON DATOS (ÉXITO)
                    else if (profile != null) {
                        // Inicial del nombre
                        val fullName = "${profile.nombre} ${profile.apellido}"
                        val initial = if (fullName.isNotBlank()) fullName.take(1).uppercase() else "U"

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.DarkGray, shape = RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = profile.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                            // Mostrar Rol (Opcional)
                            Text(
                                text = profile.role.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF6B8E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // C) ESTADO ERROR O VACÍO
                    else {
                        Text(
                            text = profileUiState.error ?: "No se pudo cargar el perfil",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // --- SECCIÓN OTROS ---
            Text(
                text = "Otros",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 8.dp)
                    .align(Alignment.Start)
            )

            listOf(
                Pair(Icons.Default.Bookmark, "Favoritos"),
                Pair(Icons.Default.Headphones, "Centro de ayuda"),
                Pair(Icons.Default.Language, "Aplica para profesor")
            ).forEach { (icon, title) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { /* Acción futura */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = icon, contentDescription = title, tint = Color.Black)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }

            // --- SECCIÓN FORMAS DE PAGO ---
            Text(
                text = "Formas de pago",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 8.dp)
                    .align(Alignment.Start)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { /* Acción futura */ },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = "Tarjeta", tint = Color.Black)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Agregar tarjeta de débito", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }

            // --- BOTÓN CERRAR SESIÓN ---
            Button(
                onClick = {
                    // 1. Limpiar sesión en memoria
                    UserSession.token = null
                    UserSession.userId = null

                    // 2. Navegar al Login y borrar historial
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B8E),
                    contentColor = Color.White
                )
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}