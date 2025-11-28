# 🥬 Huerto Hogar - Aplicación Móvil

Aplicación móvil Android para la venta de productos orgánicos y hortalizas frescas. Desarrollada con Kotlin y Jetpack Compose.

---

## 📋 Información del Proyecto

| Campo | Valor |
|-------|-------|
| **Nombre del Proyecto** | Huerto Hogar |
| **Tipo** | Aplicación Android Nativa |
| **Lenguaje** | Kotlin |
| **Framework UI** | Jetpack Compose |
| **Arquitectura** | MVVM (Model-View-ViewModel) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |

---

## 👥 Integrantes del Equipo

| Nombre | Rol | Responsabilidades |
|--------|-----|-------------------|
| [Nombre Integrante 1] | Desarrollador Principal | Backend, APIs, Autenticación |
| [Nombre Integrante 2] | Desarrollador Frontend | UI/UX, Compose, Animaciones |
| [Nombre Integrante 3] | QA / Testing | Pruebas unitarias, Documentación |

---

## 🚀 Funcionalidades Principales

### Para Usuarios (Rol: USER)
- ✅ Registro e inicio de sesión con Firebase Authentication
- ✅ Catálogo de productos con búsqueda y filtros por categoría
- ✅ Carrito de compras con persistencia local
- ✅ Proceso de checkout con opción de envío a domicilio o retiro en tienda
- ✅ Historial de pedidos realizados
- ✅ Escaneo de códigos QR para productos
- ✅ Visualización del clima actual (API externa)

### Para Administradores (Rol: ADMIN)
- ✅ Panel de administración dedicado
- ✅ Gestión de productos (CRUD)
- ✅ Gestión de usuarios
- ✅ Gestión de pedidos con actualización de estados
- ✅ Gestión de documentos con subida a S3

---

## 🌐 APIs Utilizadas

