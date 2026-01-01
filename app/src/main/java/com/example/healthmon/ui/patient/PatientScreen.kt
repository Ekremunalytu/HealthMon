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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthmon.ui.components.HealthMonBottomNavBar
import com.example.healthmon.ui.components.HealthMonNavItems
import com.example.healthmon.ui.components.HeartRateChart
import com.example.healthmon.ui.theme.*

@Composable
fun PatientScreen(
    viewModel: PatientViewModel = hiltViewModel(),
    token: String = "",
    patientId: String = "",
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedNavItem by remember { mutableStateOf("home") }

    LaunchedEffect(patientId, token) {
        if (patientId.isNotEmpty() && token.isNotEmpty()) {
            viewModel.startDataStreaming(patientId, token)
        }
    }

    val vitalDataState by viewModel.vitalDataState.collectAsState()
    val heartRate = vitalDataState.heartRate.bpm
    val inactivityMinutes = vitalDataState.inactivity.durationMinutes

    var showEmergencyDialog by remember { mutableStateOf(false) }
    var emergencySending by remember { mutableStateOf(false) }

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

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceCardDark,
            title = {
                Text(
                    text = "Çıkış Yap",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Uygulamadan çıkış yapmak istediğinize emin misiniz?",
                    color = TextSecondaryDark,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogout)
                    }
                ) {
                    Text("Çıkış Yap", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("İptal", color = TextSecondaryDark)
                }
            }
        )
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { if (!emergencySending) showEmergencyDialog = false },
            containerColor = SurfaceCardDark,
            title = {
                Text(
                    text = "⚠️ ACİL DURUM",
                    color = AlertRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Acil yardım çağrısı göndermek istediğinize emin misiniz?",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bu işlem bakıcınıza bildirim gönderecek ve 112'yi arayacaktır.",
                        color = TextSecondaryDark,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (emergencySending) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Primary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Acil yardım gönderiliyor...",
                                color = Primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (patientId.isNotEmpty() && token.isNotEmpty()) {
                            emergencySending = true
                            viewModel.sendEmergency(
                                patientId = patientId,
                                message = "ACİL YARDIM! Hasta yardım istiyor.",
                                token = token
                            ) { success ->
                                emergencySending = false
                                showEmergencyDialog = false
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:112")
                                }
                                context.startActivity(intent)
                            }
                        }
                    },
                    enabled = !emergencySending
                ) {
                    Text("EVET, YARDIM ÇAĞIR", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEmergencyDialog = false },
                    enabled = !emergencySending
                ) {
                    Text("İptal", color = TextSecondaryDark)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            PatientHeader(
                onSettingsClick = { showLogoutDialog = true }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CaregiverCard(
                    onCallClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+905551234567")
                        }
                        context.startActivity(intent)
                    }
                )

                // Heart Rate Eşik Değerleri State'leri
                var showHeartRateDialog by remember { mutableStateOf(false) }
                var minHeartRate by remember { mutableIntStateOf(40) }
                var maxHeartRate by remember { mutableIntStateOf(120) }

                // Inactivity Süre State'leri
                var showInactivityDialog by remember { mutableStateOf(false) }
                var inactivityTargetMinutes by remember { mutableIntStateOf(60) }

                if (showHeartRateDialog) {
                    HeartRateThresholdDialog(
                        currentMin = minHeartRate,
                        currentMax = maxHeartRate,
                        onDismiss = { showHeartRateDialog = false },
                        onConfirm = { newMin, newMax ->
                            minHeartRate = newMin
                            maxHeartRate = newMax
                            showHeartRateDialog = false
                        }
                    )
                }

                if (showInactivityDialog) {
                    DurationSettingDialog(
                        currentDuration = inactivityTargetMinutes,
                        onDismiss = { showInactivityDialog = false },
                        onConfirm = { newDuration ->
                            inactivityTargetMinutes = newDuration
                            showInactivityDialog = false
                        }
                    )
                }

                HeartRateChart(
                    heartRate = heartRate,
                    isNormal = heartRate in minHeartRate..maxHeartRate
                )

                // YENİ EKLENEN KART
                HeartRateThresholdCard(
                    minThreshold = minHeartRate,
                    maxThreshold = maxHeartRate,
                    onSettingsClick = { showHeartRateDialog = true }
                )

                InactivityCard(
                    durationMinutes = inactivityMinutes,
                    targetMinutes = inactivityTargetMinutes,
                    onSettingsClick = { showInactivityDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                EmergencyButton(
                    onClick = { showEmergencyDialog = true },
                    modifier = Modifier.scale(emergencyScale)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        HealthMonBottomNavBar(
            items = HealthMonNavItems.patientItems,
            selectedRoute = selectedNavItem,
            onItemSelected = { selectedNavItem = it },
            onEmergencyClick = { showEmergencyDialog = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// --- Diğer Composable Fonksiyonlar (PatientHeader, CaregiverCard, vb.) değişmeden kalır ---
@Composable
private fun PatientHeader(
    onSettingsClick: () -> Unit
) {
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
                .clickable { onSettingsClick() },
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
fun HeartRateThresholdCard(
    minThreshold: Int,
    maxThreshold: Int,
    onSettingsClick: () -> Unit
) {
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(InfoBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonitorHeart,
                        contentDescription = null,
                        tint = InfoBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Nabız Eşikleri",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$minThreshold - $maxThreshold BPM",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Divider(color = Color.White.copy(alpha = 0.05f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSettingsClick() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nabız uyarı limitlerini düzenle",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Değiştir",
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
fun HeartRateThresholdDialog(
    currentMin: Int,
    currentMax: Int,
    onDismiss: () -> Unit,
    onConfirm: (min: Int, max: Int) -> Unit
) {
    var minBpm by remember { mutableStateOf(currentMin.toString()) }
    var maxBpm by remember { mutableStateOf(currentMax.toString()) }
    var minError by remember { mutableStateOf(false) }
    var maxError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardDark,
        title = {
            Text(
                "Nabız Eşikleri Ayarla",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Uyarı almak istediğiniz minimum ve maksimum nabız değerlerini girin.",
                    color = TextSecondaryDark,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minBpm,
                        onValueChange = { minBpm = it.filter { c -> c.isDigit() }; minError = false },
                        label = { Text("Min BPM") },
                        isError = minError,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BackgroundDark,
                            unfocusedContainerColor = BackgroundDark,
                            focusedIndicatorColor = Primary,
                            unfocusedIndicatorColor = SurfaceDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = TextSecondaryDark,
                             errorContainerColor = BackgroundDark,
                        )
                    )
                    OutlinedTextField(
                        value = maxBpm,
                        onValueChange = { maxBpm = it.filter { c -> c.isDigit() }; maxError = false },
                        label = { Text("Max BPM") },
                        isError = maxError,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BackgroundDark,
                            unfocusedContainerColor = BackgroundDark,
                            focusedIndicatorColor = Primary,
                            unfocusedIndicatorColor = SurfaceDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = TextSecondaryDark,
                            errorContainerColor = BackgroundDark,
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val min = minBpm.toIntOrNull()
                    val max = maxBpm.toIntOrNull()
                    minError = min == null
                    maxError = max == null
                    if (min != null && max != null && min < max) {
                        onConfirm(min, max)
                    } else {
                        // Show error state, maybe a Toast or text in dialog
                        if(min != null && max != null && min >= max) minError = true
                    }
                }
            ) {
                Text("Kaydet", color = Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextSecondaryDark)
            }
        }
    )
}


@Composable
private fun InactivityCard(
    durationMinutes: Int = 0,
    targetMinutes: Int = 60,
    onSettingsClick: () -> Unit = {}
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
                modifier = Modifier.clickable { onSettingsClick() },
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
private fun DurationSettingDialog(
    currentDuration: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedDuration by remember { mutableIntStateOf(currentDuration) }
    val durationOptions = listOf(15, 30, 45, 60, 90, 120)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardDark,
        title = {
            Text(
                text = "Hareketsizlik Süresi Ayarla",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Uyarı almak istediğiniz süreyi seçin:",
                    color = TextSecondaryDark,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                durationOptions.forEach { duration ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedDuration == duration) Primary.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable { selectedDuration = duration }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$duration dakika",
                            color = if (selectedDuration == duration) Primary else Color.White,
                            fontWeight = if (selectedDuration == duration) FontWeight.Bold else FontWeight.Normal
                        )
                        if (selectedDuration == duration) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedDuration) }
            ) {
                Text("Kaydet", color = Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextSecondaryDark)
            }
        }
    )
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
