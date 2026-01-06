package com.example.healthmon

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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
    
    // Android 12+ için gerekli izinler
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            checkLocationServicesEnabled()
        } else {
            Toast.makeText(
                this,
                "Bluetooth taraması için izinler gereklidir",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Runtime izinlerini kontrol et ve iste
        checkAndRequestPermissions()
        
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
    
    override fun onResume() {
        super.onResume()
        // Kullanıcı ayarlardan dönünce konum servisini tekrar kontrol et
        if (hasAllPermissions()) {
            checkLocationServicesEnabled()
        }
    }
    
    private fun checkAndRequestPermissions() {
        if (!hasAllPermissions()) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            checkLocationServicesEnabled()
        }
    }
    
    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun checkLocationServicesEnabled() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        if (!gpsEnabled && !networkEnabled) {
            Toast.makeText(
                this,
                "⚠️ ESP32'yi görmek için KONUM SERVİSİNİ AÇIN!",
                Toast.LENGTH_LONG
            ).show()
            
            // Kullanıcıyı konum ayarlarına yönlendir
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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
