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
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.delete
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.collections.map

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
    //                         MÉTODOS DE AUTENTICACIÓN
    // ========================================================================

    // 1. REGISTRAR USUARIO
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

    // 2. CREAR PERFIL
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

    // 3. INICIAR SESIÓN
    suspend fun signIn(email: String, password: String): Result<LoginResult> {
        return try {
            Log.d("SupabaseService", "--- INICIO LOGIN ---")

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
                val token = signInResponse.access_token

                if (userId != null && token != null) {
                    Log.d("SupabaseService", "Login exitoso para ID: $userId")
                    Result.success(LoginResult(userId = userId, token = token))
                } else {
                    Result.failure(Exception("ID o token nulos en respuesta exitosa"))
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
    //                     DATA CLASSES (AUTH)
    // ========================================================================

    @Serializable
    data class User(val id: String)

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
    data class SignUpResponse(val user: User? = null)

    @Serializable
    data class ProfileRequest(
        val id: String,
        val email: String,
        val nombre: String,
        val apellido: String,
        val plan: String,
        val role: String
    )

    @Serializable
    data class SignInResponse(
        val user: User? = null,
        val access_token: String? = null
    )

    @Serializable
    data class LoginResult(
        val userId: String,
        val token: String
    )

    // ========================================================================
    //                     PERFIL DE USUARIO
    // ========================================================================

    suspend fun getUserProfile(userId: String, token: String): Result<UserProfile> {
        return try {
            val response: List<UserProfile> = client.get("$supabaseUrl/rest/v1/profiles") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url { parameters.append("id", "eq.$userId") }
            }.body()

            if (response.isNotEmpty()) {
                Result.success(response[0])
            } else {
                Result.failure(Exception("Perfil no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Serializable
    data class UserProfile(
        val id: String,
        val email: String,
        val nombre: String,
        val apellido: String,
        val plan: String,
        val role: String,
        val foto_url: String? = null,
        val descripcion: String? = null
    )

    // 4. ACTUALIZAR PERFIL
    suspend fun updateUserProfile(userId: String, token: String, nombre: String, apellido: String): Result<Unit> {
        return try {
            val response: HttpResponse = client.patch("$supabaseUrl/rest/v1/profiles?id=eq.$userId") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                headers.append("Prefer", "return=minimal")
                setBody(mapOf("nombre" to nombre, "apellido" to apellido))
            }
            if (response.status.isSuccess()) Result.success(Unit) else Result.failure(Exception("Error actualizar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. SUBIR IMAGEN
    suspend fun uploadAvatar(token: String, imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val response: HttpResponse = client.post("$supabaseUrl/storage/v1/object/avatars/$fileName") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                headers.append("Content-Type", "image/jpeg")
                headers.append("x-upsert", "true")
                setBody(imageBytes)
            }
            if (response.status.isSuccess()) {
                val publicUrl = "$supabaseUrl/storage/v1/object/public/avatars/$fileName"
                Result.success(publicUrl)
            } else {
                Result.failure(Exception("Error al subir imagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. ACTUALIZAR FOTO
    suspend fun updateUserPhoto(userId: String, token: String, photoUrl: String): Result<Unit> {
        return try {
            client.patch("$supabaseUrl/rest/v1/profiles?id=eq.$userId") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                setBody(mapOf("foto_url" to photoUrl))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    //                     TARJETAS SIMULADAS
    // ========================================================================

    // 7. OBTENER TARJETAS
    suspend fun getUserCards(userId: String, token: String): Result<List<TarjetaSimulada>> {
        return try {
            val response: HttpResponse = client.get("$supabaseUrl/rest/v1/tarjetas_simuladas") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url {
                    parameters.append("usuario_id", "eq.$userId")
                    parameters.append("select", "*")
                }
            }
            if (response.status.isSuccess()) {
                val tarjetas: List<TarjetaSimulada> = response.body()
                Result.success(tarjetas)
            } else {
                Result.failure(Exception("Error servidor: ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 8. AGREGAR TARJETA
    suspend fun addCard(token: String, tarjeta: TarjetaRequest): Result<Unit> {
        return try {
            val response: HttpResponse = client.post("$supabaseUrl/rest/v1/tarjetas_simuladas") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                setBody(tarjeta)
            }
            if (response.status.isSuccess()) Result.success(Unit) else Result.failure(Exception("Error guardar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Serializable
    data class TarjetaSimulada(
        val id: String,
        val usuario_id: String,
        val numero_tarjeta: String,
        val titular: String,
        val fecha_vencimiento: String,
        val tipo: String,
        val saldo_simulado: Double = 0.0
    )

    @Serializable
    data class TarjetaRequest(
        val usuario_id: String,
        val numero_tarjeta: String,
        val titular: String,
        val fecha_vencimiento: String,
        val cvv: String,
        val tipo: String,
        val saldo_simulado: Double
    )

    // 9. ELIMINAR TARJETA
    suspend fun deleteCard(token: String, cardId: String): Result<Unit> {
        return try {
            val response: HttpResponse = client.delete("$supabaseUrl/rest/v1/tarjetas_simuladas") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url { parameters.append("id", "eq.$cardId") }
            }
            if (response.status.isSuccess()) Result.success(Unit) else Result.failure(Exception("Error eliminar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 14. ACTUALIZAR SALDO
    suspend fun updateCardBalance(token: String, cardId: String, newBalance: Double): Result<Unit> {
        return try {
            val response: HttpResponse = client.patch("$supabaseUrl/rest/v1/tarjetas_simuladas?id=eq.$cardId") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                setBody(mapOf("saldo_simulado" to newBalance))
            }
            if (response.status.isSuccess()) Result.success(Unit) else Result.failure(Exception("Error saldo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    //              👇 SECCIÓN DE CURSOS (ACTUALIZADA PARA PLAYER)
    // ========================================================================

    // 🟢 NUEVO MODELO: RECURSO
    @Serializable
    data class Recurso(
        val id: String,
        val nombre_archivo: String? = null,
        val tipo: String? = null,
        val url: String? = null,
        val archivo_url: String? = null
    )

    // 🟢 TAREA ACTUALIZADA (Con instrucciones y recursos)
    @Serializable
    data class Tarea(
        val id: String,
        val titulo: String,
        val instrucciones: String? = null, // 👇 Nuevo campo
        val recursos: List<Recurso> = emptyList() // 👇 Lista anidada
    )

    @Serializable
    data class Seccion(
        val id: String,
        val nombre_seccion: String,
        val tareas: List<Tarea> = emptyList()
    )

    @Serializable
    data class ProfileSubset(
        val nombre: String? = "Desconocido",
        val apellido: String? = "",
        val foto_url: String? = null,
        val descripcion: String? = null
    )

    @Serializable
    data class Curso(
        val id: String,
        val nombre_curso: String,
        val descripcion: String?,
        val categoria: String,
        val precio: Double,
        val usuario_id: String,
        val banner_url: String? = null,
        val profiles: ProfileSubset? = null,
        val secciones: List<Seccion> = emptyList()
    )

    // 10. OBTENER TODOS LOS CURSOS (Home)
    suspend fun getAllCourses(token: String): Result<List<Curso>> {
        return try {
            val response: HttpResponse = client.get("$supabaseUrl/rest/v1/cursos") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url {
                    parameters.append("select", "*, profiles(nombre, apellido, foto_url)")
                    parameters.append("order", "created_at.desc")
                }
            }
            if (response.status.isSuccess()) {
                val cursos: List<Curso> = response.body()
                Result.success(cursos)
            } else {
                Result.failure(Exception("Error al obtener cursos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 11. OBTENER UN CURSO POR ID (GET SINGLE) - ⭐ ACTUALIZADO PARA PLAYER ⭐
    suspend fun getCourseById(token: String, courseId: String): Result<Curso> {
        return try {
            Log.d("SupabaseService", "Buscando curso completo ID: $courseId")

            val response: HttpResponse = client.get("$supabaseUrl/rest/v1/cursos") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)

                url {
                    parameters.append("id", "eq.$courseId")

                    // 👇 CONSULTA MAESTRA ACTUALIZADA:
                    // 1. profiles: info del profe
                    // 2. secciones: estructura
                    // 3. tareas: contenido + instrucciones
                    // 4. recursos: archivos adjuntos (anidados en tareas)
                    parameters.append(
                        "select",
                        "*, profiles(nombre, apellido, foto_url, descripcion), secciones(id, nombre_seccion, tareas(id, titulo, instrucciones, recursos(*)))"
                    )
                    // Opcional: ordenar secciones
                    // parameters.append("secciones.order", "created_at.asc")
                }
            }

            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                val cursos: List<Curso> = Json { ignoreUnknownKeys = true }.decodeFromString(responseBody)
                if (cursos.isNotEmpty()) {
                    Result.success(cursos[0])
                } else {
                    Result.failure(Exception("Curso no encontrado"))
                }
            } else {
                Log.e("SupabaseService", "Error HTTP: ${response.status}")
                Result.failure(Exception("Error del servidor"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Excepción buscando curso", e)
            Result.failure(e)
        }
    }

    // ========================================================================
    //                     COMPRAS E INSCRIPCIONES
    // ========================================================================

    @Serializable
    data class InscripcionRequest(
        val usuario_id: String,
        val curso_id: String,
        val precio_pagado: Double
    )

    // 12. ENROLL MANUAL (CORREGIDO PARA MOSTRAR EL ERROR REAL)
    // Este método es el que estás usando ahora. Si falla, el log te dirá exactamente por qué.
    suspend fun enrollCourse(token: String, userId: String, courseId: String, precio: Double): Result<Unit> {
        return try {
            val inscripcion = InscripcionRequest(userId, courseId, precio)
            val response: HttpResponse = client.post("$supabaseUrl/rest/v1/inscripciones") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                setBody(inscripcion)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                // 👇 CAMBIO CLAVE: Leer el body para ver el error de Supabase (ej: Políticas RLS)
                val errorMsg = response.bodyAsText()
                Log.e("SupabaseService", "Error enroll detallado: $errorMsg")
                Result.failure(Exception("Error enroll: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 13. VERIFICAR INSCRIPCIÓN
    suspend fun isUserEnrolled(token: String, userId: String, courseId: String): Result<Boolean> {
        return try {
            val response: HttpResponse = client.get("$supabaseUrl/rest/v1/inscripciones") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url {
                    parameters.append("usuario_id", "eq.$userId")
                    parameters.append("curso_id", "eq.$courseId")
                    parameters.append("select", "*")
                }
            }
            if (response.status.isSuccess()) {
                val bodyText = response.bodyAsText()
                val lista = Json { ignoreUnknownKeys = true }.decodeFromString<List<InscripcionRequest>>(bodyText)
                Result.success(lista.isNotEmpty())
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error verificando", e)
            Result.failure(e)
        }
    }

    // MODELO PARA RPC
    @Serializable
    data class PurchaseRequest(
        val p_curso_id: String,
        val p_tarjeta_estudiante_id: String,
        val p_precio: Double
    )

    // 15. COMPRA SEGURA (DESACTIVADO PARA EVITAR ERRORES)
    // Ya no debes usar este método. Usa enrollCourse (Método 12).
    suspend fun purchaseCourseRpc(token: String, courseId: String, cardId: String, price: Double): Result<Unit> {
        return Result.failure(Exception("Método obsoleto. Por favor usa enrollCourse."))
    }


    @Serializable
    data class InscripcionResponse(
        val cursos: Curso? // Puede ser null si se borró el curso, por seguridad
    )

    // 16. OBTENER MIS CURSOS (Biblioteca del Estudiante)
    suspend fun getEnrolledCourses(token: String, userId: String): Result<List<Curso>> {
        return try {
            val response: HttpResponse = client.get("$supabaseUrl/rest/v1/inscripciones") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url {
                    // 1. Filtramos por el usuario actual
                    parameters.append("usuario_id", "eq.$userId")

                    // 2. MAGIA DE SUPABASE (Join):
                    // Pedimos que nos traiga el objeto 'cursos' relacionado con cada inscripción.
                    // Y dentro del curso, pedimos el perfil del profesor.
                    parameters.append("select", "cursos(*,profiles(nombre,apellido,foto_url))")

                    // Ordenamos por fecha de inscripción (más recientes primero)
                    parameters.append("order", "created_at.desc")
                }
            }

            if (response.status.isSuccess()) {
                val bodyText = response.bodyAsText()

                // 3. Deserializamos la lista de inscripciones
                val listaInscripciones = Json { ignoreUnknownKeys = true }.decodeFromString<List<InscripcionResponse>>(bodyText)

                // 4. Transformamos (Map) para quedarnos solo con la lista de Cursos limpios
                // Filtramos los nulos por si acaso un curso fue borrado pero la inscripción quedó
                val misCursos = listaInscripciones.mapNotNull { it.cursos }

                Result.success(misCursos)
            } else {
                Log.e("SupabaseService", "Error mis cursos: ${response.bodyAsText()}")
                Result.failure(Exception("Error obteniendo mis cursos"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Excepción mis cursos", e)
            Result.failure(e)
        }
    }



// ... dentro de SupabaseService ...

    // 👇 ESTA ES LA CLASE QUE TE FALTABA PARA QUE NO DE ERROR
    // Sirve para leer la respuesta { "cursos": { ... } } que manda Supabase
    @Serializable
    data class FavoritoResponse(
        val cursos: Curso? // Puede ser null si el curso se borró
    )

    // 17. VERIFICAR FAVORITO (¿Ya le di like?)
    suspend fun checkIsFavorite(token: String, userId: String, courseId: String): Result<Boolean> {
        return try {
            val response: HttpResponse = client.get("$supabaseUrl/rest/v1/favoritos") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url {
                    parameters.append("usuario_id", "eq.$userId")
                    parameters.append("curso_id", "eq.$courseId")
                    parameters.append("select", "id") // Solo necesitamos saber si existe
                }
            }
            if (response.status.isSuccess()) {
                val body = response.bodyAsText()
                // Si la respuesta no es una lista vacía "[]", es true
                Result.success(body.length > 2)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 18. AGREGAR A FAVORITOS
    suspend fun addToFavorites(token: String, userId: String, courseId: String): Result<Unit> {
        return try {
            val response: HttpResponse = client.post("$supabaseUrl/rest/v1/favoritos") {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                setBody(mapOf("usuario_id" to userId, "curso_id" to courseId))
            }
            if (response.status.isSuccess()) Result.success(Unit) else Result.failure(Exception("Error al agregar fav"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 19. ELIMINAR DE FAVORITOS
    suspend fun removeFromFavorites(token: String, userId: String, courseId: String): Result<Unit> {
        return try {
            val response: HttpResponse = client.delete("$supabaseUrl/rest/v1/favoritos") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url {
                    parameters.append("usuario_id", "eq.$userId")
                    parameters.append("curso_id", "eq.$courseId")
                }
            }
            if (response.status.isSuccess()) Result.success(Unit) else Result.failure(Exception("Error al borrar fav"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 20. OBTENER LISTA DE FAVORITOS (Para la pantalla de perfil/favoritos)
    suspend fun getMyFavorites(token: String, userId: String): Result<List<Curso>> {
        return try {
            // Hacemos el JOIN para traer el curso completo
            val response: HttpResponse = client.get("$supabaseUrl/rest/v1/favoritos") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                headers.append("apikey", supabaseAnonKey)
                url {
                    parameters.append("usuario_id", "eq.$userId")
                    // Traemos datos del curso y perfil del autor
                    parameters.append("select", "cursos(*,profiles(nombre,apellido,foto_url))")
                }
            }
            if (response.status.isSuccess()) {
                val body = response.bodyAsText()

                // 👇 AQUI USAMOS LA CLASE QUE AGREGAMOS ARRIBA
                val lista = Json { ignoreUnknownKeys = true }.decodeFromString<List<FavoritoResponse>>(body)

                // Extraemos solo los cursos válidos (no nulos)
                val cursos = lista.mapNotNull { it.cursos }

                Result.success(cursos)
            } else {
                Result.failure(Exception("Error obteniendo favoritos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}