# Hito 3: RabbitMQ y API del Gateway

Este documento complementa la documentación interactiva de la API:

- Swagger/OpenAPI: http://127.0.0.1:8000/docs
- ReDoc: http://127.0.0.1:8000/redoc
- GraphQL/GraphiQL: http://127.0.0.1:8000/graphql

El frontend se comunica exclusivamente con el API Gateway mediante REST y
GraphQL. RabbitMQ se utiliza internamente para procesos que no necesitan
bloquear la respuesta principal.

## RabbitMQ

RabbitMQ se ejecuta con Docker Compose:

powershell
cd E:\TpDistribuidos
docker compose up -d rabbitmq
docker compose ps


La consola está disponible en http://localhost:15672, con usuario rentar y
contraseña rentar. El exchange utilizado es rentar.events, de tipo topic.

## Endpoint REST de vehículos externos

POST /vehiculos/externos registra un vehículo propiedad de un cliente y
dispara las validaciones asíncronas. Requiere un JWT válido. Un cliente puede
omitir clienteId, porque el Gateway lo obtiene del token; un administrador
debe informarlo.

Ejemplo para un cliente:

json
{
  "patente": "EXT123",
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2022,
  "color": "Blanco",
  "tipo": "SEDAN",
  "precioDiario": 50000
}


La respuesta 201 Created representa el registro inicial. El vehículo comienza
en estado PENDIENTE y luego pasa a DISPONIBLE solamente si las dos
validaciones finalizan como VALIDADO.

## Flujos asíncronos

### Verificación de vehículos

Al crear un vehículo normal se publica vehiculos.verificacion.solicitada.
Vehicle Service consulta el registro policial simulado y publica
vehiculos.verificacion.resultado. Los resultados son VALIDADO o ROBADO.
Un vehículo robado se da de baja lógicamente (activo = false) y ROBADO no
se agrega como estado operativo.

### Incorporación de vehículos externos

El registro externo publica vehiculos.externos.registrados. Dos consumidores
independientes reciben una copia del evento:

1. Vehicle Service valida la patente.
2. Customer Service valida la titularidad.
3. Vehicle Service correlaciona ambos resultados mediante correlationId.
4. Se publica vehiculos.externos.validacion.completada.
5. El vehículo queda disponible si ambas validaciones son satisfactorias; de lo
   contrario queda inhabilitado.

### Mantenimiento preventivo

Al confirmar una reserva se publica reserva.confirmada. El componente de
mantenimiento consume el evento, genera una solicitud y publica
mantenimiento.resultado con REALIZADO o NO_REALIZADO. La reserva no espera
este procesamiento para ser confirmada.

## Envoltorio de eventos

Los eventos utilizan un envoltorio común:

json
{
  "eventId": "uuid",
  "eventType": "reserva.confirmada",
  "correlationId": "uuid-del-proceso",
  "occurredAt": "2026-10-30T18:00:00Z",
  "payload": {}
}


eventId identifica el mensaje, correlationId relaciona las validaciones de
un mismo vehículo y occurredAt utiliza ISO-8601 UTC.

## Colas y consumidores

| Cola | Evento | Consumidor |
|---|---|---|
| rentar.vehicle.verification | vehiculos.verificacion.solicitada | Vehicle Service |
| rentar.vehicle.verification.result | vehiculos.verificacion.resultado | Vehicle Service |
| rentar.external.vehicle.patent.request | vehiculos.externos.registrados | Validador de patente |
| rentar.external.vehicle.ownership.request | vehiculos.externos.registrados | Validador de titularidad |
| rentar.external.vehicle.patent | vehiculos.externos.validacion.patente | Agregador |
| rentar.external.vehicle.ownership | vehiculos.externos.validacion.titularidad | Agregador |
| rentar.external.vehicle.completed | vehiculos.externos.validacion.completada | Vehicle Service |
| rentar.maintenance.request | reserva.confirmada | Mantenimiento |
| rentar.maintenance.result | mantenimiento.resultado | Rental Service |

## Pruebas y evidencias

Para documentar una prueba de vehículo externo se deben conservar:

1. Captura de Swagger con POST /vehiculos/externos y la respuesta 201.
2. Logs del validador de patente y del validador de titularidad.
3. Captura de RabbitMQ mostrando el exchange y las colas.
4. Consulta final de la base:

sql
SELECT id, patente, estado, activo
FROM rentar_vehiculos.vehiculo
WHERE patente = 'EXT123';


Para mantenimiento:

sql
SELECT id, estado, resultado_mantenimiento
FROM rentar_reservas.reserva
WHERE id = 33;


Los logs esperados incluyen REALIZADO y la base debe almacenar el mismo
resultado.
