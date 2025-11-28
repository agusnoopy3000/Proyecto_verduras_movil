# 🥬 Huerto Hogar - Aplicación Móvil

Aplicación móvil Android para la venta de productos orgánicos y hortalizas frescas. Desarrollada con Kotlin y Jetpack Compose.

---

## 📋 Información del Proyecto

| Campo | Valor |
|-------|-------|
| **Nombre del Proyecto** | Huerto Hogar |
| **Tipo** | Aplicación Android Nativa |
| **Lenguaje** | Kotlin |
| **Framework UI** | Jetpack Compose + Material 3 |
| **Arquitectura** | MVVM (Model-View-ViewModel) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |

---

## 👥 Integrantes del Equipo

| Nombre | Rol |
|--------|-----|
| Agustín Garrido | Desarrollador Full Stack |

> **Última actualización:** 28 de Noviembre de 2025

---

## 🚀 Funcionalidades Principales

### Para Usuarios (Rol: USER)
- ✅ Registro e inicio de sesión con Firebase Authentication
- ✅ Autenticación Multi-Factor (MFA) con SMS
- ✅ Catálogo de productos con búsqueda y filtros por categoría
- ✅ Carrito de compras con persistencia local (Room Database)
- ✅ Proceso de checkout con opción de envío a domicilio o retiro en tienda
- ✅ Historial de pedidos realizados
- ✅ Escaneo de códigos QR para productos
- ✅ Visualización del clima actual (API externa OpenWeatherMap)
- ✅ Chat de soporte integrado
- ✅ Recuperación de contraseña por email

### Para Administradores (Rol: ADMIN)
- ✅ Panel de administración dedicado
- ✅ Gestión de productos (CRUD completo)
- ✅ Gestión de usuarios
- ✅ Gestión de pedidos con actualización de estados
- ✅ Gestión de documentos con subida a AWS S3
- ✅ Estadísticas y métricas

---

## 🌐 APIs y Endpoints

### 1. 🔧 API del Microservicio (Backend Propio - Spring Boot)

**Base URL**: `http://52.2.172.54:8080/api/v1`

#### Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/login` | Inicio de sesión con email/password |
| `POST` | `/auth/register` | Registro de nuevo usuario |
| `POST` | `/auth/firebase-sync` | Sincronización token Firebase con backend |
| `POST` | `/auth/forgot-password` | Enviar email de recuperación |

#### Productos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/productos` | Listar todos los productos |
| `GET` | `/productos/{id}` | Obtener producto por ID |
| `POST` | `/productos` | Crear nuevo producto (Admin) |
| `PUT` | `/productos/{id}` | Actualizar producto (Admin) |
| `DELETE` | `/productos/{id}` | Eliminar producto (Admin) |

#### Pedidos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/pedidos` | Listar pedidos del usuario |
| `GET` | `/pedidos/{id}` | Obtener pedido por ID |
| `POST` | `/pedidos` | Crear nuevo pedido |
| `PUT` | `/pedidos/{id}` | Actualizar estado del pedido (Admin) |

#### Usuarios (Admin)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/users` | Listar todos los usuarios |
| `GET` | `/users/{id}` | Obtener usuario por ID |
| `PUT` | `/users/{id}` | Actualizar usuario |
| `DELETE` | `/users/{id}` | Eliminar usuario |

#### Documentos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/documentos` | Listar documentos |
| `POST` | `/documentos/upload` | Subir documento a S3 |
| `DELETE` | `/documentos/{id}` | Eliminar documento |

---

### 2. 🌤️ API Externa - OpenWeatherMap

