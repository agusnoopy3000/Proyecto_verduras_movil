# Reglas de Firestore para Huerto Hogar

## Reglas para Desarrollo (Temporales)

Usar estas reglas solo durante desarrollo para permitir lectura/escritura sin restricciones:

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

## Reglas para Producción (Recomendadas)

Usar estas reglas cuando la app esté en producción:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // =====================
    // COLECCIÓN: pedidos
    // =====================
    match /pedidos/{pedidoId} {
      // Permitir lectura si:
      // - El usuario está autenticado Y
      // - Es el dueño del pedido O es admin
      allow read: if request.auth != null && (
        resource.data.userEmail == request.auth.token.email ||
        request.auth.token.rol == 'ADMIN'
      );
      
      // Permitir escritura solo desde el backend (service account)
      // o si el usuario es admin
      allow write: if request.auth != null && (
        request.auth.token.rol == 'ADMIN' ||
        request.auth.uid == 'service-account'
      );
      
      // Alternativamente, permitir escritura a usuarios autenticados
      // (más permisivo, útil si la app escribe directamente)
      // allow write: if request.auth != null;
    }
    
    // =====================
    // COLECCIÓN: users
    // =====================
    match /users/{userEmail} {
      // Permitir lectura si:
      // - Es el propio usuario O
      // - Es admin
      allow read: if request.auth != null && (
        request.auth.token.email == userEmail ||
        request.auth.token.rol == 'ADMIN'
      );
      
      // Permitir escritura solo desde el backend o para el propio usuario
      allow write: if request.auth != null && (
        request.auth.token.email == userEmail ||
        request.auth.token.rol == 'ADMIN'
      );
    }
    
    // =====================
    // COLECCIÓN: productos (si la agregas en el futuro)
    // =====================
    match /productos/{productoId} {
      // Cualquier usuario autenticado puede leer productos
      allow read: if request.auth != null;
      
      // Solo admin puede escribir productos
      allow write: if request.auth != null && request.auth.token.rol == 'ADMIN';
    }
  }
}
```

## Cómo Aplicar las Reglas

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto "Huerto Hogar"
3. En el menú lateral, haz clic en **Firestore Database**
4. Haz clic en la pestaña **Rules**
5. Reemplaza las reglas existentes con las deseadas
6. Haz clic en **Publish**

## Notas Importantes

- Las reglas de desarrollo (`allow read, write: if true`) son **inseguras** y solo deben usarse en desarrollo.
- En producción, siempre valida la autenticación y autorización.
- El backend de Spring Boot usa un Service Account que tiene permisos completos.
- La app Android puede escribir si el usuario está autenticado con Firebase Auth.
