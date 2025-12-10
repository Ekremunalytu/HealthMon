package com.example.healthmon.ui.patient

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmon.ui.components.HealthMonBottomNavBar
import com.example.healthmon.ui.components.HealthMonNavItems
import com.example.healthmon.ui.components.HeartRateChart
import com.example.healthmon.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun PatientScreen() {
    val context = LocalContext.current
    var selectedNavItem by remember { mutableStateOf("home") }
    
    // Simulated heart rate (updates every second with small variations)
    var heartRate by remember { mutableIntStateOf(78) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)  // Every second
            // Small variation: ±3 BPM from current value, stay in 60-100 range
            val change = Random.nextInt(-3, 4)
            heartRate = (heartRate + change).coerceIn(60, 100)
        }
    }
    
    // Simulated inactivity timer (increments every minute for demo: every 5 sec)
    var inactivityMinutes by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)  // 5 sec for demo (use 60000L for real minutes)
            inactivityMinutes++
        }
    }
    
    // Emergency button pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "emergency_pulse")
    val emergencyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Background glow effects
        Box(
            modifier = Modifier
                .offset(x = 200.dp, y = (-100).dp)
                .size(250.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 100.dp)
                .size(180.dp)
                .blur(60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(InfoBlue.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            // Header
            PatientHeader()
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Caregiver Card
                CaregiverCard(
                    onCallClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+905551234567")  // Bakıcı numarası
                        }
                        context.startActivity(intent)
                    }
                )
                
                // Heart Rate Chart - now dynamic!
                HeartRateChart(
                    heartRate = heartRate,
                    isNormal = heartRate in 60..100
                )
                
                // Inactivity Status Card
                InactivityCard(
                    durationMinutes = inactivityMinutes,
                    targetMinutes = 60
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Emergency Button - opens dialer for 112
                EmergencyButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:112")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.scale(emergencyScale)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Bottom Navigation
        HealthMonBottomNavBar(
            items = HealthMonNavItems.patientItems,
            selectedRoute = selectedNavItem,
            onItemSelected = { selectedNavItem = it },
            onEmergencyClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:112")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PatientHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hoş geldin,",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark
            )
            Text(
                text = "Ahmet Bey",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .clickable { /* TODO: Settings */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Ayarlar",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CaregiverCard(
    onCallClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCardDark)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👩‍⚕️",
                    fontSize = 28.sp
                )
            }
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Hemşire Ayşe Yılmaz",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "HASTA BAKICINIZ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.sp
                )
            }
            
            // Call button - now functional!
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Primary.copy(alpha = 0.4f),
                        spotColor = Primary.copy(alpha = 0.4f)
                    )
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable(onClick = onCallClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Ara",
                    tint = BackgroundDark,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun InactivityCard(
    durationMinutes: Int = 0,
    targetMinutes: Int = 60
) {
    val progress = (durationMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()
    val isAlert = durationMinutes >= targetMinutes
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCardDark)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isAlert) AlertRed.copy(alpha = 0.15f) else AlertOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (isAlert) AlertRed else AlertOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Hareketsizlik Süresi",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "$durationMinutes dk",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isAlert) AlertRed else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Progress indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isAlert) AlertRed.copy(alpha = 0.1f) else Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAlert) AlertRed else Primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isAlert) 
                                listOf(AlertRed.copy(alpha = 0.7f), AlertRed)
                            else 
                                listOf(AlertOrange.copy(alpha = 0.7f), AlertOrange)
                        )
                    )
            )
        }
        
        Divider(color = Color.White.copy(alpha = 0.05f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hedef: $targetMinutes dk",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
            Row(
                modifier = Modifier.clickable { /* TODO */ },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Süreyi Ayarla",
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmergencyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AlertRed.copy(alpha = 0.5f),
                spotColor = AlertRed.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AlertRed)
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "ACİL YARDIM ÇAĞIR",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
