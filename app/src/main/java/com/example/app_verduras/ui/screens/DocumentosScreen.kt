package com.example.app_verduras.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.app_verduras.R
import com.example.app_verduras.Model.Documento
import com.example.app_verduras.ui.components.AdminBottomNavBar
import com.example.app_verduras.ui.components.AdminSection
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.viewmodel.DocumentoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosScreen(
    viewModel: DocumentoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProducts: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onLogout: () -> Unit
) {
    val documentos by viewModel.documentos.collectAsStateWithLifecycle(initialValue = emptyList())
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()
    val uploadSuccess by viewModel.uploadSuccess.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launcher para el selector de archivos del sistema
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
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
                title = { 
                    Text(
                        "Gestionar Documentos",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al menú")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HuertoHogarColors.Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            AdminBottomNavBar(
                currentSection = AdminSection.DOCUMENTS,
                onNavigateToMenu = onNavigateBack,
                onNavigateToProducts = onNavigateToProducts,
                onNavigateToUsers = onNavigateToUsers,
                onNavigateToOrders = onNavigateToOrders,
                onNavigateToDocuments = { /* Ya estamos aquí */ }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!isUploading) {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    }
                },
                containerColor = HuertoHogarColors.Primary,
                contentColor = Color.White
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp), 
                        strokeWidth = 3.dp,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Documento")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HuertoHogarColors.Primary.copy(alpha = 0.08f),
                            HuertoHogarColors.Accent.copy(alpha = 0.5f),
                            HuertoHogarColors.Background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(documentos) {
                    documento -> DocumentoItem(documento = documento)
                }
            }

            // --- Dialogo de exito ---
            if (uploadSuccess) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissUploadSuccessDialog() },
                    title = { Text("Éxito") },
                    text = { Text("¡Archivo subido correctamente!") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissUploadSuccessDialog() }) {
                            Text("Aceptar")
                        }
                    }
                )
            }

            // --- Overlay de Carga con Lottie ---
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.saving_cloud))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentoItem(documento: Documento) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(documento.nombre) },
            // Opcional: Podrías usar la URL para mostrar el doc o descargarlo al hacer clic
            leadingContent = {
                Icon(Icons.Default.Description, contentDescription = "Icono de documento")
            }
        )
    }
}
