package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_verduras.ui.components.AppFooter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateToProductManagement: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToOrderManagement: () -> Unit,
    onNavigateToDocumentManagement: () -> Unit, // Añadido
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administrador") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToOrderManagement,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gestionar Pedidos")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToDocumentManagement, // Añadido
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gestionar Documentos")
                }
            }

            // --- Footer ---
            AppFooter()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