### 1. API del Microservicio (Backend Propio)
- **Base URL**: `http://52.2.172.54:8080/api/v1`
- **Descripción**: Backend Spring Boot propio para gestión de usuarios, productos y pedidos

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/auth/login` | POST | Inicio de sesión |
| `/auth/register` | POST | Registro de usuario |
| `/auth/firebase-sync` | POST | Sincronización con Firebase |
| `/productos` | GET | Listar productos |
| `/productos/{id}` | GET | Obtener producto por ID |
| `/pedidos` | GET/POST | Gestión de pedidos |
| `/pedidos/{id}` | PUT | Actualizar pedido |
| `/users` | GET | Listar usuarios (Admin) |

### 2. API Externa (OpenWeatherMap)
- **Base URL**: `https://api.openweathermap.org/`
- **Documentación**: [OpenWeatherMap API](https://openweathermap.org/api)
- **Propósito**: Mostrar información climática relevante para productos agrícolas

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/data/2.5/weather?lat={lat}&lon={lon}` | GET | Clima por coordenadas |
| `/data/2.5/weather?q={city}` | GET | Clima por ciudad |

**Parámetros utilizados:**
- `appid`: API Key de OpenWeatherMap
- `units`: metric (para grados Celsius)
- `lang`: es (para descripciones en español)

---

## 🔐 Sistema de Autenticación

La aplicación implementa un sistema de **autenticación híbrida**:

1. **Firebase Authentication**: Maneja el registro e inicio de sesión de usuarios
2. **Backend JWT**: El backend genera su propio JWT después de validar el token de Firebase

### Flujo de Autenticación:
```
Usuario → Firebase Auth → Firebase ID Token → Backend /auth/firebase-sync → JWT del Backend → Uso en API calls
```

---

## 📱 Recursos Nativos Utilizados

| Recurso | Uso | Permisos |
|---------|-----|----------|
| **Cámara** | Escaneo de códigos QR | `android.permission.CAMERA` |
| **Ubicación** | Cálculo de envío, clima local | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` |
| **Almacenamiento** | Caché de imágenes, documentos | Scoped Storage |

---

## 🎨 Animaciones y Diseño Visual

### Sistema de Colores Personalizados
La aplicación utiliza una paleta de colores inspirada en la naturaleza:

| Color | Hex | Uso |
|-------|-----|-----|
| Verde Bosque | `#2E7D32` | Color primario |
| Naranja Ámbar | `#FF8F00` | Color secundario |
| Verde Claro | `#E8F5E9` | Fondos/Acentos |

### Animaciones Lottie
- `inicio_aplicacion_interactive.json` - Splash Screen
- `login_interactive.json` - Pantalla de Login
- `footer_app_movil.json` - Footer animado
- `empty_cart.json` - Carrito vacío
- `orange_skating.json` - Procesamiento de pedido
- `checkout_success.json` - Éxito en compra
- `confetti.json` - Celebración de confirmación
- `saving_cloud.json` - Subida de documentos
- `admin.json` - Panel de administración

### Efectos Visuales
- ✨ Animaciones de escala al presionar cards
- 🌈 Gradientes verticales en fondos
- 💫 Transiciones suaves entre estados
- 🎯 Feedback visual en botones con elevación

---

## 🧪 Pruebas Unitarias

### Archivos de Test
```
app/src/test/java/com/example/app_verduras/
├── model/
│   ├── UserTest.kt
│   ├── ProductoTest.kt
│   └── PedidoTest.kt
├── viewmodel/
│   ├── CartViewModelTest.kt
│   ├── WeatherViewModelTest.kt
│   ├── PedidoViewModelTest.kt
│   └── CatalogViewModelTest.kt
├── api/
│   └── WeatherApiModelsTest.kt
├── util/
│   └── SessionManagerTest.kt
└── validation/
    └── ValidationTest.kt
```

### Ejecutar Tests
```bash
./gradlew compileDebugUnitTestKotlin
```

### Cobertura
- Modelos de datos: 100%
- ViewModels (estados): 80%+
- Validaciones: 100%
- API Models: 90%+

---

## 🛠️ Configuración del Proyecto

### Requisitos Previos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17 o superior
- Android SDK 34
- Gradle 8.x

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/[tu-usuario]/Proyecto_verduras_movil.git
cd Proyecto_verduras_movil
```

2. **Configurar Firebase**
   - El archivo `google-services.json` ya está incluido
   - Proyecto Firebase: `huerto-hogar-cbe8d`

3. **Sincronizar Gradle**
```bash
./gradlew build
```

4. **Ejecutar en emulador o dispositivo**
```bash
./gradlew installDebug
```

---

## 📦 Generar APK Firmado

### Crear Keystore (.jks)
```bash
keytool -genkey -v -keystore huerto-hogar.jks -keyalg RSA -keysize 2048 -validity 10000 -alias huerto-hogar
```

### Configurar firma en `build.gradle.kts`
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("huerto-hogar.jks")
            storePassword = "tu_contraseña"
            keyAlias = "huerto-hogar"
            keyPassword = "tu_contraseña"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Generar APK
```bash
./gradlew assembleRelease
```

El APK firmado estará en: `app/build/outputs/apk/release/app-release.apk`

---

## 📂 Estructura del Proyecto

```
app/src/main/java/com/example/app_verduras/
├── api/
│   ├── ApiService.kt              # Endpoints del backend
│   ├── RetrofitClient.kt          # Cliente HTTP con JWT
│   ├── models/
│   │   ├── AuthModels.kt          # Modelos de autenticación
│   │   ├── ProductModels.kt       # Modelos de productos
│   │   └── OrderModels.kt         # Modelos de pedidos
│   └── external/
│       └── WeatherApiService.kt   # API externa del clima
├── auth/
│   ├── FirebaseAuthManager.kt     # Gestor de Firebase Auth
│   └── HybridAuthRepository.kt    # Repositorio de auth híbrida
├── dal/
│   ├── AppDatabase.kt             # Base de datos Room
│   ├── UserDao.kt
│   ├── ProductoDao.kt
│   └── PedidoDao.kt
├── Model/
│   ├── User.kt
│   ├── Producto.kt
│   ├── Pedido.kt
│   └── Documento.kt
├── repository/
│   ├── UserRepository.kt
│   ├── ProductoRepository.kt
│   └── PedidoRepository.kt
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── CartViewModel.kt
│   ├── WeatherViewModel.kt
│   └── [otros ViewModels]
├── ui/
│   ├── screens/
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── CatalogScreen.kt
│   │   ├── CartScreen.kt
│   │   └── [otras pantallas]
│   └── components/
│       └── WeatherWidget.kt
└── util/
    ├── SessionManager.kt
    └── TokenManager.kt
```

---

## 🔧 Tecnologías y Librerías

| Categoría | Tecnología |
|-----------|------------|
| **UI** | Jetpack Compose, Material 3 |
| **Navegación** | Navigation Compose |
| **Networking** | Retrofit 2, OkHttp |
| **Base de Datos** | Room Database |
| **Autenticación** | Firebase Auth |
| **Imágenes** | Coil |
| **Animaciones** | Lottie |
| **Cámara** | CameraX, ML Kit |
| **Ubicación** | Google Play Services Location |
| **Cloud Storage** | AWS S3 SDK |
| **Coroutines** | Kotlin Coroutines, Flow |

---

## 📸 Capturas de Pantalla

| Login | Home | Catálogo |
|-------|------|----------|
| [Captura] | [Captura] | [Captura] |

| Carrito | Pedido | Admin Panel |
|---------|--------|-------------|
| [Captura] | [Captura] | [Captura] |

---

## 📝 Licencia

Este proyecto fue desarrollado con fines académicos.

---

## 🤝 Contribuciones

Para contribuir al proyecto:
1. Fork del repositorio
2. Crear branch de feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit de cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push al branch (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

---

**Desarrollado con ❤️ para el curso de Desarrollo Móvil**
