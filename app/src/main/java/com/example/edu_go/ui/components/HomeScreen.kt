package com.example.edu_go.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.remote.SupabaseService
import com.example.edu_go.ui.viewmodel.HomeViewModel
import com.example.edu_go.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onLogout: () -> Unit = {}
) {
    val homeViewModel: HomeViewModel = viewModel()
    val uiState by homeViewModel.uiState.collectAsState()
    val token = UserSession.token

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        if (token != null && uiState.courses.isEmpty()) {
            homeViewModel.loadCourses(token)
        }
    }

    Scaffold(
        bottomBar = { EduGoBottomNavigation(navController = navController) },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- 1. HEADER  ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = Color(0xFFFF6B8E),
                        shape = RoundedCornerShape(bottomEnd = 24.dp, bottomStart = 24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_edugopng),
                    contentDescription = "Logo Edu-Go",
                    modifier = Modifier.size(140.dp)
                )
            }

            // --- 2. PULL TO REFRESH BOX ---
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = {
                    if (token != null) {
                        homeViewModel.loadCourses(token)
                    }
                },
                modifier = Modifier.fillMaxSize(),

            ) {
                // Contenido desplazable (Lista)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // TÍTULO Y BUSCADOR
                    item {
                        Text(
                            text = "Pagina principal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // BUSCADOR
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { homeViewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Buscar curso por nombre...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color(0xFFE0E0E0),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = { homeViewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Borrar", tint = Color.Gray)
                                    }
                                }
                            } else null,
                            shape = RoundedCornerShape(28.dp),
                            singleLine = true
                        )
                    }

                    // FILTROS
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val categories = listOf(
                                "Todos", "Tecnología", "Artes y diseño", "Idiomas",
                                "Ciencias y matemáticas", "Habilidades profesionales"
                            )
                            items(categories) { category ->
                                val isSelected = uiState.selectedCategory == category
                                Surface(
                                    color = if (isSelected) Color.Black else Color.White,
                                    shape = RoundedCornerShape(50),
                                    border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
                                    modifier = Modifier.clickable {
                                        homeViewModel.onCategorySelected(category)
                                    }
                                ) {
                                    Text(
                                        text = category,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // LISTA DE CURSOS
                    if (!uiState.isLoading && uiState.courses.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No hay cursos disponibles", color = Color.Gray)
                            }
                        }
                    } else {
                        itemsIndexed(uiState.courses) { index, curso ->
                            val themeColor = if (index % 2 == 0) Color(0xFFFF0000) else Color(0xFF00C853)

                            CourseCard(
                                curso = curso,
                                themeColor = themeColor,
                                // 👇 AQUÍ CONECTAMOS EL CLICK PARA IR AL DETALLE
                                onClick = {
                                    Log.d("HomeScreen", "Navegando al curso: ${curso.id}")
                                    navController.navigate("course_detail/${curso.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTE: TARJETA DE CURSO ---
@Composable
fun CourseCard(
    curso: SupabaseService.Curso,
    themeColor: Color,
    onClick: () -> Unit //
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column {
            // Banner + Categoría
            Box(modifier = Modifier.height(115.dp).fillMaxWidth()) {
                if (curso.banner_url != null) {
                    AsyncImage(
                        model = curso.banner_url,
                        contentDescription = "Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0)))
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = curso.categoria.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Contenido
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // TÍTULO
                Text(
                    text = curso.nombre_curso,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp
                    ),
                    color = Color(0xFF1A1A1A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // AUTOR
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val profile = curso.profiles
                    val nombreAutor = if (profile != null) {
                        "${profile.nombre ?: ""} ${profile.apellido ?: ""}".trim()
                    } else {
                        "Desconocido"
                    }

                    if (profile?.foto_url != null) {
                        AsyncImage(
                            model = profile.foto_url,
                            contentDescription = "Foto",
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFFEEEEEE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nombreAutor.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.Gray)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = nombreAutor,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PRECIO Y ACCIONES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Precio
                    Text(
                        text = "$${String.format("%.2f", curso.precio)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = themeColor,
                            fontSize = 20.sp
                        )
                    )

                    // Grupo de Botones
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón Ver (Ojo) - También lleva al detalle
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onClick() }, // 👈 Conectado también aquí
                            shape = CircleShape,
                            color = Color(0xFFF5F5F5),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Ver",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Botón Comprar - También lleva al detalle (por ahora)
                        Button(
                            onClick = { onClick() }, // 👈 Conectado también aquí
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "Comprar",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}