**Base URL**: `https://api.openweathermap.org/data/2.5`
**Documentación**: [https://openweathermap.org/api](https://openweathermap.org/api)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/weather?q={city}&appid={key}&units=metric&lang=es` | Clima por ciudad |
| `GET` | `/weather?lat={lat}&lon={lon}&appid={key}&units=metric&lang=es` | Clima por coordenadas |

**Uso en la app**: Mostrar información climática relevante para productos agrícolas en la pantalla principal.

---

### 3. 🔥 Firebase Services

| Servicio | Uso |
|----------|-----|
| **Firebase Authentication** | Autenticación de usuarios (email/password, MFA) |
| **Firebase Firestore** | Base de datos NoSQL para sincronización |
| **Firebase Analytics** | Métricas y análisis de uso |
| **Firebase Crashlytics** | Reportes de errores y crashes |
| **Firebase Performance** | Monitoreo de rendimiento |

---

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/app_verduras/
├── Model/                    # Modelos de datos
│   ├── User.kt
│   ├── Producto.kt
│   ├── Pedido.kt
│   └── CartItem.kt
├── api/                      # Configuración de APIs
│   ├── ApiService.kt         # Retrofit API interfaces
│   └── WeatherApiService.kt  # OpenWeatherMap API
├── auth/                     # Autenticación
│   ├── HybridAuthRepository.kt
│   └── FirebaseMFAManager.kt
├── dal/                      # Data Access Layer (Room)
│   ├── AppDatabase.kt
│   ├── UserDao.kt
│   ├── CartDao.kt
│   └── ProductoDao.kt
├── firebase/                 # Servicios Firebase
│   └── FirestoreService.kt
├── ui/                       # Interfaz de Usuario
│   ├── components/           # Componentes reutilizables
│   │   ├── EnhancedSnackbar.kt
│   │   ├── SuccessToast.kt
│   │   └── SupportChat.kt
│   ├── screens/              # Pantallas
│   │   ├── SplashScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── CatalogScreen.kt
│   │   ├── CartScreen.kt
│   │   ├── CheckoutScreen.kt
│   │   └── AdminScreen.kt
│   └── theme/                # Tema y estilos
├── util/                     # Utilidades
│   ├── SessionManager.kt
│   ├── AnalyticsManager.kt
│   └── CrashlyticsManager.kt
└── viewmodel/                # ViewModels (MVVM)
    ├── AuthViewModel.kt
    ├── ProductViewModel.kt
    ├── CartViewModel.kt
    └── PedidoViewModel.kt
```

---

## 🔐 APK Firmado y Keystore

### Ubicación del APK Firmado
```
app/build/outputs/apk/release/app-release.apk
```

### Ubicación del Keystore (.jks)
```
app/huerto-hogar.jks
```

### Información del Keystore
| Campo | Valor |
|-------|-------|
| **Nombre archivo** | `huerto-hogar.jks` |
| **Alias** | `huerto-hogar` |
| **Tipo** | JKS (Java KeyStore) |
| **Validez** | 10,000 días |

### Generar APK Firmado
```bash
# Desde la raíz del proyecto
./gradlew assembleRelease

# El APK se genera en:
# app/build/outputs/apk/release/app-release.apk
```

---

## 🛠️ Instrucciones para Ejecutar el Proyecto

### Prerrequisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17 o superior
- Android SDK 34
- Dispositivo Android o emulador (API 24+)

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/agusnoopy3000/Proyecto_verduras_movil.git
cd Proyecto_verduras_movil
```

2. **Abrir en Android Studio**
   - Abrir Android Studio
   - File → Open → Seleccionar la carpeta del proyecto
   - Esperar a que Gradle sincronice las dependencias

3. **Configurar Firebase** (⚠️ IMPORTANTE)
   - Ir a [Firebase Console](https://console.firebase.google.com)
   - Seleccionar el proyecto `huerto-hogar-cbe8d`
   - Descargar `google-services.json`
   - Colocar el archivo en la carpeta `app/`
   - **Nota**: Este archivo NO está en el repositorio por seguridad
   - Puedes usar `app/google-services.json.example` como referencia

4. **Ejecutar la aplicación**
```bash
# Desde terminal
./gradlew assembleDebug
./gradlew installDebug

# O desde Android Studio:
# Click en Run (▶️) o Shift+F10
```

5. **Generar APK de Release**
```bash
./gradlew assembleRelease
```

### Credenciales de Prueba
| Rol | Email | Password |
|-----|-------|----------|
| Usuario | `usuario@test.com` | `Test123!` |
| Admin | `admin@huertohogar.cl` | `Admin123!` |

---

## 📊 Tecnologías Utilizadas

### Frontend (Android)
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Kotlin | 1.9.x | Lenguaje principal |
| Jetpack Compose | 1.5.x | UI declarativa |
| Material 3 | 1.2.x | Componentes de diseño |
| Room | 2.6.x | Base de datos local |
| Retrofit | 2.9.x | Cliente HTTP |
| Coil | 2.5.x | Carga de imágenes |
| Lottie | 6.x | Animaciones |

### Backend (Spring Boot)
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17 | Lenguaje |
| Spring Boot | 3.x | Framework |
| Spring Security | - | Autenticación JWT |
| PostgreSQL | - | Base de datos |
| AWS S3 | - | Almacenamiento de archivos |

### Servicios en la Nube
| Servicio | Proveedor | Uso |
|----------|-----------|-----|
| Firebase Auth | Google | Autenticación |
| Firestore | Google | Base de datos NoSQL |
| EC2 | AWS | Hosting del backend |
| S3 | AWS | Almacenamiento de archivos |

---

## 🔄 Persistencia de Datos

### Local (Room Database)
- **Usuarios**: Caché del usuario logueado
- **Carrito**: Items del carrito persistentes
- **Productos**: Caché para modo offline

### Remoto
- **Firebase Firestore**: Sincronización en tiempo real
- **Backend PostgreSQL**: Datos principales (usuarios, productos, pedidos)

---

## 📄 Documentación Adicional

- [Documentación Técnica](docs/DOCUMENTACION_TECNICA.md)
- [API README](docs/API_README.md)
- [Integración Firebase](docs/BACKEND_FIREBASE_IMPLEMENTATION.md)
- [Colección Postman](docs/HuertoHogar_Postman_Collection.json)

---

## 🔗 Enlaces del Proyecto

| Recurso | URL |
|---------|-----|
| **Repositorio GitHub** | https://github.com/agusnoopy3000/Proyecto_verduras_movil |
| **Backend API** | http://52.2.172.54:8080/api/v1 |

---

**Desarrollado con ❤️ por el equipo de Huerto Hogar**
