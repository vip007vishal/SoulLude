package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GradientBottom
import com.example.ui.theme.getThemeSingleColor
import com.example.ui.theme.getThemeGradientColors
import androidx.compose.ui.graphics.Brush

@Composable
fun VerticalEqualizerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .pointerInput(valueRange) {
                detectTapGestures { changeOffset ->
                    val height = size.height.toFloat()
                    if (height > 0) {
                        val fraction = ((height - changeOffset.y) / height).coerceIn(0f, 1f)
                        val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    }
                }
            }
            .pointerInput(valueRange) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val height = size.height.toFloat()
                    if (height > 0) {
                        val currentFraction = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                        val deltaFraction = -dragAmount / height
                        val newFraction = (currentFraction + deltaFraction).coerceIn(0f, 1f)
                        val newValue = valueRange.start + newFraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val height = maxHeight
        val primaryColor = MaterialTheme.colorScheme.primary
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val widthPx = size.width
            val heightPx = size.height
            val centerX = widthPx / 2
            
            // Draw background track
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = androidx.compose.ui.geometry.Offset(centerX, 0f),
                end = androidx.compose.ui.geometry.Offset(centerX, heightPx),
                strokeWidth = 6f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            val fraction = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            val thumbY = heightPx - (fraction * heightPx)
            
            // Draw active track
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(centerX, thumbY),
                end = androidx.compose.ui.geometry.Offset(centerX, heightPx),
                strokeWidth = 6f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            // Draw thumb with glow/aura
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = 32f,
                center = androidx.compose.ui.geometry.Offset(centerX, thumbY)
            )
            drawCircle(
                color = Color.White,
                radius = 16f,
                center = androidx.compose.ui.geometry.Offset(centerX, thumbY)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(viewModel: com.example.viewmodel.MusicViewModel, onBack: () -> Unit) {
    val isEnabled by viewModel.eqEnabled.collectAsState()
    val eqBands by viewModel.eqBands.collectAsState()
    val bassBoost by viewModel.bassBoost.collectAsState()
    val surround by viewModel.surround.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()

    var selectedProfile by remember { mutableStateOf(0) }
    val profiles = listOf(
        Pair("Speaker", Icons.Default.Speaker),
        Pair("Headphones", Icons.Default.Headphones),
        Pair("Bluetooth", Icons.Default.Bluetooth),
        Pair("Car", Icons.Default.CarRepair),
        Pair("DAC", Icons.Default.Devices)
    )

    val themeMode by viewModel.themeMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = getThemeGradientColors(themeMode)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Equalizer & Effects", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Switch(
                checked = isEnabled,
                onCheckedChange = { viewModel.setEqEnabled(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedProfile,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp,
            divider = {}
        ) {
            profiles.forEachIndexed { index, profile ->
                Tab(
                    selected = selectedProfile == index,
                    onClick = { selectedProfile = index },
                    icon = { Icon(profile.second, contentDescription = null, tint = if (selectedProfile == index) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)) },
                    text = { Text(profile.first, color = if (selectedProfile == index) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f), fontSize = 10.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEnabled) {
            Text("Presets", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Flat", "Bass Boost", "Acoustic", "Vocal").forEach { preset ->
                    OutlinedButton(
                        onClick = { viewModel.applyPreset(preset) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (selectedPreset == preset) MaterialTheme.colorScheme.primary else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedPreset == preset) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(preset, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dummy sliders
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val bands = listOf("60", "230", "910", "3.6k", "14k")
                bands.forEachIndexed { index, freq ->
                    val bandValue = eqBands.getOrNull(index) ?: 0f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().weight(1f)
                    ) {
                        Text("+15", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        VerticalEqualizerSlider(
                            value = bandValue,
                            onValueChange = { viewModel.setEqBand(index, it) },
                            valueRange = -15f..15f,
                            modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("-15", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(freq, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bass and Treble knobs
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Slider(
                        value = bassBoost,
                        onValueChange = { viewModel.setBassBoost(it) },
                        modifier = Modifier.width(120.dp),
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Bass Boost", color = Color.White.copy(alpha = 0.8f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Slider(
                        value = surround,
                        onValueChange = { viewModel.setSurround(it) },
                        modifier = Modifier.width(120.dp),
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Surround", color = Color.White.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Save Custom Preset")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Custom Preset")
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("Turn on Equalizer to adjust audio settings", color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}
