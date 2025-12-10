package com.example.healthmon.ui.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthmon.ui.components.HealthMonButton
import com.example.healthmon.ui.components.HealthMonTextField
import com.example.healthmon.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedUserType by remember { mutableStateOf("patient") }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Background gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "bg_animation")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            val type = (uiState as LoginUiState.Success).userType
            onLoginSuccess(if (type == "Hasta") "patient" else "caregiver")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Background glow effects
        Box(
            modifier = Modifier
                .offset(x = (100 * glowOffset).dp, y = (-50).dp)
                .size(300.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .size(200.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // App Logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Primary.copy(alpha = 0.2f), Primary.copy(alpha = 0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏥",
                    fontSize = 48.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Welcome text
            Text(
                text = "Hoş Geldiniz",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sağlığınız kontrol altında, güvenle giriş yapın.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // User Type Selector (Segmented Control)
            UserTypeSelector(
                selectedType = selectedUserType,
                onTypeSelected = { selectedUserType = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Username Field
            HealthMonTextField(
                value = username,
                onValueChange = { username = it },
                label = "Kullanıcı Adı",
                leadingIcon = Icons.Filled.Person
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Şifre",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Şifremi Unuttum?",
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HealthMonTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "",
                    leadingIcon = Icons.Filled.Lock,
                    isPassword = true
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message
            if (uiState is LoginUiState.Error) {
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = AlertRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Login Button
            HealthMonButton(
                text = "Giriş Yap →",
                onClick = {
                    val userType = if (selectedUserType == "patient") "Hasta" else "Hasta Bakıcı"
                    viewModel.login(username, password, userType)
                },
                isLoading = uiState is LoginUiState.Loading
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.1f)
                )
                Text(
                    text = "  veya  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Social login buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialButton(emoji = "🔷", onClick = { /* Google */ })
                SocialButton(emoji = "🍎", onClick = { /* Apple */ })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign up link
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Hesabınız yok mu? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryDark
                )
                Text(
                    text = "Hemen Kaydolun",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun UserTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceDark)
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            UserTypeButton(
                label = "🤕 Hasta",
                isSelected = selectedType == "patient",
                onClick = { onTypeSelected("patient") },
                modifier = Modifier.weight(1f)
            )
            UserTypeButton(
                label = "👨‍⚕️ Hasta Bakıcı",
                isSelected = selectedType == "caregiver",
                onClick = { onTypeSelected("caregiver") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UserTypeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (isSelected) SurfaceVariantDark else Color.Transparent
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.White else TextSecondaryDark,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SocialButton(
    emoji: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(SurfaceDark)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
    }
}
