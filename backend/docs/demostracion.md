# Recorrido para mostrar el Hito 1

[Volver al índice](../README.md). Este recorrido crea datos de demostración mediante REST y consulta mediante GraphQL; no recarga SQL ni elimina tablas.

## Antes de presentar

Iniciar el backend según su README, preferiblemente con una base de demo separada. Verificar en el log la base/puertos seleccionados. Abrir Swagger en 8080 y GraphiQL en `/graphiql`. Los IDs del documento son ejemplos: anotar los que devuelve cada alta.

Preparar patente, documento y email que no existan. Usar fechas futuras para las reservas. No mostrar en pantalla tokens completos, claves reales ni serializaciones de entidades que expongan hashes de contraseñas.

## Secuencia

| Paso | Qué ejecutar | Qué observar y explicar |
|---|---|---|
| 1 | `POST /auth/login` con administrador de demo | JWT, tipo Bearer y rol. Autorizar Swagger y configurar headers GraphQL. |
| 2 | `GET /api/clientes/me` | Identidad obtenida del token. |
| 3 | `POST /api/clientes` con el ejemplo de REST | 201, ID generado y ausencia de contraseña en ese DTO de respuesta. Guardar ID. |
| 4 | `POST /vehiculos` con `DEMO101` o patente nueva | 201, estado disponible y actividad. Guardar ID. |
| 5 | Repetir el alta de esa patente | 400 del monolito y mensaje de duplicado. No es el 409 del Gateway. |
| 6 | `PUT /vehiculos/{id}` con el body completo | Color/precio actualizado; el DTO no incluye patente. |
| 7 | Query `Disponibilidad` para el intervalo futuro | El vehículo puede aparecer si no tiene reservas superpuestas. |
| 8 | `POST /api/reservas` usando los IDs reales | 201 y cálculo del importe con el precio actual. |
| 9 | Query `Reservas` o `ReservasFiltradas` | Selección de campos y composición de cliente/vehículo. |
| 10 | Repetir reserva para el mismo vehículo e intervalo | 409 por superposición. |
| 11 | `PUT /api/reservas/{id}/cancelar` | Estado cancelado y conservación de la reserva. |
| 12 | Query `Historial` para el cliente creado | Vehículo, patente, intervalo, días, importe y estado cancelado. |
| 13 | `DELETE /vehiculos/{id}` y luego `GET /vehiculos/{id}` | 204 al dar de baja y entidad conservada con `activo=false`. |

No presentar como exitosamente verificada la liberación de disponibilidad después de cancelar: la consulta del monolito aún toma canceladas como ocupación. Está explicado en [arquitectura](arquitectura.md). Puede mostrarse como una diferencia pendiente respecto de la regla de negocio esperada.

## Explicación breve durante la prueba

“La petición REST entra al controller y se convierte a un DTO. El service aplica las reglas, el repository usa JPA y Hibernate persiste en MySQL. Para consultar con GraphQL usamos el contrato `schema.graphqls` y resolvers `@QueryMapping`; el consumidor elige los campos de la respuesta. El JWT identifica al usuario y las contraseñas se verifican con BCrypt. Este es el monolito del Hito 1; el Gateway de los hitos posteriores está en otro módulo y no participa de este recorrido.”

## Qué cubre esta documentación

Las rutas y ejemplos se revisaron contra el código de controllers, DTO, services y esquema. Las consultas se pueden validar estáticamente contra el SDL. La ejecución requiere un backend iniciado, una base compatible y datos reales para los IDs utilizados. No representa un informe de pruebas de integración ya ejecutadas ni certifica todas las reglas de la consigna.
