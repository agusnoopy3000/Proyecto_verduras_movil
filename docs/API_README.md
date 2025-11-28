# 🌱 Huerto Hogar API - Documentación de Pruebas

## 📍 URLs Importantes

| Recurso | URL |
|---------|-----|
| **API Base** | `http://52.2.172.54:8080/api/v1/` |
| **Swagger UI** | `http://52.2.172.54:8080/swagger-ui/index.html` |

---

## 👤 Usuarios de Prueba

| Rol | Email | Password | Estado |
|-----|-------|----------|--------|
| **USER** | `prueba@test.cl` | `Test123!` | ✅ VERIFICADO |

> **Nota:** Si necesitas más usuarios, usa el endpoint `/auth/register` para crearlos.

### Registrar nuevo usuario:
```bash
curl -X POST http://52.2.172.54:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "run": "98765432-1",
    "nombre": "Admin",
    "apellidos": "Sistema",
    "email": "admin@huertohogar.cl",
    "password": "Admin123!",
    "direccion": "Oficina Central",
    "telefono": "+56900000000"
  }'
```

---

## 🔐 Autenticación

### Login (POST)
```bash
curl -X POST http://52.2.172.54:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "cliente@huertohogar.cl",
    "password": "Cliente123!"
  }'
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "email": "cliente@huertohogar.cl",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "CLIENTE"
  }
}
```

### Registro (POST)
```bash
curl -X POST http://52.2.172.54:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "run": "12345678-9",
    "nombre": "María",
    "apellidos": "González López",
    "email": "nuevo@test.cl",
    "password": "Password123!",
    "direccion": "Av. Principal 123",
    "telefono": "+56912345678"
  }'
```

---

## 📦 Productos

### Listar Productos (GET)
```bash
curl -X GET http://52.2.172.54:8080/api/v1/products \
  -H "Authorization: Bearer TU_TOKEN"
```

### Buscar Productos (GET)
```bash
curl -X GET "http://52.2.172.54:8080/api/v1/products?q=tomate" \
  -H "Authorization: Bearer TU_TOKEN"
```

### Producto por Código QR (GET)
```bash
curl -X GET http://52.2.172.54:8080/api/v1/products/codigo/VER-001 \
  -H "Authorization: Bearer TU_TOKEN"
```

---

## 🛒 Pedidos

### Crear Pedido (POST)
```bash
curl -X POST http://52.2.172.54:8080/api/v1/orders \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productoId": "prod-001",
        "cantidad": 2,
        "precioUnitario": 2500
      }
    ],
    "direccionEnvio": "Mi dirección",
    "metodoPago": "TRANSFERENCIA",
    "notas": "Sin notas"
  }'
```

### Mis Pedidos (GET)
```bash
curl -X GET http://52.2.172.54:8080/api/v1/orders \
  -H "Authorization: Bearer TU_TOKEN"
```

---

## 👤 Usuario

### Mi Perfil (GET)
```bash
curl -X GET http://52.2.172.54:8080/api/v1/users/me \
  -H "Authorization: Bearer TU_TOKEN"
```

---

## 📊 Estados de Pedido

| Estado | Descripción |
|--------|-------------|
| `PENDIENTE` | Pedido creado, esperando confirmación |
| `CONFIRMADO` | Pedido confirmado, en preparación |
| `EN_CAMINO` | Pedido en camino al cliente |
| `ENTREGADO` | Pedido entregado exitosamente |
| `CANCELADO` | Pedido cancelado |

---

## 🔑 Cómo usar el Token JWT

1. Hacer login para obtener el token
2. Copiar el valor de `token` de la respuesta
3. Agregar header en cada request:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```

---

## 📁 Archivos de esta carpeta

| Archivo | Descripción |
|---------|-------------|
| `API_Tests_HuertoHogar.json` | Documentación completa con ejemplos |
| `HuertoHogar_Postman_Collection.json` | Importar en Postman |
| `API_README.md` | Este archivo |

---

## ⚡ Prueba Rápida

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://52.2.172.54:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@huertohogar.cl","password":"Cliente123!"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"

# 2. Obtener productos
curl -X GET http://52.2.172.54:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN"
```
