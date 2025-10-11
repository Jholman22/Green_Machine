package com.example.greenmachine
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.greenmachine.ui.theme.GreenMachineTheme
import kotlinx.coroutines.delay
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.background

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreenMachineTheme {
                SplashScreenContent()
            }
        }
    }
}

@Composable
fun SplashScreenContent() {
    val context = LocalContext.current

    var visible by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0f) }
    var showText by remember { mutableStateOf(false) }

    // Lógica de carga y transición
    LaunchedEffect(Unit) {
        val duration = 3000 // Duración total de la barra (2 segundos)
        val steps = 100
        val delayPerStep = duration / steps

        repeat(steps) {
            progress += 1f / steps
            delay(delayPerStep.toLong())
        }

        showText = true // Mostrar texto cuando termina la barra

        delay(1000) // Espera con el texto visible
        visible = false // Inicia el fade out

        delay(500) // Da tiempo a animación
        context.startActivity(Intent(context, MainActivity::class.java))
        (context as? ComponentActivity)?.finish()
    }

    // Visibilidad animada de todo el contenido
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logos uno al lado del otro
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logoun1),
                        contentDescription = "Logo 1",
                        modifier = Modifier.size(120.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ff),
                        contentDescription = "Logo 2",
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Línea debajo de los logos
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Barra de progreso animada
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Texto "GREEN MACHINE" solo cuando termina la barra
                AnimatedVisibility(visible = showText) {
                    Text(
                        text = "GREEN MACHINE",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}
