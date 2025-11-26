package com.example.healthmon

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// In file: app/src/main/java/com/example/healthmon/LoginActivity.kt

// ... other code in LoginActivity.kt

@Preview(showBackground = true)
@Composable
fun SomeOtherScreenPreview() { // Renamed from DefaultPreview
    MaterialTheme {
        // The composable you want to preview from this file
        LoginScreen()
    }
}