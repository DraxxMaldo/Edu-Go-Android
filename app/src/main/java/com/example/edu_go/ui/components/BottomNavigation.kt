package com.example.edu_go.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
// Eliminamos height fijo para que se adapte al sistema
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun EduGoBottomNavigation(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Definición de colores para que sea fácil de ajustar
    val rosadoFondo = Color(0xFFFF6B8E)
    val rosadoSeleccion = Color(0xFFD65A76)
    val textoBlanco = Color.White
    val textoInactivo = Color.White.copy(alpha = 0.6f)

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth(),
        containerColor = rosadoFondo,
        contentColor = textoBlanco,
        tonalElevation = 0.dp
    ) {
        // --- Ítem: Inicio ---
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = textoBlanco,
                selectedTextColor = textoBlanco,
                indicatorColor = rosadoSeleccion,
                unselectedIconColor = textoInactivo,
                unselectedTextColor = textoInactivo
            )
        )

        // --- Ítem: Cursos ---
        NavigationBarItem(
            icon = { Icon(Icons.Default.Book, contentDescription = "Cursos") },
            label = { Text("Cursos", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            selected = currentRoute == "courses",
            onClick = {
                navController.navigate("courses") {
                    popUpTo("home") { inclusive = true }
                }
            },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = textoBlanco,
                selectedTextColor = textoBlanco,
                indicatorColor = rosadoSeleccion,
                unselectedIconColor = textoInactivo,
                unselectedTextColor = textoInactivo
            )
        )

        // --- Ítem: Perfil ---
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo("home") { inclusive = true }
                }
            },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = textoBlanco,
                selectedTextColor = textoBlanco,
                indicatorColor = rosadoSeleccion,
                unselectedIconColor = textoInactivo,
                unselectedTextColor = textoInactivo
            )
        )
    }
}