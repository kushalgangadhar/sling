package com.kushalgangadhar.sling.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kushalgangadhar.sling.utils.VisualizerHelper
import kotlin.math.abs
import kotlin.math.hypot

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kushalgangadhar.sling.utils.TimerHelper


@Composable
fun AudioVisualizer(
    visualizerHelper: VisualizerHelper,
    modifier: Modifier = Modifier
) {
    // Collect the continuous stream of FFT bytes
    val fftData by visualizerHelper.fftData.collectAsState()
    val barColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            if (fftData.isEmpty()) return@Canvas

            // We only need the first half of the FFT array for the visible frequency spectrum
            val visualizerSize = fftData.size / 2
            
            // Calculate how wide each bar should be based on screen width
            val barWidth = size.width / visualizerSize
            val gap = 2f // 2 pixels between bars

            for (i in 0 until visualizerSize step 2) {
                // The FFT data is a mix of real and imaginary parts. 
                // We use hypotenuse to get the actual magnitude (height) of the frequency.
                val rfk = fftData[i]
                val ifk = fftData[i + 1]
                val magnitude = hypot(rfk.toFloat(), ifk.toFloat())

                // Scale the magnitude so it looks good on screen
                val scaledHeight = (magnitude * 2).coerceAtMost(size.height)

                val xOffset = i * barWidth
                val yOffset = size.height - scaledHeight

                // Draw the rounded bar
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x = xOffset, y = yOffset),
                    size = Size(width = barWidth * 2 - gap, height = scaledHeight),
                    cornerRadius = CornerRadius(x = 4f, y = 4f)
                )
            }
        }
    }
}


@Composable
fun SleepTimerDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    // The options we want to show the user
    val timerOptions = listOf(5, 15, 30, 45, 60)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Sleep Timer")
        },
        text = {
            Column {
                Text(text = "Stop audio playback after:")
                Spacer(modifier = Modifier.height(16.dp))

                // Create a button for each time option
                timerOptions.forEach { minutes ->
                    TextButton(
                        onClick = {
                            TimerHelper.startSleepTimer(context, minutes.toLong())
                            onDismissRequest() // Close the dialog
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$minutes Minutes")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Add an option to explicitly cancel any active timer
                TextButton(
                    onClick = {
                        TimerHelper.cancelSleepTimer(context)
                        onDismissRequest() // Close the dialog
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Turn Off Timer", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}