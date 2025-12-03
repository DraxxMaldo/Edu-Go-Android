package com.example.edu_go.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.edu_go.ui.viewmodel.CheckoutViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    courseId: String
) {
    val viewModel: CheckoutViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()


    val context = LocalContext.current

    LaunchedEffect(courseId) {
        viewModel.loadCheckoutData(courseId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Resumen de Compra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            // BARRA INFERIOR CON ACCIONES
            if (uiState.course != null && uiState.selectedCard != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .navigationBarsPadding() // Mantiene el padding correcto
                        .padding(16.dp)
                ) {
                    // Botón Confirmar
                    Button(
                        onClick = {
                            viewModel.processPurchase {
                                // 👇 AQUÍ LANZAMOS LA NOTIFICACIÓN AL TERMINAR LA COMPRA
                                val precio = uiState.course?.precio ?: 0.0
                                val nombreCurso = uiState.course?.nombre_curso ?: "Curso"

                                showPurchaseNotification(context, nombreCurso, precio)

                                // Navegar al home
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isProcessing
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Procesando...", fontSize = 16.sp)
                        } else {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar y Pagar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Cancelar
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isProcessing
                    ) {
                        Text("Cancelar compra", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.course != null) {

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- SECCIÓN DE PRODUCTO ---
                    Text(
                        "Estás comprando:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Banner
                            if (uiState.course?.banner_url != null) {
                                AsyncImage(
                                    model = uiState.course!!.banner_url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Sin imagen", color = Color.White)
                                }
                            }

                            // Detalles
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = uiState.course?.categoria?.uppercase() ?: "GENERAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF6B8E),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.course?.nombre_curso ?: "Curso sin nombre",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total a pagar:", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                                    val precioStr = String.format("%.2f", uiState.course!!.precio)
                                    Text(
                                        text = "$$precioStr",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color(0xFF00C853)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- SECCIÓN MÉTODO DE PAGO ---
                    Text(
                        "Pagar con:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.userCards.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No tienes tarjetas registradas. Por favor agrega una en tu perfil.",
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        uiState.userCards.forEach { tarjeta ->
                            val isSelected = uiState.selectedCard?.id == tarjeta.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable { viewModel.selectCard(tarjeta) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                                ),
                                border = if (isSelected) BorderStroke(2.dp, Color(0xFF00C853)) else BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (isSelected) Color(0xFFC8E6C9) else Color(0xFFF5F5F5),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = if(isSelected) Color(0xFF2E7D32) else Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tarjeta.tipo.uppercase() + " •••• " + tarjeta.numero_tarjeta.takeLast(4),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF333333)
                                        )
                                        Text(
                                            text = "Saldo disp: $${tarjeta.saldo_simulado}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if(tarjeta.saldo_simulado < (uiState.course?.precio ?: 0.0)) Color.Red else Color.Gray
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Seleccionado",
                                            tint = Color(0xFF00C853),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ ${uiState.error}",
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}


fun showPurchaseNotification(context: Context, courseName: String, price: Double) {
    val channelId = "compras_edugo_channel"
    val notificationId = 101


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Compras Realizadas"
        val descriptionText = "Notificaciones de compras de cursos"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // 2. Construir la notificación
    val precioStr = String.format("%.2f", price)
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.stat_sys_download_done) // Icono del sistema (Check)
        .setContentTitle("¡Compra Exitosa!")
        .setContentText("Se ha descontado $$precioStr de tu saldo por el curso: $courseName")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true) // Se borra al tocarla

    // 3. Mostrar la notificación
    try {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    } catch (e: SecurityException) {

        e.printStackTrace()
    }
}