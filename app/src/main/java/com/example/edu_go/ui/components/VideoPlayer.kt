package com.example.edu_go.ui.components



import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Creamos el Player
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Manejo del ciclo de vida (Pausar si salimos de la app, liberar al cerrar)
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer?.pause()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                // Opcional: reanudar automáticamente o dejar pausado
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.release() // ¡Importante! Liberar memoria
        }
    }

    // Cargar video cuando cambia la URL
    LaunchedEffect(videoUrl) {
        if (videoUrl.isNotEmpty()) {
            exoPlayer?.release() // Liberar el anterior si había

            val player = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
                this.playWhenReady = playWhenReady
                prepare()
            }
            exoPlayer = player
        }
    }

    // UI del Player
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f) // Formato panorámico estándar
            .background(Color.Black)
    ) {
        if (exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true // Mostrar controles (play, pausa, barra)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Loading mientras carga
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}