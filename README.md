# App Verduras - Aplicación de E-Commerce

**App Verduras** es una aplicación móvil nativa para Android, desarrollada como una solución de e-commerce completa. Permite a los usuarios explorar un catálogo de productos, gestionar su carrito de compras y realizar pedidos. Además, cuenta con un robusto panel de administración para gestionar usuarios, pedidos y productos, junto con funcionalidades nativas avanzadas como un escáner de códigos QR y un gestor de documentos.

## ✨ Características Principales

La aplicación está dividida en dos grandes módulos: la vista del cliente y el panel de administración.

### Vista del Cliente

- **Autenticación de Usuarios**: Sistema completo de registro e inicio de sesión con validaciones de seguridad.
  - El correo debe pertenecer a los dominios `@gmail.com` o `@duocuc.cl`.
  - La contraseña requiere una longitud mínima de 6 caracteres y un carácter especial.
- **Catálogo de Productos**: Visualización de productos con imágenes, nombres y precios.
- **Carrito de Compras**: Los usuarios pueden añadir, modificar y eliminar productos de su carrito en tiempo real.
- **Navegación Intuitiva**: Interfaz de usuario clara y moderna construida con Jetpack Compose.
- **Escáner de Códigos QR**: Acceso rápido a productos o información a través de la cámara del dispositivo.

### Panel de Administración

- **Acceso Restringido**: Panel protegido al que solo pueden acceder los usuarios con rol de administrador.
- **Gestión de Usuarios**: Permite ver, editar y eliminar usuarios registrados en la aplicación.
- **Gestión de Pedidos**: Visualización y actualización del estado de los pedidos realizados por los clientes.
- **Gestión de Productos**: CRUD completo (Crear, Leer, Actualizar, Eliminar) para los productos del catálogo.
- **Gestión de Documentos**: Una sección para adjuntar y gestionar documentos importantes (fotos, PDFs, etc.) utilizando el selector de archivos nativo del sistema.
- **Feedback Visual**: Mensajes de confirmación (`Snackbar`) para asegurar que las operaciones se han completado con éxito.

## 🛠️ Tecnologías y Arquitectura

Esta aplicación ha sido construida siguiendo las mejores prácticas de desarrollo de Android moderno.

- **Lenguaje**: **Kotlin** 100%.
- **Interfaz de Usuario**: **Jetpack Compose**, el framework declarativo moderno para la construcción de UI nativa.
- **Arquitectura**: **MVVM (Model-View-ViewModel)**, que separa la lógica de negocio de la interfaz de usuario para un código más limpio, escalable y fácil de mantener.
- **Navegación**: **Jetpack Navigation** para gestionar los flujos de navegación dentro de la app.
- **Base de Datos**: **Room**, una capa de abstracción sobre SQLite que permite un acceso robusto a la base de datos local.
- **Asincronía**: **Kotlin Coroutines & Flow** para gestionar operaciones en segundo plano de forma eficiente.
- **Integración de Cámara**: **CameraX**, para una integración sencilla y potente con la cámara del dispositivo.
- **Machine Learning**: **Google ML Kit (Barcode Scanning)**, para la detección y análisis de códigos QR en tiempo real.
- **Gestión de Dependencias**: **Gradle** con el DSL de Kotlin (`build.gradle.kts`).

## 🚀 Cómo Empezar

1.  Clona este repositorio en tu máquina local.
2.  Abre el proyecto con una versión reciente de Android Studio.
3.  Android Studio (Gradle) se encargará de descargar todas las dependencias necesarias.
4.  Ejecuta la aplicación en un emulador o en un dispositivo físico.

