package com.example.edu_go.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse // Importante para el fix
import io.ktor.client.statement.bodyAsText // Importante para el fix
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess // Importante para el fix
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

    // ========================================================================
    //                         MÉTODOS (FUNCIONES)
    // ========================================================================

    // 1. REGISTRAR USUARIO (Auth)
    suspend fun signUp(
        email: String,
        password: String,
        nombre: String,
        lastName: String
    ): Result<String> {
        return try {
            Log.d("SupabaseService", "Registrando usuario con meta $email")

            val userMetadata = UserMetadata(
                nombre = nombre,
                apellido = lastName,
                plan = "",
                role = "estudiante"
            )

            // Usamos bodyAsText aquí también por seguridad, aunque tu lógica original funcionaba
            val response: HttpResponse = client.post("$supabaseUrl/auth/v1/signup") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $supabaseAnonKey")
                headers.append("apikey", supabaseAnonKey)

                setBody(SignUpRequest(
                    email = email,
                    password = password,
                    data = userMetadata
                ))
            }

            if (response.status.isSuccess()) {
                val json = Json { ignoreUnknownKeys = true }
                val signUpResponse = json.decodeFromString<SignUpResponse>(response.bodyAsText())
                val userId = signUpResponse.user?.id

                Log.d("SupabaseService", "Usuario registrado exitosamente con ID: $userId")
                Result.success(userId ?: "")
            } else {
                Log.e("SupabaseService", "Error en registro: ${response.bodyAsText()}")
                Result.failure(Exception("Error en registro"))
            }

        } catch (e: Exception) {
            Log.e("SupabaseService", "Error al registrar: ${e.message}", e)
            Result.failure(e)
        }
    }

    // 2. CREAR PERFIL (Base de datos)
    suspend fun createProfile(userId: String, name: String, lastName: String, email: String): Result<Unit> {
        return try {
            Log.d("SupabaseService", "Creando perfil para usuario: $userId")

            client.post("$supabaseUrl/rest/v1/profiles") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $supabaseAnonKey")
                headers.append("apikey", supabaseAnonKey)
                headers.append("Prefer", "return=minimal")

                setBody(ProfileRequest(
                    id = userId,
                    email = email,
                    nombre = name,
                    apellido = lastName,
                    plan = "",
                    role = "Estudiante"
                ))
            }

            Log.d("SupabaseService", "Perfil creado exitosamente para ID: $userId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SupabaseService", "Error al crear perfil: ${e.message}", e)
            Result.failure(e)
        }
    }

    // 3. INICIAR SESIÓN (CORREGIDO)
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            Log.d("SupabaseService", "--- INICIO LOGIN ---")

            // Petición HTTP cruda
            val response: HttpResponse = client.post("$supabaseUrl/auth/v1/token?grant_type=password") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $supabaseAnonKey")
                headers.append("apikey", supabaseAnonKey)
                setBody(mapOf(
                    "email" to email,
                    "password" to password
                ))
            }

            val responseBody = response.bodyAsText()
            Log.d("SupabaseService", "Respuesta Server: $responseBody")

            if (response.status.isSuccess()) {
                val json = Json { ignoreUnknownKeys = true }
                val signInResponse = json.decodeFromString<SignInResponse>(responseBody)
                val userId = signInResponse.user?.id

                if (userId != null) {
                    Result.success(userId)
                } else {
                    Result.failure(Exception("ID nulo en respuesta exitosa"))
                }
            } else {
                Log.e("SupabaseService", "Fallo Login: $responseBody")
                Result.failure(Exception("Error servidor: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Excepción Login: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========================================================================
    //                     DATA CLASSES (MODELOS DE DATOS)
    // ========================================================================

    // --------------------------------------------------------
    // A. COMUNES (Usados tanto en registro como login)
    // --------------------------------------------------------
    @Serializable
    data class User(
        val id: String
    )

    // --------------------------------------------------------
    // B. MODELOS PARA REGISTRO (Sign Up & Create Profile)
    // --------------------------------------------------------
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
    data class ProfileRequest(
        val id: String,
        val email: String,
        val nombre: String,
        val apellido: String,
        val plan: String,
        val role: String
    )

    // --------------------------------------------------------
    // C. MODELOS PARA INICIO DE SESIÓN (Log In)
    // --------------------------------------------------------
    @Serializable
    data class SignInResponse(
        val user: User? = null,
        val access_token: String? = null
    )

}