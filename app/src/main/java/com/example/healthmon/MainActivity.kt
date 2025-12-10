package com.example.healthmon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthmon.ui.caregiver.CaregiverScreen
import com.example.healthmon.ui.login.LoginScreen
import com.example.healthmon.ui.patient.PatientScreen
import com.example.healthmon.ui.theme.HealthMonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthMonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { userType ->
                    if (userType == "patient") {
                        navController.navigate("patient") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else if (userType == "caregiver") {
                        navController.navigate("caregiver") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("patient") {
            PatientScreen()
        }
        composable("caregiver") {
            CaregiverScreen()
        }
    }
}
