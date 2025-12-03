package com.example.edu_go.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.edu_go.R
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.UserStore
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userStore = remember { UserStore(context) }

    val currentToken = UserSession.token
    val currentUserId = UserSession.userId

    // Estado del Scroll
    val scrollState = rememberScrollState()

    // Estados de Diálogos
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteCardDialog by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<SupabaseService.TarjetaSimulada?>(null) }

    LaunchedEffect(Unit) {
        if (currentToken != null && currentUserId != null) {
            profileViewModel.loadUserProfile(userId = currentUserId, token = currentToken)
            profileViewModel.loadUserCards(userId = currentUserId, token = currentToken)
        } else {
            navController.navigate("login") { popUpTo("home") { inclusive = true } }
        }
    }

    // --- DIÁLOGOS (Sin cambios) ---
    if (showEditDialog && profileUiState.profile != null) {
        EditProfileDialog(
            currentName = profileUiState.profile!!.nombre,
            currentLastName = profileUiState.profile!!.apellido,
            currentPhotoUrl = profileUiState.profile!!.foto_url,
            onDismiss = { showEditDialog = false },
            onImageSelected = { uri ->
                if (currentToken != null && currentUserId != null) {
                    profileViewModel.uploadProfileImage(context, currentUserId, currentToken, uri)
                }
            },
            onConfirm = { newName, newLastName ->
                if (currentToken != null && currentUserId != null) {
                    profileViewModel.updateProfile(userId = currentUserId, token = currentToken, nombre = newName, apellido = newLastName)
                }
                showEditDialog = false
            }
        )
    }

    if (showDeleteCardDialog && cardToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteCardDialog = false },
            title = { Text("Eliminar Tarjeta") },
            text = { Text("¿Estás seguro de que deseas eliminar la tarjeta terminada en ${cardToDelete!!.numero_tarjeta.takeLast(4)}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentToken != null && currentUserId != null) {
                            profileViewModel.deleteCard(currentToken, currentUserId, cardToDelete!!.id)
                        }
                        showDeleteCardDialog = false
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCardDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        bottomBar = { EduGoBottomNavigation(navController = navController) }
    ) { paddingValues ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFFFF6B8E))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Cuenta",
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_edugopng),
                    contentDescription = "Logo",
                    modifier = Modifier.size(80.dp).align(Alignment.CenterEnd),
                    contentScale = ContentScale.Fit
                )
            }

            // --- CONTENIDO DEL CUERPO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Texto centrado
                Text(
                    text = "Toca tu tarjeta para editar tu perfil",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .fillMaxWidth()
                )

                // --- TARJETA DE PERFIL ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { if (profileUiState.profile != null) showEditDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val profile = profileUiState.profile
                        if (profileUiState.isLoading && profile == null) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 8.dp), strokeWidth = 2.dp)
                            Text("Cargando...")
                        } else if (profile != null) {
                            val fullName = "${profile.nombre} ${profile.apellido}"
                            val initial = if (fullName.isNotBlank()) fullName.take(1).uppercase() else "U"

                            Box(contentAlignment = Alignment.Center) {
                                if (profile.foto_url != null) {
                                    AsyncImage(
                                        model = profile.foto_url,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(56.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(56.dp).background(Color.DarkGray, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = initial, style = MaterialTheme.typography.headlineSmall.copy(color = Color.White))
                                    }
                                }
                                // Icono editar
                                Box(modifier = Modifier.offset(x = 20.dp, y = 20.dp).background(Color.White, CircleShape).padding(4.dp)) {
                                    Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(14.dp), tint = Color(0xFFFF6B8E))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(profile.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                Text(profile.role.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B8E), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(profileUiState.error ?: "Error", color = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- OTROS (Con clic en Favoritos) ---
                Text("Otros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    Pair(Icons.Default.Bookmark, "Favoritos"),
                    Pair(Icons.Default.Headphones, "Centro de ayuda"),
                    Pair(Icons.Default.Language, "Aplica para profesor")
                ).forEach { (icon, title) ->
                    Card(
                        // 👇 MODIFICACIÓN AQUÍ: Navegamos a 'favorites' si es el título correcto
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (title == "Favoritos") {
                                    navController.navigate("favorites")
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, title, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- FORMAS DE PAGO ---
                Text("Formas de pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                if (profileUiState.cards.isNotEmpty()) {
                    profileUiState.cards.forEach { card ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, "Tarjeta", tint = Color.DarkGray)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("**** ${card.numero_tarjeta.takeLast(4)}", fontWeight = FontWeight.Bold)
                                    Text("${card.tipo} • Exp: ${card.fecha_vencimiento}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text("$${card.saldo_simulado}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                IconButton(onClick = { cardToDelete = card; showDeleteCardDialog = true }) {
                                    Icon(Icons.Default.Delete, "Borrar", tint = Color.Red.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                } else {
                    Text("No tienes tarjetas guardadas", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                }

                // Botón Agregar Tarjeta
                OutlinedButton(
                    onClick = { navController.navigate("add_card") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar nueva tarjeta", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- LOGOUT ---
                Button(
                    onClick = {
                        scope.launch {
                            userStore.clearSession()
                            UserSession.token = null
                            UserSession.userId = null
                            navController.navigate("login") { popUpTo("home") { inclusive = true } }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B8E), contentColor = Color.White)
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}