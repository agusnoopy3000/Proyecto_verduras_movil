# Integración Spring Boot + Firebase Firestore

## Arquitectura de Sincronización Bidireccional

```
┌─────────────────────────────────────────────────────────────────┐
│                        APP ANDROID                               │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐ │
│  │   Room DB   │◄───│ Repositorios│───►│ FirestoreService.kt │ │
│  │   (Local)   │    │             │    │   (Firebase SDK)    │ │
│  └─────────────┘    └─────────────┘    └──────────┬──────────┘ │
└──────────────────────────────────────────────────┼──────────────┘
                                                   │
                    ┌──────────────────────────────▼──────────────┐
                    │              FIREBASE FIRESTORE              │
                    │  ┌─────────────┐    ┌─────────────────────┐ │
                    │  │   pedidos   │    │        users        │ │
                    │  │ (colección) │    │    (colección)      │ │
                    │  └─────────────┘    └─────────────────────┘ │
                    └──────────────────────────────▲──────────────┘
                                                   │
┌──────────────────────────────────────────────────┼──────────────┐
│                     SPRING BOOT BACKEND                          │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐ │
│  │ PostgreSQL/ │◄───│  Services   │───►│FirestoreSyncService │ │
│  │   MySQL     │    │             │    │   (Admin SDK)       │ │
│  └─────────────┘    └─────────────┘    └─────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Flujo de Datos

### Creación de Pedido (Cliente)
1. Cliente hace pedido desde la App
2. App envía request a Spring Boot API
3. Spring Boot guarda en PostgreSQL/MySQL
4. Spring Boot sincroniza a Firestore (async)
5. Admin recibe actualización en tiempo real en su app

### Actualización de Estado (Admin)
1. Admin cambia estado en su app
2. App puede:
   - a) Llamar a Spring Boot API → actualiza BD → sincroniza Firestore
   - b) Actualizar Firestore directamente (si se permite en reglas)
3. Otros admins ven el cambio en tiempo real

### Registro de Usuario
1. Usuario se registra (Firebase Auth + Spring Boot)
2. Spring Boot guarda en BD local
3. Spring Boot sincroniza a Firestore
4. Admin puede ver todos los usuarios en tiempo real

---

## Archivos a Crear en Spring Boot

### 1. Dependencia (pom.xml)
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

### 2. FirebaseConfig.java
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
                logger.info("✅ Firebase inicializado correctamente");
            }
        } catch (IOException e) {
            logger.error("❌ Error al inicializar Firebase: {}", e.getMessage());
        }
    }
}
```

