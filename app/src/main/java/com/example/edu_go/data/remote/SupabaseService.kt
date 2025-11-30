package com.example.edu_go.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SupabaseService {
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

    suspend fun signUp(
        email: String,
        password: String,
        nombre: String,
        lastName: String,
        plan: String = "Básico"
    ): Result<String> {
        return try {
            Log.d("SupabaseService", "Registrando usuario con metadata: $email")

            val userMetadata = UserMetadata(
                nombre = nombre,
                apellido = lastName,
                plan = plan,
                role = "Estudiante"
            )

            val response: SignUpResponse = client.post("$supabaseUrl/auth/v1/signup") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $supabaseAnonKey")
                headers.append("apikey", supabaseAnonKey)

                setBody(SignUpRequest(
                    email = email,
                    password = password,
                    data = userMetadata
                ))
            }.body()

            val userId = response.user?.id
            if (userId != null) {
                Log.d("SupabaseService", "Usuario registrado exitosamente con ID: $userId")
            } else {
                Log.d("SupabaseService", "Usuario registrado, pero ID es nulo (esperando verificación)")
            }

            Result.success(userId ?: "") // Devolvemos vacío si es nulo

        } catch (e: Exception) {
            Log.e("SupabaseService", "Error al registrar: ${e.message}", e)
            Result.failure(e)
        }
    }

    @Serializable
    data class SignUpRequest(
        val email: String,
        val password: String,
        val data: UserMetadata
    )

    @Serializable
    data class UserMetadata(
        val nombre: String,
        val apellido: String,
        val plan: String,
        val role: String
    )

    @Serializable
    data class SignUpResponse(
        val user: User? = null
    )

    @Serializable
    data class User(
        val id: String
    )
}