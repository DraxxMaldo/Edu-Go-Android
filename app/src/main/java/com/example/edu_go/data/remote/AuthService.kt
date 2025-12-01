package com.example.edu_go.data.remote


import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AuthService {
    private val supabaseUrl = "https://zipzbrlyxfgnculnknqq.supabase.co"
    private val supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InppcHpicmx5eGZnbmN1bG5rbnFxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIyMDQzODUsImV4cCI6MjA3Nzc4MDM4NX0.HVnaf_1ZYhD6h02J4THQlOMdrT8XH3BDdXKg9TK5g2M"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    suspend fun getCurrentUser(sessionToken: String): Result<CurrentUser> {
        return try {
            Log.d("AuthService", "Obteniendo usuario actual")

            val response = client.get("$supabaseUrl/auth/v1/user") {
                headers.append(HttpHeaders.Authorization, "Bearer $sessionToken")
            }

            if (response.status.isSuccess()) {
                val userResponse: CurrentUserResponse = response.body()
                Result.success(CurrentUser(
                    id = userResponse.id,
                    email = userResponse.email
                ))
            } else {
                Result.failure(Exception("Error al obtener usuario"))
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Error al obtener usuario actual: ${e.message}", e)
            Result.failure(e)
        }
    }

    @Serializable
    data class CurrentUser(
        val id: String,
        val email: String
    )

    @Serializable
    data class CurrentUserResponse(
        val id: String,
        val email: String
    )
}