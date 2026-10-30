# Hito 3 — RabbitMQ y contratos de eventos

El Hito 3 incorpora comunicación asíncrona mediante **RabbitMQ**. Se
mantienen REST, GraphQL y gRPC del Hito 2: gRPC continúa utilizándose cuando
una operación necesita una respuesta inmediata y RabbitMQ se utiliza para
procesos que pueden continuar en segundo plano.

## Ejecución local

Desde la raíz del proyecto:

powershell
docker compose up -d rabbitmq
docker compose ps


RabbitMQ queda disponible en:

- AMQP: localhost:5672
- Consola de administración: <http://localhost:15672>
- Usuario: rentar
- Contraseña: rentar

Para detenerlo:

powershell
docker compose down


Para eliminar también los mensajes persistidos durante el desarrollo:

powershell
docker compose down -v


## Topología inicial

Se utiliza un exchange de tipo topic:

text
Exchange: rentar.events


Las aplicaciones publican en el exchange usando las claves de enrutamiento
indicadas en la tabla. Cada proceso consumidor posee su propia cola para que
los consumidores independientes reciban una copia del evento.

| Cola | Routing key | Consumidor | Propósito |
|---|---|---|---|
| rentar.vehicle.verification | vehiculos.verificacion.solicitada | Vehicle Service | Consultar el registro policial simulado |
| rentar.vehicle.verification.result | vehiculos.verificacion.resultado | Vehicle Service | Aplicar el resultado al vehículo |
| rentar.external.vehicle.patent.request | vehiculos.externos.registrados | Validador de patente | Iniciar la validación de patente |
| rentar.external.vehicle.ownership.request | vehiculos.externos.registrados | Validador de titularidad | Iniciar la validación de titularidad |
| rentar.external.vehicle.patent | vehiculos.externos.validacion.patente | Correlacionador de incorporación | Recibir el resultado de patente |
| rentar.external.vehicle.ownership | vehiculos.externos.validacion.titularidad | Correlacionador de incorporación | Recibir el resultado de titularidad |
| rentar.external.vehicle.completed | vehiculos.externos.validacion.completada | Vehicle Service | Completar o rechazar la incorporación |
| rentar.maintenance.request | reserva.confirmada | Componente de mantenimiento | Crear y procesar mantenimiento preventivo |
| rentar.maintenance.result | mantenimiento.resultado | Rental Service | Asociar el resultado con la reserva |

Las dos colas de solicitud están vinculadas a la misma clave para que ambos
validadores reciban una copia del evento de registro. Si ambos consumidores
usaran una sola cola, RabbitMQ repartiría los mensajes entre ellos y no se
ejecutarían las dos validaciones para cada vehículo.

La topología puede ajustarse durante la implementación, pero los nombres de
los eventos y sus campos deben mantenerse compatibles entre productores y
consumidores.

## Envoltorio común de los eventos

Todos los mensajes deben utilizar un envoltorio común:

json
{
  "eventId": "uuid-del-evento",
  "eventType": "reserva.confirmada",
  "correlationId": "uuid-del-proceso",
  "occurredAt": "2026-10-23T18:00:00Z",
  "payload": {}
}


- eventId: identifica de forma única el mensaje y permite evitar
  procesamientos duplicados.
- eventType: nombre del evento publicado.
- correlationId: relaciona mensajes del mismo proceso, especialmente las
  validaciones de un vehículo externo.
- occurredAt: fecha y hora UTC en formato ISO-8601.
- payload: datos específicos del evento.

## Eventos

### vehiculos.verificacion.solicitada

Solicita la verificación de un vehículo recién registrado.

json
{
  "vehiculoId": 15,
  "patente": "ABC123"
}


### vehiculos.verificacion.resultado

Informa el resultado de la consulta al registro policial simulado.

json
{
  "vehiculoId": 15,
  "patente": "ABC123",
  "resultado": "VALIDADO"
}


Valores permitidos: VALIDADO, ROBADO.

### vehiculos.externos.registrados

Inicia la incorporación de un vehículo perteneciente a un cliente.

json
{
  "vehiculoExternoId": 50,
  "clienteId": 89,
  "patente": "DEF456"
}


### vehiculos.externos.validacion.patente

Informa el resultado de validar la patente del vehículo externo.

json
{
  "vehiculoExternoId": 50,
  "patente": "DEF456",
  "resultado": "VALIDADO"
}


Valores permitidos: VALIDADO, RECHAZADO.

### vehiculos.externos.validacion.titularidad

Informa el resultado de validar que el cliente sea titular del vehículo.

json
{
  "vehiculoExternoId": 50,
  "clienteId": 89,
  "documento": "30123456",
  "patente": "DEF456",
  "resultado": "VALIDADO"
}


Valores permitidos: VALIDADO, RECHAZADO.

### vehiculos.externos.validacion.completada

Indica que ambas validaciones fueron satisfactorias y que puede completarse la
incorporación.

json
{
  "vehiculoExternoId": 50,
  "resultadoFinal": "VALIDADO"
}


### reserva.confirmada

Informa que una reserva fue confirmada y permite iniciar el mantenimiento sin
bloquear la creación de la reserva.

json
{
  "reservaId": 150,
  "vehiculoId": 15,
  "fechaInicio": "2026-10-25T10:00:00",
  "fechaFin": "2026-10-28T10:00:00"
}


### mantenimiento.resultado

Informa el resultado del mantenimiento preventivo.

json
{
  "reservaId": 150,
  "vehiculoId": 15,
  "resultado": "REALIZADO"
}


Valores permitidos: REALIZADO, NO_REALIZADO.

## Reglas de consumo

- El consumidor debe confirmar el mensaje (ack) solamente después de
  procesarlo correctamente.
- Si ocurre un error recuperable, el mensaje debe poder reintentarse.
- Los consumidores deben ser idempotentes usando eventId o una clave de
  negocio equivalente.
- Un resultado ROBADO inhabilita el vehículo, pero no crea un nuevo estado
  operativo.
- Un vehículo externo comienza en PENDIENTE y solo pasa a DISPONIBLE si
  ambas validaciones terminan en VALIDADO.
