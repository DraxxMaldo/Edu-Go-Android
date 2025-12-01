package com.example.edu_go

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.edu_go.ui.navigation.AppNavigation
import com.example.edu_go.ui.theme.EduGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Esto permite que la app se dibuje detrás de las barras del sistema
        enableEdgeToEdge()

        setContent {
            EduGoTheme {

                AppNavigation()
            }
        }
    }
}