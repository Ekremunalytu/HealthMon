package com.example.healthmon.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthmon.ui.theme.BackgroundDark
import com.example.healthmon.ui.theme.Primary

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToPatient: (String, String) -> Unit,
    onNavigateToCaregiver: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SplashUiState.NavigateToLogin -> onNavigateToLogin()
            is SplashUiState.NavigateToPatient -> {
                val state = uiState as SplashUiState.NavigateToPatient
                onNavigateToPatient(state.token, state.patientId)
            }
            is SplashUiState.NavigateToCaregiver -> {
                val state = uiState as SplashUiState.NavigateToCaregiver
                onNavigateToCaregiver(state.token, state.caregiverId)
            }
            is SplashUiState.Loading -> {
                // Stay here
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
             Text(
                text = "🏥",
                fontSize = 80.sp
            )
        }
        CircularProgressIndicator(
            modifier = Modifier.size(120.dp),
            color = Primary,
            strokeWidth = 4.dp
        )
    }
}
