# Proyecto Verduras Móvil

Este proyecto es una aplicación móvil de catálogo y carrito de compras para una tienda de verduras y productos frescos. Permite a los usuarios explorar productos, filtrarlos por categoría, buscarlos por nombre y gestionar su carrito de compras.

---

## ¿Qué hace la aplicación?

La aplicación cumple con las siguientes funcionalidades principales:

-   **Catálogo de Productos**: Muestra una lista de productos con su imagen, nombre, descripción y precio. Los usuarios pueden navegar por el catálogo completo.
-   **Búsqueda y Filtrado**: Permite buscar productos por nombre y filtrar el catálogo por categorías (ej. "Verduras", "Frutas").
-   **Carrito de Compras**: Los usuarios pueden agregar productos al carrito, aumentar o disminuir la cantidad de cada producto, y ver el resumen de su compra.
-   **Gestión de Carrito Vacío**: Muestra una interfaz amigable cuando el carrito está vacío, invitando al usuario a seguir comprando.
-   **Persistencia de Datos**: El contenido del carrito se guarda localmente en el dispositivo, por lo que la compra no se pierde si se cierra la aplicación.

---

## ¿Cómo se ejecuta?

Sigue estos pasos para levantar y ejecutar el proyecto en tu entorno de desarrollo.

### 1. Clonar el repositorio
```
git clone https://github.com/agusnoopy3000/Proyecto_verduras_movil.git
```
### 2. Abrir en Android Studio

1.  Abre **Android Studio** (se recomienda una versión reciente).
2.  Selecciona **Open an Existing Project** y navega hasta la carpeta del proyecto.

### 3. Configurar el emulador o dispositivo

-   Asegúrate de tener un dispositivo virtual (emulador) con **API nivel 24 o superior**.
-   Alternativamente, puedes conectar un dispositivo físico con la depuración USB activada.

### 4. Ejecutar la aplicación

1.  Espera a que **Gradle** sincronice el proyecto.
2.  Presiona el botón **"Run 'app'"** (el ícono de play verde) en la barra de herramientas de Android Studio, seleccionando el dispositivo de destino.

---

## Tecnologías Utilizadas

Este proyecto fue construido utilizando un stack de tecnologías modernas para el desarrollo en Android:

-   **Lenguaje de Programación**: Kotlin
-   **Framework de UI**: Jetpack Compose para una interfaz de usuario declarativa y moderna.
-   **Arquitectura**: MVVM (Model-View-ViewModel) para separar la lógica de negocio de la interfaz de usuario.
-   **Gestión de Estado**: StateFlow y ViewModel de Jetpack para gestionar el estado de la UI de forma reactiva.
-   **Asincronía**: Kotlin Coroutines para manejar operaciones en segundo plano como peticiones de red o acceso a la base de datos.
-   **Base de Datos Local**: Room para la persistencia de datos del carrito de compras.
-   **Navegación**: Jetpack Navigation for Compose para gestionar la navegación entre las diferentes pantallas de la aplicación.
-   **Carga de Imágenes**: Coil para cargar y cachear imágenes de manera eficiente desde una URL o recursos locales.
-   **Networking (Red)**: Retrofit para realizar llamadas a una API REST (si aplica).
-   **Servicios en la Nube**:
    -   **AWS S3**: SDK de AWS para interactuar con Amazon S3 (posiblemente para la gestión de imágenes de productos).
-   **Cámara y Visión por Computadora**:
    -   **CameraX**: Para acceder a la funcionalidad de la cámara del dispositivo.
    -   **Google ML Kit**: Utilizado para el escaneo de códigos de barras.
-   **Dependencias Adicionales**:
    -   **Material 3**: Para los componentes de UI y el diseño visual.
    -   **Core KTX**: Extensiones de Kotlin para las APIs de Android.
    -   **Lifecycle KTX**: Para la gestión del ciclo de vida de los componentes.

---

## Roles del Equipo (Opcional)

-   **Agustin Garrido**: [Rol: Desarrollador Android, Líder de Proyecto]
-   **Martin Coronel**: [Rol: Desarrollador Android, Encargado de UI/UX]
