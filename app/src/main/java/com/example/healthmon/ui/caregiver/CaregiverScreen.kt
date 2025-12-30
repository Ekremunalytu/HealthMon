package com.example.healthmon.ui.caregiver

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthmon.ui.components.HealthMonBottomNavBar
import com.example.healthmon.ui.components.HealthMonNavItems
import com.example.healthmon.ui.components.HeartRateChart
import com.example.healthmon.ui.theme.*

data class Patient(
    val name: String,
    val isActive: Boolean = false,
    val emoji: String = "👤"
)

data class MedicalHistoryItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val status: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun CaregiverScreen(
    viewModel: CaregiverViewModel = hiltViewModel(),
    token: String = "",
    caregiverId: String = "",
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedNavItem by remember { mutableStateOf("home") }
    
    // Logout Dialog State
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Load patients when screen opens
    LaunchedEffect(Unit) {
        if (caregiverId.isNotEmpty() && token.isNotEmpty()) {
            viewModel.loadPatients(caregiverId, token)
        }
    }
    
    // Get data from ViewModel
    val vitalDataState by viewModel.vitalDataState.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val selectedPatientId by viewModel.selectedPatientId.collectAsState()
    
    val heartRate = vitalDataState.heartRate.bpm
    
    // Map API PatientInfo to UI Patient model (keeping existing UI structure for now)
    val uiPatients = patients.map { info ->
        Patient(
            name = info.name,
            isActive = info.id == selectedPatientId, // Active if selected
            emoji = "👤" // Default emoji
        )
    }

    val medicalHistory = remember {
        listOf(
            MedicalHistoryItem(
                "İlaç Takviyesi",
                "Metformin 500mg",
                "09:00",
                "Alındı",
                Icons.Filled.Add,
                Color(0xFFA855F7)
            ),
            MedicalHistoryItem(
                "Tansiyon Ölçümü",
                "Düzenli kontrol",
                "08:30",
                "120/80",
                Icons.Filled.Favorite,
                Color(0xFF14B8A6)
            ),
            MedicalHistoryItem(
                "Kahvaltı",
                "Diyet programı",
                "08:00",
                "Tamamlandı",
                Icons.Filled.Home,
                Color(0xFFF59E0B)
            )
        )
    }

    // Logout Confirmation Dialog
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Background glow effects
        Box(
            modifier = Modifier
                .offset(x = 180.dp, y = (-80).dp)
                .size(200.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            // Header with logout button
            CaregiverHeader(
                onSettingsClick = { showLogoutDialog = true }
            )
            
            // Patient Selector
            PatientSelector(
                patients = uiPatients,
                selectedIndex = patients.indexOfFirst { it.id == selectedPatientId }.takeIf { it >= 0 } ?: 0,
                onPatientSelected = { index -> 
                    if (index in patients.indices) {
                        viewModel.selectPatient(patients[index].id, token)
                    }
                }
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Heart Rate Chart - now dynamic!
                HeartRateChart(
                    heartRate = heartRate,
                    isNormal = heartRate in 60..100
                )
                
                // Stats Grid
                StatsGrid()
                
                // Medical History Dialog State
                var showHistoryDialog by remember { mutableStateOf(false) }
                
                if (showHistoryDialog) {
                    MedicalHistoryDialog(
                        items = medicalHistory,
                        onDismiss = { showHistoryDialog = false }
                    )
                }
                
                // Medical History
                MedicalHistorySection(
                    items = medicalHistory,
                    onViewAllClick = { showHistoryDialog = true }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Bottom Navigation
        HealthMonBottomNavBar(
            items = HealthMonNavItems.caregiverItems,
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
private fun CaregiverHeader(
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👩‍⚕️", fontSize = 24.sp)
                // Online indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(BackgroundDark)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Primary)
                    )
                }
            }
            
            Column {
                Text(
                    text = "HOŞGELDİN",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Dr. Zeynep",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Notification button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .clickable { /* TODO */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Bildirimler",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                // Notification badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
            }
            
            // Settings/Logout button
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
                    contentDescription = "Ayarlar / Çıkış",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun PatientSelector(
    patients: List<Patient>,
    selectedIndex: Int,
    onPatientSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        patients.forEachIndexed { index, patient ->
            PatientChip(
                patient = patient,
                isSelected = index == selectedIndex,
                onClick = { onPatientSelected(index) }
            )
        }
    }
}

@Composable
private fun PatientChip(
    patient: Patient,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chip_scale"
    )
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (isSelected) Primary 
                else SurfaceDark
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) BackgroundDark.copy(alpha = 0.3f)
                    else Color.White.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (patient.isActive) {
                Text(text = patient.emoji, fontSize = 16.sp)
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (isSelected) BackgroundDark else TextSecondaryDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Column {
            Text(
                text = patient.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) BackgroundDark else TextSecondaryDark,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected && patient.isActive) {
                Text(
                    text = "Aktif Hasta",
                    style = MaterialTheme.typography.labelSmall,
                    color = BackgroundDark.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun StatsGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Activity Card
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Person,
            iconColor = InfoBlue,
            title = "Hareket",
            value = "45",
            unit = "dk",
            progressValue = 0.75f,
            progressColor = InfoBlue,
            subtitle = "+10% hedef",
            subtitleColor = Primary
        )
        
        // Goal Card
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Star,
            iconColor = AlertOrange,
            title = "Kalan Hedef",
            value = "15",
            unit = "dk",
            extraInfo = "Bugün" to "60 dk"
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    unit: String,
    progressValue: Float? = null,
    progressColor: Color = Primary,
    subtitle: String? = null,
    subtitleColor: Color = Primary,
    extraInfo: Pair<String, String>? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark.copy(alpha = 0.8f),
                        SurfaceDark.copy(alpha = 0.6f)
                    )
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark
        )
        
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        if (progressValue != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressValue)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressColor)
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        if (extraInfo != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = extraInfo.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark
                )
                Text(
                    text = extraInfo.second,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MedicalHistorySection(
    items: List<MedicalHistoryItem>,
    onViewAllClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tıbbi Geçmiş",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tümü",
                style = MaterialTheme.typography.labelMedium,
                color = Primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }
        
        items.forEach { item ->
            MedicalHistoryCard(item = item)
        }
    }
}

/**
 * Dialog showing all medical history items
 */
@Composable
private fun MedicalHistoryDialog(
    items: List<MedicalHistoryItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardDark,
        title = {
            Text(
                text = "Tüm Tıbbi Geçmiş",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Extended mock data for demo
                val allItems = items + listOf(
                    MedicalHistoryItem(
                        "Kan Şekeri Kontrolü",
                        "Rutin ölçüm",
                        "07:30",
                        "95 mg/dL",
                        Icons.Filled.Favorite,
                        Color(0xFFEC4899)
                    ),
                    MedicalHistoryItem(
                        "Egzersiz",
                        "Hafif yürüyüş",
                        "Dün 16:00",
                        "30 dk",
                        Icons.Filled.Person,
                        Color(0xFF22C55E)
                    ),
                    MedicalHistoryItem(
                        "İlaç Takviyesi",
                        "Aspirin 100mg",
                        "Dün 21:00",
                        "Alındı",
                        Icons.Filled.Add,
                        Color(0xFFA855F7)
                    )
                )
                
                allItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(item.iconColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = item.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                            Text(
                                text = item.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun MedicalHistoryCard(item: MedicalHistoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable { /* TODO */ }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(item.iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = item.time,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.status,
                style = MaterialTheme.typography.labelSmall,
                color = if (item.status == "Alındı" || item.status == "Tamamlandı") Primary else Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
