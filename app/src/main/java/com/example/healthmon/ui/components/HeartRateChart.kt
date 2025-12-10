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

@Composable
fun HeartRateChart(
    heartRate: Int,
    isNormal: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chart_animation")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
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
        
        // Chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            
            // Create gradient brush
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(Primary.copy(alpha = 0.4f), Primary.copy(alpha = 0f)),
                startY = 0f,
                endY = height
            )
            
            // Generate wave path
            val path = Path()
            val fillPath = Path()
            
            val points = 100
            val segmentWidth = width / points
            
            path.moveTo(0f, centerY)
            fillPath.moveTo(0f, height)
            fillPath.lineTo(0f, centerY)
            
            for (i in 0..points) {
                val x = i * segmentWidth
                val offset = (i.toFloat() / points + animatedOffset) * 2 * Math.PI
                
                // Create heartbeat-like pattern
                val y = when {
                    i % 20 in 8..12 -> centerY - (40 * kotlin.math.sin((i % 20 - 8) * Math.PI / 4)).toFloat()
                    i % 20 == 13 -> centerY + 30
                    else -> centerY + (5 * kotlin.math.sin(offset)).toFloat()
                }
                
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            fillPath.lineTo(width, height)
            fillPath.close()
            
            // Draw fill gradient
            drawPath(fillPath, gradientBrush)
            
            // Draw line
            drawPath(
                path = path,
                color = Primary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            
            // Draw pulsing dot at end
            drawCircle(
                color = Primary,
                radius = 6.dp.toPx() * pulseScale,
                center = Offset(width, centerY),
                alpha = 2f - pulseScale
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("10:00", "10:15", "10:30", "Şimdi").forEach { time ->
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
