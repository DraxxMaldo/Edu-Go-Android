package com.example.edu_go.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
@Composable
fun EditProfileDialog(
    currentName: String,
    currentLastName: String,
    currentPhotoUrl: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var newLastName by remember { mutableStateOf(currentLastName) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzador para abrir la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            onImageSelected(uri) // Notificamos que se eligió foto
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Editar Perfil") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // --- SECCIÓN FOTO ---
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { galleryLauncher.launch("image/*") }, // Al hacer clic abre galería
                    contentAlignment = Alignment.Center
                ) {
                    // Si seleccionó una nueva, mostramos esa. Si no, la URL actual.
                    val model = selectedImageUri ?: currentPhotoUrl

                    if (model != null) {
                        AsyncImage(
                            model = model,
                            contentDescription = "Foto perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder gris si no hay nada
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray, CircleShape)
                        )
                    }

                    // Icono de camara encima
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- CAMPOS DE TEXTO ---
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = newLastName,
                    onValueChange = { newLastName = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(newName, newLastName) }) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}