package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminPanelScreen(
    onNavigateToProductManagement: () -> Unit,
    onNavigateToUserManagement: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Panel de Administrador",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateToProductManagement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar Productos")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToUserManagement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar Usuarios")
        }
    }
}
