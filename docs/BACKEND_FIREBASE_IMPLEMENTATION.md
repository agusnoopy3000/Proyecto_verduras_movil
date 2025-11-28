# Implementación Firebase Sync para Spring Boot - Huerto Hogar

## Resumen
Implementar sincronización automática de pedidos y usuarios desde Spring Boot hacia Firebase Firestore.

## Archivos a Crear/Modificar

### 1. pom.xml - Agregar dependencia
```xml
<!-- Firebase Admin SDK -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

### 2. FirebaseConfig.java (CREAR)
```java
package com.huertohogar.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    
    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("✅ Firebase Admin SDK inicializado correctamente");
            }
        } catch (IOException e) {
            logger.error("❌ Error al inicializar Firebase: {}. La sincronización con Firestore no estará disponible.", e.getMessage());
        }
    }
}
```

### 3. FirestoreSyncService.java (CREAR)
```java
package com.huertohogar.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.huertohogar.model.Pedido;
import com.huertohogar.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class FirestoreSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirestoreSyncService.class);
    private static final String COLLECTION_PEDIDOS = "pedidos";
    private static final String COLLECTION_USERS = "users";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private boolean isFirebaseAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }
    
    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
    
    // ==================== PEDIDOS ====================
    
    @Async
    public void syncPedido(Pedido pedido) {
        if (!isFirebaseAvailable()) {
            logger.warn("Firebase no disponible, omitiendo sincronización de pedido {}", pedido.getId());
            return;
        }
        
        try {
            Map<String, Object> pedidoData = new HashMap<>();
            pedidoData.put("id", pedido.getId());
            pedidoData.put("userEmail", pedido.getUsuario() != null ? pedido.getUsuario().getEmail() : null);
            pedidoData.put("fechaEntrega", pedido.getFechaEntrega() != null ? 
                pedido.getFechaEntrega().toString() : null);
            pedidoData.put("direccionEntrega", pedido.getDireccionEntrega());
            pedidoData.put("region", pedido.getRegion());
            pedidoData.put("comuna", pedido.getComuna());
            pedidoData.put("comentarios", pedido.getComentarios());
            pedidoData.put("total", pedido.getTotal());
            pedidoData.put("estado", pedido.getEstado() != null ? pedido.getEstado().name() : "PENDIENTE");
            pedidoData.put("createdAt", pedido.getCreatedAt() != null ? 
                pedido.getCreatedAt().format(ISO_FORMATTER) : null);
            
            getFirestore().collection(COLLECTION_PEDIDOS)
                .document(String.valueOf(pedido.getId()))
                .set(pedidoData)
                .get(); // Esperar a que se complete
            
            logger.info("✅ Pedido {} sincronizado con Firestore", pedido.getId());
            
        } catch (Exception e) {
            logger.error("❌ Error sincronizando pedido {} con Firestore: {}", pedido.getId(), e.getMessage());
        }
    }
    
    @Async
    public void syncPedidoEstado(Long pedidoId, String nuevoEstado) {
        if (!isFirebaseAvailable()) {
            logger.warn("Firebase no disponible, omitiendo actualización de estado del pedido {}", pedidoId);
            return;
        }
        
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("estado", nuevoEstado);
            
            getFirestore().collection(COLLECTION_PEDIDOS)
                .document(String.valueOf(pedidoId))
                .update(updates)
                .get();
            
            logger.info("✅ Estado de pedido {} actualizado en Firestore: {}", pedidoId, nuevoEstado);
            
        } catch (Exception e) {
            logger.error("❌ Error actualizando estado de pedido {} en Firestore: {}", pedidoId, e.getMessage());
        }
    }
    
    @Async
    public void deletePedido(Long pedidoId) {
        if (!isFirebaseAvailable()) {
            logger.warn("Firebase no disponible, omitiendo eliminación del pedido {}", pedidoId);
            return;
        }
        
        try {
            getFirestore().collection(COLLECTION_PEDIDOS)
                .document(String.valueOf(pedidoId))
                .delete()
                .get();
            
            logger.info("✅ Pedido {} eliminado de Firestore", pedidoId);
            
        } catch (Exception e) {
            logger.error("❌ Error eliminando pedido {} de Firestore: {}", pedidoId, e.getMessage());
        }
    }
    
    // ==================== USUARIOS ====================
    
    @Async
    public void syncUsuario(Usuario usuario) {
        if (!isFirebaseAvailable()) {
            logger.warn("Firebase no disponible, omitiendo sincronización de usuario {}", usuario.getEmail());
            return;
        }
        
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", usuario.getEmail());
            userData.put("nombre", usuario.getNombre());
            userData.put("apellido", usuario.getApellidos()); // Nota: en la app Android es "apellido" singular
            userData.put("run", usuario.getRun());
            userData.put("direccion", usuario.getDireccion());
            userData.put("telefono", usuario.getTelefono());
            userData.put("rol", usuario.getRol() != null ? usuario.getRol().name() : "USER");
            userData.put("createdAt", usuario.getCreatedAt() != null ? 
                usuario.getCreatedAt().format(ISO_FORMATTER) : null);
            // NOTA: No sincronizamos la contraseña por seguridad
            
            getFirestore().collection(COLLECTION_USERS)
                .document(usuario.getEmail()) // Email como document ID
                .set(userData)
                .get();
            
            logger.info("✅ Usuario {} sincronizado con Firestore", usuario.getEmail());
            
        } catch (Exception e) {
            logger.error("❌ Error sincronizando usuario {} con Firestore: {}", usuario.getEmail(), e.getMessage());
        }
    }
    
    @Async
    public void updateUsuario(Usuario usuario) {
        syncUsuario(usuario); // set() hace upsert automáticamente
    }
}
```

### 4. Modificar la clase principal (Application.java)
```java
@SpringBootApplication
@EnableAsync  // <-- AGREGAR ESTA ANOTACIÓN
public class HuertoHogarApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuertoHogarApplication.class, args);
    }
}
```

### 5. Modificar PedidoService.java (o equivalente)
```java
@Service
public class PedidoService {
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private FirestoreSyncService firestoreSync; // <-- AGREGAR
    
