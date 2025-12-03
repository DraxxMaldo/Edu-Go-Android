package com.example.edu_go

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.edu_go.data.UserSession
import com.example.edu_go.data.UserStore
import com.example.edu_go.ui.navigation.AppNavigation
import com.example.edu_go.ui.theme.EduGoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite dibujar detrás de las barras del sistema
        enableEdgeToEdge()

        setContent {
            EduGoTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                // 1. Inicializamos la conexión con el almacenamiento (Disco)
                val userStore = remember { UserStore(context) }

                // 2. Estados para controlar qué mostrar
                var isLoadingSession by remember { mutableStateOf(true) } // ¿Estamos revisando?
                var startDestination by remember { mutableStateOf("login") } // ¿A dónde vamos?

                // 3. Lógica que corre UNA SOLA VEZ al iniciar la app
                LaunchedEffect(Unit) {
                    scope.launch {
                        // Leemos el Token y el ID guardados en el teléfono
                        // .first() obtiene el valor actual y se desconecta
                        val token = userStore.getAccessToken.first()
                        val userId = userStore.getUserId.first()

                        if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                            // ✅ SESIÓN ENCONTRADA:
                            // Restauramos la memoria RAM (UserSession) para que la App la use
                            UserSession.token = token
                            UserSession.userId = userId

                            // Decidimos ir directo al Home
                            startDestination = "home"
                        } else {
                            // ❌ NO HAY SESIÓN:
                            // Vamos al Login
                            startDestination = "login"
                        }

                        // Ya terminamos de revisar, quitamos la pantalla de carga
                        isLoadingSession = false
                    }
                }

                // 4. Decisión de qué pantalla pintar
                if (isLoadingSession) {
                    // Muestra una ruedita cargando mientras revisa el disco (pasa muy rápido)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Carga la navegación con el destino correcto ("home" o "login")
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}