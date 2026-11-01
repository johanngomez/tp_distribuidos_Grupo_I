# REST: rutas y ejemplos

[Volver al índice](../README.md). Base de estas rutas: **http://localhost:8080**. Para probar con el profesor, abrir Swagger del monolito, autenticarse y reemplazar los IDs por los obtenidos en cada respuesta.

## Inventario de endpoints

| Método | Ruta | Operación | Respuestas del flujo implementado |
|---|---|---|---|
| POST | `/auth/login` | Autenticar y emitir JWT | 200 al autenticar; errores según excepciones y seguridad |
| GET | `/vehiculos` | Listar, incluidos inactivos | 200 |
| GET | `/vehiculos/{id}` | Buscar por ID | 200 / 404 |
| POST | `/vehiculos` | Crear como disponible y activo | 201 / 400 / 500 |
| PUT | `/vehiculos/{id}` | Modificar datos permitidos | 200 / 404; validación puede devolver 400 |
| DELETE | `/vehiculos/{id}` | Baja lógica | 204 / 404 |
| GET | `/api/clientes` | Listar clientes | 200 |
| GET | `/api/clientes/me` | Perfil asociado al token | 200 en el caso válido |
| GET | `/api/clientes/{id}` | Buscar cliente | 200 / 404 |
| POST | `/api/clientes` | Crear cliente | 201 / 400 |
| PUT | `/api/clientes/{id}` | Modificar cliente | 200 / 404 |
| DELETE | `/api/clientes/{id}` | Baja lógica | 204 / 404 |
| GET | `/api/reservas` | Listar reservas como entidades | 200 |
| GET | `/api/reservas/{id}` | Buscar reserva | 200 / 404 |
| POST | `/api/reservas` | Crear y calcular importe | 201 / 400 / 409 / 404 |
| PUT | `/api/reservas/{id}/cancelar` | Cancelar antes del inicio | 200 / 400 / 409 / 404 |

Las rutas protegidas pueden responder errores de autenticación antes de entrar al controller. Las tablas describen el monolito: por ejemplo, la patente duplicada se devuelve como 400 aquí, no el 409 del Gateway. Hay capturas amplias de excepciones que pueden clasificar como 404 o 500 errores de otra naturaleza.

## Vehículos

`POST /vehiculos`:

```json
{
  "patente": "DEMO101",
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2024,
  "color": "Gris",
  "tipo": "SEDAN",
  "precioDiario": 50000
}
```

Usar una patente nueva en cada ejecución. El servidor genera `id` y fija `estado=DISPONIBLE` y `activo=true`. Los tipos son `SEDAN`, `SUV`, `PICKUP`, `COUPE` y `HATCHBACK`.

Ejemplo curl completo:

```bash
curl -i -X POST http://localhost:8080/vehiculos \
  -H 'Authorization: Bearer TOKEN_JWT_RECIBIDO' \
  -H 'Content-Type: application/json' \
  -d '{"patente":"DEMO101","marca":"Toyota","modelo":"Corolla","anio":2024,"color":"Gris","tipo":"SEDAN","precioDiario":50000}'
```

`GET /vehiculos` lista registros. `GET /vehiculos/ID_VEHICULO` consulta uno: reemplazar el marcador por un número real, por ejemplo el `id` devuelto en el alta.

`PUT /vehiculos/ID_VEHICULO`:

```json
{
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2024,
  "color": "Blanco",
  "tipo": "SEDAN",
  "precioDiario": 55000,
  "estado": "DISPONIBLE"
}
```

El DTO de modificación no contiene patente. No es un PATCH parcial: completar todos los campos requeridos. Los estados de vehículos de este módulo son `DISPONIBLE`, `RESERVADO` y `EN_ALQUILER`.

`DELETE /vehiculos/ID_VEHICULO` devuelve 204 sin body y guarda `activo=false`. Hacerlo al terminar la demo de reservas, no antes.

Pruebas negativas: repetir la patente; precio cero; marca vacía; tipo fuera del enum; buscar un ID que se verificó inexistente. Observar código y body, sin extrapolar el formato del Gateway.

## Clientes

`POST /api/clientes`:

```json
{
  "documento": "45000101",
  "nombre": "Ana",
  "apellido": "Demo",
  "email": "ana.demo101@example.com",
  "telefono": "1100000101",
  "fechaNacimiento": "1995-05-20",
  "password": "ClaveDemo123456"
}
```

Documento y email deben ser nuevos. La contraseña es de demostración y se guarda como hash. El service fija actividad y rol de cliente, y la respuesta pública omite la contraseña. Las fechas de nacimiento usan `YYYY-MM-DD`.

`PUT /api/clientes/ID_CLIENTE`:

```json
{
  "nombre": "Ana",
  "apellido": "Demo",
  "telefono": "1199990101",
  "fechaNacimiento": "1995-05-20"
}
```

No incluye cambio de documento, email, contraseña ni rol. `GET /api/clientes/me` consulta al usuario del JWT; `DELETE /api/clientes/ID_CLIENTE` lo desactiva sin eliminar la fila. La baja impide un nuevo login y la creación de reservas para ese cliente según las comprobaciones del service.

## Reservas

`POST /api/reservas`:

```json
{
  "clienteId": 2,
  "vehiculoId": 1,
  "fechaInicio": "2030-11-10T10:00:00",
  "fechaFin": "2030-11-12T10:00:00"
}
```

Los IDs son ilustrativos: usar los del cliente y vehículo recién creados. Cambiar el intervalo si dejó de ser futuro o ya tiene reservas. Las fechas usan ISO-8601, sin zona, porque se reciben como `LocalDateTime`.

El precio se toma del vehículo; no se envía en el body. Si el precio es 50000 y la duración exactamente 48 horas, el total es 100000. La reserva queda `CONFIRMADA` y el vehículo pasa a `RESERVADO`.

`PUT /api/reservas/ID_RESERVA/cancelar` no requiere body. Cancela si aún no comenzó. No hay un endpoint público de edición general de reserva en este controller.

Pruebas negativas: fechas pasadas, fin anterior o igual al inicio, cliente inexistente/inactivo, vehículo inexistente/inactivo y segunda reserva superpuesta. No asumir que la simple declaración `@Valid` impide todos los null: `ReservaCreateDTO` no declara restricciones de campo y algunas entradas inválidas pueden terminar en una captura genérica.

Para consultar junto con el profesor, preferir los [ejemplos GraphQL](graphql.md), que seleccionan explícitamente campos públicos. Los GET REST de reservas serializan entidades relacionadas, con el riesgo de exposición señalado en [arquitectura](arquitectura.md).
