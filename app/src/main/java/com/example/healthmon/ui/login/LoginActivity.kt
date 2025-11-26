package com.example.healthmon.ui.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val userTypes = listOf("Hasta", "Hasta Bakıcı")
    var selectedUserType by remember { mutableStateOf(userTypes[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Giriş Yap", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Kullanıcı Adı") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("Kullanıcı Tipi:", style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.fillMaxWidth()) {
            userTypes.forEach { userType ->
                Row(
                    Modifier
                        .selectable(
                            selected = (userType == selectedUserType),
                            onClick = { selectedUserType = userType }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (userType == selectedUserType),
                        onClick = { selectedUserType = userType }
                    )
                    Text(
                        text = userType,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // TODO: Giriş işlemleri burada yapılacak.
                // Örneğin: `username`, `password` ve `selectedUserType` ile
                // bir API isteği atılabilir veya veritabanı kontrolü yapılabilir.
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Giriş Yap")
        }
    }
}
