package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PedidoScreen(navController: NavController) {
    var direccion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Detalles del Pedido", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección de entrega") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha de entrega (dd/mm/aaaa)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                navController.navigate("confirmacion")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar Pedido")
        }
    }
}
