package com.example.edu_go.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.ui.viewmodel.CourseContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseContentScreen(
    navController: NavController,
    courseId: String
) {
    val viewModel: CourseContentViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(courseId) {
        viewModel.loadCourseContent(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.course?.nombre_curso ?: "Cargando curso...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.course != null) {

                Column(modifier = Modifier.fillMaxSize()) {

                    val selectedTask = uiState.selectedTask


                    // Buscamos si la tarea actual tiene un video
                    val videoResource = selectedTask?.recursos?.find { it.tipo?.lowercase() == "video" }
                    val rawVideoUrl = videoResource?.url ?: videoResource?.archivo_url

                    if (rawVideoUrl != null) {

                        val finalVideoUrl = getStreamableUrl(rawVideoUrl)

                        Column {
                            // 2. Aquí incrustamos el componente VideoPlayer
                            VideoPlayer(
                                videoUrl = finalVideoUrl,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Barra de título pequeña debajo del video
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFAFAFA))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = selectedTask?.titulo ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Black
                                )
                            }
                        }

                    } else {
                        // CASO B: NO HAY VIDEO -> MOSTRAR CAJA NEGRA (Solo Título)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A1A1A)) // Fondo oscuro estilo cine
                                .padding(24.dp)
                        ) {
                            Column {
                                Text(
                                    text = "LECCIÓN ACTUAL",
                                    color = Color(0xFF00C853),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = selectedTask?.titulo ?: "Selecciona una clase",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // 2. CONTENIDO DESPLAZABLE (Instrucciones + Playlist)
                    Column(
                        modifier = Modifier
                            .weight(1f) // Ocupa el resto de la pantalla
                            .verticalScroll(rememberScrollState())
                    ) {

                        // A. INSTRUCCIONES Y RECURSOS DE LA TAREA SELECCIONADA
                        if (selectedTask != null) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                // Instrucciones (Texto)
                                if (!selectedTask.instrucciones.isNullOrBlank()) {
                                    Text("Sobre esta clase:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = selectedTask.instrucciones,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Lista de Recursos (Botones) - Filtramos el video principal para no duplicarlo abajo
                                val otrosRecursos = selectedTask.recursos.filter { it != videoResource }

                                if (otrosRecursos.isNotEmpty()) {
                                    Text("Archivos y enlaces:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    otrosRecursos.forEach { recurso ->
                                        ResourceButton(recurso = recurso, onClick = { url ->
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        })
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                HorizontalDivider(color = Color(0xFFF0F0F0))
                            }
                        }

                        // B. PLAYLIST (TEMARIO)
                        Text(
                            text = "Contenido del Curso",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        uiState.course!!.secciones.forEach { seccion ->
                            PlaylistSectionItem(
                                seccion = seccion,
                                currentTaskId = selectedTask?.id,
                                onTaskClick = { tarea -> viewModel.selectTask(tarea) }
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${uiState.error}", color = Color.Red)
                }
            }
        }
    }
}

fun getStreamableUrl(originalUrl: String): String {
    if (originalUrl.contains("drive.google.com")) {
        val pattern = "/d/([^/]+)".toRegex()
        val match = pattern.find(originalUrl)
        val fileId = match?.groupValues?.get(1)
        if (fileId != null) {
            return "https://drive.google.com/uc?export=download&id=$fileId"
        }
    }
    return originalUrl
}

// --- COMPONENTE: BOTÓN DE RECURSO ---
@Composable
fun ResourceButton(recurso: SupabaseService.Recurso, onClick: (String) -> Unit) {
    val urlDestino = recurso.url ?: recurso.archivo_url ?: ""
    val icon = when (recurso.tipo?.lowercase()) {
        "video" -> Icons.Default.PlayCircleFilled
        "pdf" -> Icons.Default.Article
        else -> Icons.Default.Link
    }

    OutlinedButton(
        onClick = { if (urlDestino.isNotEmpty()) onClick(urlDestino) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF00C853))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = recurso.nombre_archivo ?: "Recurso",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- COMPONENTE: SECCIÓN DE PLAYLIST (ACORDEÓN) ---
@Composable
fun PlaylistSectionItem(
    seccion: SupabaseService.Seccion,
    currentTaskId: String?,
    onTaskClick: (SupabaseService.Tarea) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9F9F9))
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = seccion.nombre_seccion,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1A1A)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))

        AnimatedVisibility(visible = expanded) {
            Column {
                seccion.tareas.forEach { tarea ->
                    val isSelected = tarea.id == currentTaskId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color(0xFFE3F2FD) else Color.White)
                            .clickable { onTaskClick(tarea) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.PlayCircleFilled,
                                contentDescription = "Reproduciendo",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayCircleOutline,
                                contentDescription = "Pendiente",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = tarea.titulo,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF2196F3) else Color(0xFF424242)
                            )
                        )
                    }
                    HorizontalDivider(color = Color(0xFFFAFAFA))
                }
            }
        }
    }
}