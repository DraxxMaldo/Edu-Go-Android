package com.example.edu_go.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.ui.viewmodel.CourseDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseId: String
) {
    val viewModel: CourseDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cargar datos al entrar
    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    Scaffold(

        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        bottomBar = {
            if (uiState.course != null) {
                BottomAppBar(
                    containerColor = Color.White,
                    contentPadding = PaddingValues(16.dp),
                    tonalElevation = 8.dp
                ) {
                    if (uiState.isEnrolled) {
                        Button(
                            onClick = {
                                navController.navigate("course_content/${uiState.course!!.id}")
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Continuar Curso", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { navController.navigate("checkout/${uiState.course!!.id}") },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val precioFormateado = String.format("%.2f", uiState.course!!.precio)
                            Text("Comprar por $$precioFormateado", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.course != null) {
                val curso = uiState.course!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- 1. BANNER ---
                    Box(modifier = Modifier.height(250.dp).fillMaxWidth()) {
                        if (curso.banner_url != null) {
                            AsyncImage(
                                model = curso.banner_url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
                        }

                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                        }

                        // 👇 BOTÓN FAVORITO CON ACCIÓN Y SNACKBAR
                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(courseId)
                                // Mostrar Snackbar
                                scope.launch {
                                    val mensaje = if (!uiState.isFavorite) "Eliminado de favoritos" else "Agregado a Favoritos"
                                    snackbarHostState.currentSnackbarData?.dismiss() // Cerrar anterior si hay
                                    snackbarHostState.showSnackbar(mensaje, duration = SnackbarDuration.Short)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (uiState.isFavorite) Color(0xFFFF6B8E) else Color.Gray
                            )
                        }
                    }

                    // --- 2. INFORMACIÓN DEL CURSO ---
                    Column(modifier = Modifier.padding(24.dp)) {
                        Surface(
                            color = Color(0xFFFF6B8E).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = curso.categoria.uppercase(),
                                color = Color(0xFFFF6B8E),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = curso.nombre_curso,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF1A1A1A)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val profile = curso.profiles
                        val nombreAutor = if (profile != null) "${profile.nombre} ${profile.apellido}" else "Desconocido"

                        Row(verticalAlignment = Alignment.Top) {
                            if (profile?.foto_url != null) {
                                AsyncImage(
                                    model = profile.foto_url,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEEEEEE), CircleShape))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text("Creado por", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(nombreAutor, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                                if (!profile?.descripcion.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = profile?.descripcion!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Sobre este curso", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = curso.descripcion ?: "Sin descripción disponible.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF616161),
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- BENEFICIOS ---
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AllInclusive, null, modifier = Modifier.size(24.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Acceso de por vida", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1A1A1A))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Smartphone, null, modifier = Modifier.size(24.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Acceso desde dispositivos móviles", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1A1A1A))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, null, modifier = Modifier.size(24.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Archivos auxiliares", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1A1A1A))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(24.dp))

                        // --- TEMARIO ---
                        Text("Contenido del curso", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(16.dp))

                        if (curso.secciones.isEmpty()) {
                            Text("No hay contenido publicado aún.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            curso.secciones.forEach { seccion ->
                                SectionItem(seccion = seccion)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionItem(seccion: SupabaseService.Seccion) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = seccion.nombre_seccion,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.background(Color.White)) {
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    if (seccion.tareas.isEmpty()) {
                        Text(
                            text = "Sin tareas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        seccion.tareas.forEach { tarea ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B8E),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tarea.titulo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF424242)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFFAFAFA))
                        }
                    }
                }
            }
        }
    }
}