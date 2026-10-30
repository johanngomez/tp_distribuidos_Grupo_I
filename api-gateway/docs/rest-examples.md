# API REST del API Gateway

La especificación OpenAPI se genera automáticamente y puede consultarse en:

- Swagger UI: http://127.0.0.1:8000/docs
- ReDoc: http://127.0.0.1:8000/redoc
- JSON OpenAPI: http://127.0.0.1:8000/openapi.json

Las rutas protegidas requieren Authorization: Bearer <JWT>. El token se obtiene con POST /auth/login.

## Inicio de sesión

json
POST /auth/login
{
  "email": "admin@rentar.com",
  "password": "123456"
}


Respuesta 200 OK:

json
{
  "token": "<jwt>",
  "tipo": "Bearer",
  "esAdmin": true
}


## Registrar vehículo

Solo administradores. Respuesta 201 Created.

json
POST /vehiculos
{
  "patente": "AAA999",
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2024,
  "color": "Gris",
  "tipo": "SEDAN",
  "precioDiario": 50000
}


Errores frecuentes: 400 datos inválidos, 401 token inválido, 403 falta de rol administrador, 503 Vehicle Service no disponible.

## Crear reserva

Respuesta 201 Created.

json
POST /api/reservas
{
  "clienteId": 1,
  "vehiculoId": 1,
  "fechaInicio": "2026-11-10T10:00:00",
  "fechaFin": "2026-11-15T10:00:00"
}


El Gateway valida cliente y vehículo antes de solicitar la creación al Rental Service. Puede devolver 404 si no existe el cliente o vehículo, 409 si el cliente no puede alquilar o el vehículo no está disponible, y 503 si un servicio interno no responde.

## Consultar reservas

text
GET /api/reservas?cliente_id=1&fecha_inicio=2026-11-10T00:00:00&fecha_fin=2026-11-30T00:00:00


Los administradores pueden consultar todas las reservas; los clientes solo las asociadas a su usuario.

## Cancelar reserva

text
PUT /api/reservas/18/cancelar


Respuesta 200 OK con la reserva cancelada. Si la reserva ya comenzó, se devuelve 409 Conflict.
