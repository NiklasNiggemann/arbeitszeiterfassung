package com.niklasniggemann.arbeitszeiterfassung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.niklasniggemann.arbeitszeiterfassung.ui.theme.ArbeitszeiterfassungTheme
import kotlinx.coroutines.delay

// Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge content
        setContent {
            ArbeitszeiterfassungTheme {
                MainScreen()
            }
        }
    }
}

// Main Screen Composable
@Composable
fun MainScreen() {
    Scaffold { innerPadding ->
        Content(modifier = Modifier.padding(innerPadding))
    }
}

// Main Content Composable
@Composable
fun Content(modifier: Modifier) {
    var isTracking by remember { mutableStateOf(false) }
    var isSignatureEnabled by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf<Long?>(null) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var pausedTime by remember { mutableLongStateOf(0L) } // Keeps track of the total elapsed time before pause
    val employeeName = "John Doe"
    val signaturePoints = remember { mutableStateListOf<Offset>() }

    // Handles time tracking logic
    LaunchedEffect(isTracking) {
        if (isTracking) {
            startTime = System.currentTimeMillis() // Update start time to current time on resume
            while (isTracking) {
                elapsedTime = pausedTime + (System.currentTimeMillis() - (startTime ?: 0L))
                delay(1000)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Employee Info
            EmployeeInfo(employeeName)

            Spacer(modifier = Modifier.height(8.dp))

            // Tracked Time Display
            TrackedTimeDisplay(isTracking, elapsedTime)

            Spacer(modifier = Modifier.height(16.dp))

            // Start/Stop Buttons
            if (isTracking) {
                StopErfassung {
                    isTracking = false
                    isSignatureEnabled = true
                    pausedTime = elapsedTime // Preserve the elapsed time when stopped
                }
            } else {
                StartErfassung {
                    isTracking = true
                    isSignatureEnabled = false
                    startTime = System.currentTimeMillis() // Capture resume start time
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signature Section
            SignatureSection(isSignatureEnabled, signaturePoints)

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            if (isSignatureEnabled) {
                SubmitButton {
                    // Reset signature and timer
                    signaturePoints.clear()
                    elapsedTime = 0L
                    pausedTime = 0L
                    startTime = null
                    isSignatureEnabled = false
                    println("Form submitted!")
                }
            }
        }
    }
}


// Employee Info Composable
@Composable
fun EmployeeInfo(name: String) {
    Text(text = "Mitarbeiter:", style = MaterialTheme.typography.bodyLarge)
    Text(text = name, style = MaterialTheme.typography.bodyLarge)
}

// Tracked Time Display Composable
@Composable
fun TrackedTimeDisplay(isTracking: Boolean, elapsedTime: Long) {
    val timeText = "Tracked Time: ${formatElapsedTime(elapsedTime)}"
    Text(text = timeText, style = MaterialTheme.typography.bodyLarge)
}

// Signature Section Composable
@Composable
fun SignatureSection(isEnabled: Boolean, points: MutableList<Offset>) {
    Text("Signature:", style = MaterialTheme.typography.bodyLarge)
    SignatureField(
        isEnabled = isEnabled,
        points = points,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
    )
}

// Signature Field Composable
@Composable
fun SignatureField(isEnabled: Boolean, points: MutableList<Offset>, modifier: Modifier) {
    Canvas(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.Black) // Adding a black border
            .pointerInput(isEnabled) {
                if (isEnabled) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        points.add(change.position)
                    }
                }
            }
    ) {
        if (isEnabled && points.isNotEmpty()) {
            for (i in 1 until points.size) {
                drawLine(
                    color = Color.Blue,
                    start = points[i - 1],
                    end = points[i],
                    strokeWidth = 4f
                )
            }
        }
    }
}

// Start Button Composable
@Composable
fun StartErfassung(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Start")
    }
}

// Stop Button Composable
@Composable
fun StopErfassung(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Stop")
    }
}

// Submit Button Composable
@Composable
fun SubmitButton(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Submit")
    }
}

// Utility: Format Elapsed Time
fun formatElapsedTime(elapsedMillis: Long): String {
    val seconds = elapsedMillis / 1000 % 60
    val minutes = elapsedMillis / (1000 * 60) % 60
    val hours = elapsedMillis / (1000 * 60 * 60)
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

// Preview Composable
@Preview
@Composable
fun ContentPreview() {
    ArbeitszeiterfassungTheme {
        Content(modifier = Modifier)
    }
}
