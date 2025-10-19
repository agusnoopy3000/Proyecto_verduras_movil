package com.example.app_verduras.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_verduras.Model.Documento
import com.example.app_verduras.viewmodel.DocumentoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosScreen(
    viewModel: DocumentoViewModel,
    onNavigateBack: () -> Unit
) {
    val documentos by viewModel.documentos.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    // Launcher para el selector de archivos del sistema
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Obtenemos el nombre del archivo
                val cursor = context.contentResolver.query(it, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayName = it.getString(it.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
                        viewModel.addDocumento(uri, displayName)
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Documentos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Documento")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(documentos) { documento ->
                DocumentoItem(documento = documento)
            }
        }
    }
}

@Composable
fun DocumentoItem(documento: Documento) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(documento.nombre) },
            leadingContent = {
                Icon(Icons.Default.Description, contentDescription = "Icono de documento")
            }
        )
    }
}
