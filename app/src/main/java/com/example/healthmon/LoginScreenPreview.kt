package com.example.healthmon

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() { // Renamed the function
    MaterialTheme {
        LoginScreen()
    }
}