    // En el método de crear pedido:
    @Transactional
    public Pedido crearPedido(PedidoRequest request) {
        // ... lógica existente para crear pedido ...
        Pedido savedPedido = pedidoRepository.save(pedido);
        
        // Sincronizar con Firestore (async, no bloquea)
        firestoreSync.syncPedido(savedPedido);  // <-- AGREGAR
        
        return savedPedido;
    }
    
    // En el método de actualizar estado:
    @Transactional
    public Pedido actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        
        pedido.setEstado(nuevoEstado);
        Pedido updated = pedidoRepository.save(pedido);
        
        // Sincronizar estado con Firestore
        firestoreSync.syncPedidoEstado(pedidoId, nuevoEstado.name());  // <-- AGREGAR
        
        return updated;
    }
    
    // En el método de actualizar pedido completo:
    @Transactional
    public Pedido actualizarPedido(Long pedidoId, PedidoUpdateRequest request) {
        // ... lógica existente ...
        Pedido updated = pedidoRepository.save(pedido);
        
        firestoreSync.syncPedido(updated);  // <-- AGREGAR
        
        return updated;
    }
    
    // En el método de eliminar:
    @Transactional
    public void eliminarPedido(Long pedidoId) {
        pedidoRepository.deleteById(pedidoId);
        
        firestoreSync.deletePedido(pedidoId);  // <-- AGREGAR
    }
}
```

### 6. Modificar UsuarioService.java o AuthService.java
```java
@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private FirestoreSyncService firestoreSync; // <-- AGREGAR
    
    // En el método de registro:
    @Transactional
    public Usuario registrarUsuario(RegistroRequest request) {
        // ... lógica existente de registro ...
        Usuario savedUsuario = usuarioRepository.save(usuario);
        
        // Sincronizar con Firestore
        firestoreSync.syncUsuario(savedUsuario);  // <-- AGREGAR
        
        return savedUsuario;
    }
    
    // En el método de actualizar perfil:
    @Transactional
    public Usuario actualizarPerfil(String email, PerfilUpdateRequest request) {
        // ... lógica existente ...
        Usuario updated = usuarioRepository.save(usuario);
        
        firestoreSync.updateUsuario(updated);  // <-- AGREGAR
        
        return updated;
    }
}
```

### 7. Agregar al .gitignore
```
# Firebase
firebase-service-account.json
**/firebase-service-account.json
```

## Pasos de Configuración

1. **Obtener credenciales de Firebase:**
   - Ir a Firebase Console → Tu proyecto
   - Configuración (⚙️) → Cuentas de servicio
   - Click en "Generar nueva clave privada"
   - Descargar el archivo JSON

2. **Configurar el archivo:**
   - Renombrar a `firebase-service-account.json`
   - Colocar en `src/main/resources/`

3. **Compilar y probar:**
   - Reiniciar el servidor Spring Boot
   - Crear un pedido de prueba
   - Verificar en Firebase Console → Firestore que aparece el documento

## Estructura de Colecciones en Firestore

```
firestore/
├── pedidos/
│   ├── {pedidoId}/
│   │   ├── id: number
│   │   ├── userEmail: string
│   │   ├── fechaEntrega: string
│   │   ├── direccionEntrega: string
│   │   ├── region: string
│   │   ├── comuna: string
│   │   ├── comentarios: string
│   │   ├── total: number
│   │   ├── estado: string
│   │   └── createdAt: string
│   └── ...
│
└── users/
    ├── {userEmail}/
    │   ├── email: string
    │   ├── nombre: string
    │   ├── apellido: string
    │   ├── run: string
    │   ├── direccion: string
    │   ├── telefono: string
    │   ├── rol: string
    │   └── createdAt: string
    └── ...
```

## Notas Importantes

1. **Manejo de errores:** Los métodos de sincronización capturan excepciones internamente y no afectan la operación principal. Si Firestore falla, el pedido/usuario se guarda igual en la BD local.

2. **Asincronía:** Todos los métodos de sync son `@Async`, se ejecutan en segundo plano.

3. **Seguridad:** NUNCA sincronizar contraseñas a Firestore.

4. **IDs:** 
   - Pedidos: usar el ID numérico convertido a String
   - Usuarios: usar el email como document ID
