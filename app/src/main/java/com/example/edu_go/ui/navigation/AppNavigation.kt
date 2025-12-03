package com.example.edu_go.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType // 👈 Importante para recibir el ID
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument // 👈 Importante para definir el argumento
import com.example.edu_go.ui.components.*

@Composable
fun AppNavigation(
    // Parámetro para la persistencia de sesión
    startDestination: String = "login"
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- PANTALLA DE LOGIN ---
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLA DE REGISTRO ---
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLA DE INICIO (HOME) ---
        composable("home") {
            HomeScreen(
                navController = navController,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLA DE DETALLE DEL CURSO (NUEVA RUTA) ---
        composable(
            route = "course_detail/{courseId}", // Definimos que la ruta espera un ID
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType } // Definimos que el ID es texto
            )
        ) { backStackEntry ->
            // Recuperamos el ID de los argumentos
            val courseId = backStackEntry.arguments?.getString("courseId")

            // Si el ID existe, mostramos la pantalla
            if (courseId != null) {
                CourseDetailScreen(
                    navController = navController,
                    courseId = courseId
                )
            }
        }

        // --- PANTALLA DE CURSOS ---
        composable("courses") {
            CoursesScreen(
                navController = navController
            )
        }

        // --- PANTALLA DE PERFIL ---
        composable("profile") {
            ProfileScreen(
                navController = navController
            )
        }

        // --- PANTALLA AGREGAR TARJETA ---
        composable("add_card") {
            AddCardScreen(
                navController = navController
            )
        }


        // RUTA CHECKOUT
        composable(
            route = "checkout/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (courseId != null) {
                CheckoutScreen(navController = navController, courseId = courseId)
            }
        }


        // RUTA: REPRODUCTOR DEL CURSO
        composable(
            route = "course_content/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (courseId != null) {
                // Aquí llamaremos a la UI que crearemos en el siguiente paso
                CourseContentScreen(navController = navController, courseId = courseId)
            }
        }


        // PANTALLA DE FAVORITOS
        composable("favorites") {
            FavoritesScreen(navController = navController)
        }
    }
}