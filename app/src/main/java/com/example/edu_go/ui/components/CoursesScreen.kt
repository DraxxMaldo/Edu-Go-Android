package com.example.edu_go.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.ui.viewmodel.MyCoursesViewModel

@Composable
fun CoursesScreen(
    navController: NavController
) {
    val viewModel: MyCoursesViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Cargar cursos cada vez que se entra a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadMyCourses()
    }

    Scaffold(
        bottomBar = {
            EduGoBottomNavigation(navController = navController)
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // TÍTULO
            Text(
                text = "Mis Aprendizajes",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.courses.isEmpty()) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Aún no te has inscrito a ningún curso.",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("home") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                        ) {
                            Text("Explorar Cursos")
                        }
                    }
                }
            } else {
                // LISTA DE CURSOS COMPRADOS
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.courses) { curso ->
                        MyCourseCardItem(curso = curso) {
                            // 👇 Al hacer clic, vamos directo al REPRODUCTOR (no al detalle)
                            navController.navigate("course_content/${curso.id}")
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTE: TARJETA DE "MIS CURSOS" (DISEÑO HORIZONTAL) ---
@Composable
fun MyCourseCardItem(
    curso: SupabaseService.Curso,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Imagen a la izquierda (35% del ancho aprox)
            if (curso.banner_url != null) {
                AsyncImage(
                    model = curso.banner_url,
                    contentDescription = null,
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .background(Color.LightGray)
                )
            }

            // 2. Información al centro
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // Categoría pequeña
                Text(
                    text = curso.categoria.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFFFF6B8E),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Nombre del curso
                Text(
                    text = curso.nombre_curso,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nombre del profesor
                val nombreProfe = "${curso.profiles?.nombre ?: ""} ${curso.profiles?.apellido ?: ""}".trim()
                if (nombreProfe.isNotEmpty()) {
                    Text(
                        text = "Por $nombreProfe",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            // 3. Botón Play a la derecha
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "Continuar",
                    tint = Color(0xFF2196F3), // Azul de "Continuar"
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}