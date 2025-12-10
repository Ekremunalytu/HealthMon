package com.example.healthmon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.healthmon.ui.theme.Primary
import com.example.healthmon.ui.theme.SurfaceCardDark
import kotlin.math.sin

@Composable
fun HeartRateChart(
    heartRate: Int,
    isNormal: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Continuous scrolling animation - resets every 2 seconds
    val infiniteTransition = rememberInfiniteTransition(label = "ecg")
    val scrollProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scroll"
    )
    
    // Pulsing dot animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCardDark)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "❤️",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Canlı Nabız",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$heartRate",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "BPM",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            
            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isNormal) Primary.copy(alpha = 0.15f)
                        else Color(0xFFFF4D4D).copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isNormal) "NORMAL" else "DİKKAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNormal) Primary else Color(0xFFFF4D4D)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // ECG Chart - Full width smooth scrolling
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            
            // Gradient fill
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(Primary.copy(alpha = 0.3f), Primary.copy(alpha = 0f)),
                startY = 0f,
                endY = height
            )
            
            val path = Path()
            val fillPath = Path()
            
            // Number of complete heartbeat cycles visible
            val cyclesVisible = 6
            val pointsPerCycle = 50  // Smooth curve
            val totalPoints = cyclesVisible * pointsPerCycle
            val segmentWidth = width / totalPoints
            
            // Spike height based on heart rate
            val normalizedBpm = ((heartRate - 40f) / 80f).coerceIn(0.5f, 1.2f)
            val baseHeight = height * 0.35f * normalizedBpm
            
            path.moveTo(0f, centerY)
            fillPath.moveTo(0f, height)
            fillPath.lineTo(0f, centerY)
            
            for (i in 0..totalPoints) {
                val x = i * segmentWidth
                
                // Calculate phase within each heartbeat cycle (0 to 1)
                val cyclePhase = ((i.toFloat() / pointsPerCycle) + scrollProgress) % 1f
                
                // Create realistic ECG waveform:
                // P wave (small bump), QRS complex (big spike), T wave (medium bump)
                val y = when {
                    // P wave: small gentle bump (10-20% of cycle)
                    cyclePhase in 0.10f..0.20f -> {
                        val pPhase = (cyclePhase - 0.10f) / 0.10f
                        centerY - baseHeight * 0.15f * sin(pPhase * Math.PI).toFloat()
                    }
                    // Q dip: small dip before main spike (25-28% of cycle)
                    cyclePhase in 0.25f..0.28f -> {
                        val qPhase = (cyclePhase - 0.25f) / 0.03f
                        centerY + baseHeight * 0.1f * sin(qPhase * Math.PI).toFloat()
                    }
                    // R spike: main tall spike UP (28-35% of cycle)
                    cyclePhase in 0.28f..0.35f -> {
                        val rPhase = (cyclePhase - 0.28f) / 0.07f
                        centerY - baseHeight * sin(rPhase * Math.PI).toFloat()
                    }
                    // S dip: sharp dip DOWN (35-40% of cycle)
                    cyclePhase in 0.35f..0.40f -> {
                        val sPhase = (cyclePhase - 0.35f) / 0.05f
                        centerY + baseHeight * 0.4f * sin(sPhase * Math.PI).toFloat()
                    }
                    // T wave: recovery bump (50-65% of cycle)
                    cyclePhase in 0.50f..0.65f -> {
                        val tPhase = (cyclePhase - 0.50f) / 0.15f
                        centerY - baseHeight * 0.25f * sin(tPhase * Math.PI).toFloat()
                    }
                    // Baseline
                    else -> centerY
                }
                
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            fillPath.lineTo(width, height)
            fillPath.close()
            
            // Draw gradient fill
            drawPath(fillPath, gradientBrush)
            
            // Draw ECG line
            drawPath(
                path = path,
                color = Primary,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            
            // Pulsing dot at current position (right side)
            val dotX = width * 0.95f
            val dotCyclePhase = ((dotX / segmentWidth / pointsPerCycle) + scrollProgress) % 1f
            val dotY = when {
                dotCyclePhase in 0.28f..0.35f -> {
                    val rPhase = (dotCyclePhase - 0.28f) / 0.07f
                    centerY - baseHeight * sin(rPhase * Math.PI).toFloat()
                }
                dotCyclePhase in 0.35f..0.40f -> {
                    val sPhase = (dotCyclePhase - 0.35f) / 0.05f
                    centerY + baseHeight * 0.4f * sin(sPhase * Math.PI).toFloat()
                }
                else -> centerY
            }
            
            drawCircle(
                color = Primary,
                radius = 6.dp.toPx() * pulseScale,
                center = Offset(dotX, dotY),
                alpha = 2f - pulseScale
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("-12s", "-9s", "-6s", "-3s", "Şimdi").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (label == "Şimdi") Primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

