package com.example.healthmon.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.healthmon.ui.theme.AlertRed
import com.example.healthmon.ui.theme.Primary
import com.example.healthmon.ui.theme.SurfaceDark
import com.example.healthmon.ui.theme.TextSecondaryDark

data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@Composable
fun HealthMonBottomNavBar(
    items: List<NavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Glassmorphism container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceDark.copy(alpha = 0.8f),
                            SurfaceDark.copy(alpha = 0.6f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left items
                items.take(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = selectedRoute == item.route,
                        onClick = { onItemSelected(item.route) }
                    )
                }
                
                // Spacer for center SOS button
                Spacer(modifier = Modifier.width(56.dp))
                
                // Right items
                items.drop(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = selectedRoute == item.route,
                        onClick = { onItemSelected(item.route) }
                    )
                }
            }
        }
        
        // Center SOS Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .size(56.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = AlertRed.copy(alpha = 0.4f),
                    spotColor = AlertRed.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(AlertRed)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onEmergencyClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Acil Yardım",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) Primary else TextSecondaryDark,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "nav_color"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "nav_scale"
    )
    
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .scale(animatedScale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = animatedColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

// Predefined nav items for the app
object HealthMonNavItems {
    val patientItems = listOf(
        NavItem("Ana Sayfa", Icons.Filled.Home, Icons.Outlined.Home, "home"),
        NavItem("Sağlık", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "health"),
        NavItem("Takvim", Icons.Filled.DateRange, Icons.Outlined.DateRange, "calendar"),
        NavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person, "profile")
    )
    
    val caregiverItems = listOf(
        NavItem("Ana Sayfa", Icons.Filled.Home, Icons.Outlined.Home, "home"),
        NavItem("Takvim", Icons.Filled.DateRange, Icons.Outlined.DateRange, "calendar"),
        NavItem("Mesajlar", Icons.Filled.Email, Icons.Outlined.Email, "messages"),
        NavItem("Ayarlar", Icons.Filled.Settings, Icons.Outlined.Settings, "settings")
    )
}