### 3. FirestoreSyncService.java
```java
package com.huertohogar.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirestoreSyncService.class);
    private static final String COLLECTION_PEDIDOS = "pedidos";
    private static final String COLLECTION_USERS = "users";
    
    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
    
    @Async
    public void syncPedido(Pedido pedido) {
        try {
            Map<String, Object> pedidoData = new HashMap<>();
            pedidoData.put("id", pedido.getId());
            pedidoData.put("userEmail", pedido.getUsuario().getEmail());
            pedidoData.put("fechaEntrega", pedido.getFechaEntrega() != null ? 
                pedido.getFechaEntrega().toString() : null);
            pedidoData.put("direccionEntrega", pedido.getDireccionEntrega());
            pedidoData.put("region", pedido.getRegion());
            pedidoData.put("comuna", pedido.getComuna());
            pedidoData.put("comentarios", pedido.getComentarios());
            pedidoData.put("total", pedido.getTotal());
            pedidoData.put("estado", pedido.getEstado().name());
            pedidoData.put("createdAt", pedido.getCreatedAt() != null ? 
                pedido.getCreatedAt().toString() : null);
            
            getFirestore().collection(COLLECTION_PEDIDOS)
                .document(String.valueOf(pedido.getId()))
                .set(pedidoData)
                .get();
            
            logger.info("✅ Pedido {} sincronizado", pedido.getId());
        } catch (Exception e) {
            logger.error("❌ Error sincronizando pedido: {}", e.getMessage());
        }
    }
    
    @Async
    public void syncPedidoEstado(Long pedidoId, String nuevoEstado) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("estado", nuevoEstado);
            
            getFirestore().collection(COLLECTION_PEDIDOS)
                .document(String.valueOf(pedidoId))
                .update(updates)
                .get();
            
            logger.info("✅ Estado de pedido {} actualizado: {}", pedidoId, nuevoEstado);
        } catch (Exception e) {
            logger.error("❌ Error actualizando estado: {}", e.getMessage());
        }
    }
    
    @Async
    public void syncUsuario(Usuario usuario) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", usuario.getEmail());
            userData.put("nombre", usuario.getNombre());
            userData.put("apellido", usuario.getApellidos());
            userData.put("run", usuario.getRun());
            userData.put("direccion", usuario.getDireccion());
            userData.put("telefono", usuario.getTelefono());
            userData.put("rol", usuario.getRol().name());
            userData.put("createdAt", usuario.getCreatedAt() != null ? 
                usuario.getCreatedAt().toString() : null);
            
            getFirestore().collection(COLLECTION_USERS)
                .document(usuario.getEmail())
                .set(userData)
                .get();
            
            logger.info("✅ Usuario {} sincronizado", usuario.getEmail());
        } catch (Exception e) {
            logger.error("❌ Error sincronizando usuario: {}", e.getMessage());
        }
    }
    
    @Async
    public void deletePedido(Long pedidoId) {
        try {
            getFirestore().collection(COLLECTION_PEDIDOS)
                .document(String.valueOf(pedidoId))
                .delete()
                .get();
            logger.info("✅ Pedido {} eliminado de Firestore", pedidoId);
        } catch (Exception e) {
            logger.error("❌ Error eliminando pedido: {}", e.getMessage());
        }
    }
}
```

### 4. Modificar Services existentes

En cada servicio que maneje pedidos o usuarios, inyectar `FirestoreSyncService`:

```java
@Autowired
private FirestoreSyncService firestoreSync;
```

Y llamar después de cada operación CRUD:
- `firestoreSync.syncPedido(pedido)` - Al crear/actualizar
- `firestoreSync.syncPedidoEstado(id, estado)` - Al cambiar estado
- `firestoreSync.deletePedido(id)` - Al eliminar
- `firestoreSync.syncUsuario(usuario)` - Al registrar/actualizar usuario

### 5. Habilitar Async

En la clase principal:
```java
@SpringBootApplication
@EnableAsync
public class Application { ... }
```

---

## Configuración de Firebase

### Obtener Service Account Key

1. Firebase Console → Configuración (⚙️) → Cuentas de servicio
2. "Generar nueva clave privada"
3. Descargar JSON
4. Renombrar a `firebase-service-account.json`
5. Colocar en `src/main/resources/`
6. Agregar a `.gitignore`

### Reglas de Firestore (Desarrollo)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

---

## App Android (Ya implementado)

Los siguientes archivos ya están configurados:

- `FirestoreService.kt` - Servicio de Firebase
- `PedidoRepository.kt` - Sincronización de pedidos
- `UserRepository.kt` - Sincronización de usuarios
- `OrderManagementViewModel.kt` - Vista de pedidos en tiempo real
- `UserManagementViewModel.kt` - Vista de usuarios en tiempo real

---

## Prueba de Integración

1. Configurar reglas de Firestore (permisivas para desarrollo)
2. Iniciar Spring Boot con las modificaciones
3. Crear un pedido desde la app
4. Verificar en Firebase Console que aparece en Firestore
5. Abrir la app como admin y verificar que ve el pedido en tiempo real

---

## Troubleshooting

### Error: "PERMISSION_DENIED"
- Verificar reglas de Firestore
- Verificar que el service account tenga permisos

### Error: "Firebase not initialized"
- Verificar que el archivo JSON está en resources
- Verificar que el nombre es correcto

### Datos no se sincronizan
- Verificar que @EnableAsync está habilitado
- Verificar logs del backend para errores
