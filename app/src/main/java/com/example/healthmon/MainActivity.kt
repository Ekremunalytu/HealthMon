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

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            com.example.healthmon.ui.splash.SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToPatient = { token, patientId ->
                    navController.navigate("patient/$token/$patientId") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToCaregiver = { token, caregiverId ->
                    navController.navigate("caregiver/$token/$caregiverId") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { userType, token, userId ->
                    // userType decides destination (already mapped in LoginScreen logic)
                    // The IDs passed here: userId is either patientId or caregiverId in current implementation
                    if (userType == "patient") {
                        navController.navigate("patient/$token/$userId") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else if (userType == "caregiver") {
                        navController.navigate("caregiver/$token/$userId") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(
            route = "patient/{token}/{patientId}",
            arguments = listOf(
                androidx.navigation.navArgument("token") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("patientId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            PatientScreen(
                token = token,
                patientId = patientId,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "caregiver/{token}/{caregiverId}",
            arguments = listOf(
                androidx.navigation.navArgument("token") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("caregiverId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val caregiverId = backStackEntry.arguments?.getString("caregiverId") ?: ""
            CaregiverScreen(
                token = token,
                caregiverId = caregiverId,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
    }
}
