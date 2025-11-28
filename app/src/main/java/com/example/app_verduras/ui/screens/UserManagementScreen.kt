package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_verduras.Model.User
import com.example.app_verduras.repository.UserRepository
import com.example.app_verduras.ui.components.*
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.viewmodel.UserManagementViewModel
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProducts: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToDocuments: () -> Unit = {},
    onLogout: () -> Unit
) {
    val users by viewModel.users.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    var userToEdit by remember { mutableStateOf<User?>(null) }
    val toastState = rememberToastState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UserManagementViewModel.UiEvent.ShowSnackbar -> {
                    toastState.showSuccess(event.message)
                }
                is UserManagementViewModel.UiEvent.DismissEditModal -> {
                    userToEdit = null
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                "Gestionar Usuarios",
                                fontWeight = FontWeight.Bold
                            )
                            // Mostrar estado de sincronización
                            Text(
                                text = when (syncStatus) {
                                    is UserRepository.SyncStatus.Syncing -> "Sincronizando..."
                                    is UserRepository.SyncStatus.Success -> "✓ Sincronizado"
                                    is UserRepository.SyncStatus.Error -> "⚠ Error de sync"
                                    else -> "Firebase en tiempo real"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver al menú"
                            )
                        }
                    },
                    actions = {
                        // Botón de refresh
                        IconButton(
                            onClick = { viewModel.refreshUsers() },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, "Actualizar usuarios")
                            }
                        }
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
                    currentSection = AdminSection.USERS,
                    onNavigateToMenu = onNavigateBack,
                    onNavigateToProducts = onNavigateToProducts,
                    onNavigateToUsers = { /* Ya estamos aquí */ },
                    onNavigateToOrders = onNavigateToOrders,
                    onNavigateToDocuments = onNavigateToDocuments
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HuertoHogarColors.Primary.copy(alpha = 0.08f),
                                HuertoHogarColors.Accent.copy(alpha = 0.5f),
                                HuertoHogarColors.Background
                            )
                        )
                    )
            ) {
                // Contador de usuarios
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = HuertoHogarColors.Primary
                            )
                            Text(
                                "Total de usuarios",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = HuertoHogarColors.Primary
                        ) {
                            Text(
                                text = "${users.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserItem(
                            user = user,
                            onEditClick = { userToEdit = user }
                        )
                    }
                }
            }
        }

        // Toast animado en la parte superior
        AnimatedSuccessToast(
            toastData = toastState.currentToast,
            onDismiss = { toastState.dismiss() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )
    }

    userToEdit?.let { user ->
        EditUserModal(
            user = user,
            onDismiss = { userToEdit = null },
            onSave = { updatedUser ->
                viewModel.updateUser(updatedUser)
            }
        )
    }
}

@Composable
fun UserItem(
    user: User,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del usuario
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = HuertoHogarColors.Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.nombre.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HuertoHogarColors.Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HuertoHogarColors.TextSecondary
                )
                if (!user.telefono.isNullOrBlank()) {
                    Text(
                        text = "📞 ${user.telefono}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HuertoHogarColors.TextSecondary
                    )
                }
            }
            
            FilledTonalIconButton(
                onClick = onEditClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = HuertoHogarColors.Secondary.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar Usuario",
                    tint = HuertoHogarColors.Secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserModal(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var nombre by remember { mutableStateOf(user.nombre) }
    var email by remember { mutableStateOf(user.email) }
    var direccion by remember { mutableStateOf(user.direccion ?: "") }
    var telefono by remember { mutableStateOf(user.telefono ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = HuertoHogarColors.Primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { 
            Text(
                "Editar Usuario",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuertoHogarColors.Primary,
                        focusedLabelColor = HuertoHogarColors.Primary
                    )
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuertoHogarColors.Primary,
                        focusedLabelColor = HuertoHogarColors.Primary
                    )
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuertoHogarColors.Primary,
                        focusedLabelColor = HuertoHogarColors.Primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    val updatedUser = user.copy(
                        nombre = nombre,
                        email = email,
                        direccion = direccion,
                        telefono = telefono
                    )
                    onSave(updatedUser)
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuertoHogarColors.Primary
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
