# Propuesta de contrato gRPC para Vehicle Service

Estado: propuesta para revisar con el equipo. Este archivo no es el `vehicle.proto`
definitivo y no genera stubs.

## Objetivo

Permitir que el Gateway implemente el primer flujo del Hito 2:

```text
GET /vehiculos
  → VehicleGrpcClient.list_vehicles()
  → Vehicle Service Java
  → persistencia del dominio de vehículos
```

El Gateway transforma la respuesta gRPC en el JSON que ya consume React. El
Vehicle Service mantiene la validación, las reglas del dominio y el acceso a
MySQL. El Gateway no consulta tablas ni repositorios.

## Fuente del contrato: backend Java existente

La propuesta se basa en el código que ya existe en Hito 1:

- `Vehiculo` es la entidad Java persistida.
- `VehiculoResponseDTO` define la respuesta pública de REST.
- `TipoVehiculo` contiene `SEDAN`, `SUV`, `PICKUP`, `COUPE` y `HATCHBACK`.
- `EstadoVehiculo` contiene `DISPONIBLE`, `RESERVADO` y `EN_ALQUILER`.
- `VehiculoService.listarTodos()` devuelve `VehiculoResponseDTO`.
- `VehiculoController` publica actualmente `GET /vehiculos` y las operaciones
  de consulta, alta, modificación y baja lógica.

Protobuf será un contrato interno. No tiene que copiar las anotaciones JPA ni
los nombres de las columnas MySQL. El Gateway adaptará sus mensajes al JSON
camelCase que React ya consume.

## RPC mínimo para la primera prueba

```text
service VehicleService {
  rpc ListVehicles(ListVehiclesRequest) returns (ListVehiclesResponse);
}
```

`ListVehicles` no recibe filtros en esta primera versión. Devuelve el listado
que actualmente expone `GET /vehiculos`, incluyendo los campos necesarios para
mantener el contrato público del Hito 1.

## Mensajes y campos a acordar

| Mensaje/campo | Tipo sugerido | Motivo |
|---|---|---|
| `ListVehiclesRequest` | mensaje vacío | La operación actual no tiene parámetros. |
| `ListVehiclesResponse.vehicles` | `repeated Vehicle` | Representa una lista, incluso cuando está vacía. |
| `Vehicle.id` | `int64` | Corresponde al `Long` Java y al `id` JSON. |
| `Vehicle.patente` | `string` | Identificador público del vehículo. |
| `Vehicle.marca` | `string` | Se muestra en React. |
| `Vehicle.modelo` | `string` | Se muestra en React. |
| `Vehicle.anio` | `int32` | Corresponde al `Integer` Java. |
| `Vehicle.color` | `string` | Puede ser opcional en la entidad actual. |
| `Vehicle.tipo` | `string` o enum acordado | Java utiliza `TipoVehiculo`; hay que acordar los valores. |
| `Vehicle.precio_diario` | `double` o representación decimal acordada | Debe conservar el precio que React muestra. |
| `Vehicle.estado` | `string` o enum acordado | Java utiliza `EstadoVehiculo`. |
| `Vehicle.activo` | `bool` | Indica la baja lógica. |

Los nombres de Protobuf pueden cambiar si el equipo tiene otra convención. Lo
importante es acordar tipos, presencia de campos y conversión hacia estas claves
JSON públicas: `id`, `patente`, `marca`, `modelo`, `anio`, `color`, `tipo`,
`precioDiario`, `estado` y `activo`.

La correspondencia con el DTO Java es:

| Respuesta pública Java/React | Campo interno sugerido | Fuente Java |
|---|---|---|
| `id` | `id` | `Vehiculo.id` (`Long`) |
| `patente` | `patente` | `Vehiculo.patente` |
| `marca` | `marca` | `Vehiculo.marca` |
| `modelo` | `modelo` | `Vehiculo.modelo` |
| `anio` | `anio` | `Vehiculo.anio` (`Integer`) |
| `color` | `color` | `Vehiculo.color` |
| `tipo` | `tipo` | `Vehiculo.tipo` (`TipoVehiculo`) |
| `precioDiario` | `precio_diario` | `Vehiculo.precioDiario` (`double`) |
| `estado` | `estado` | `Vehiculo.estado` (`EstadoVehiculo`) |
| `activo` | `activo` | `Vehiculo.activo` (`Boolean`) |

El nombre `precio_diario` es solo una convención posible de Protobuf. Al
serializar la respuesta HTTP, el Gateway debe devolver `precioDiario`, igual
que el DTO actual.

## Operaciones mínimas del Vehicle Service según el TP

El TP exige que el servicio de vehículos pueda consultar vehículos, consultar
uno específico, consultar disponibilidad, actualizar el estado y aplicar sus
validaciones. Por eso el contrato completo deberá incluir, como mínimo, una
propuesta equivalente a:

```text
ListVehicles          → listar vehículos
GetVehicle             → consultar por id
CheckAvailability      → consultar disponibilidad con fechas y filtros
UpdateVehicleStatus    → cambiar DISPONIBLE/RESERVADO/EN_ALQUILER
```

Para conservar las funciones del Hito 1, también habrá que cubrir las
operaciones de administración de vehículos: alta, modificación y baja lógica.
Sus nombres y mensajes se acordarán en una segunda versión del contrato. No se
implementan en esta primera prueba.

La disponibilidad actual de Java cruza vehículos y reservas. En Hito 2 hay que
decidir con el equipo si `CheckAvailability` recibe esa información mediante
una coordinación del Gateway con Rental Service, o si Vehicle Service expone
una operación preparada para esa consulta. Esa decisión pertenece al diseño
conjunto y no debe resolverse accediendo a la base desde el Gateway.

## Decisiones pendientes

Antes de generar stubs, el equipo debe confirmar:

1. Nombre del servicio y de la RPC.
2. Paquete Protobuf y versión del contrato.
3. Si `tipo` y `estado` serán enums Protobuf o strings.
4. Representación del precio: `double` es simple y compatible con Hito 1;
   un tipo decimal evita ciertos redondeos y requiere una decisión adicional.
5. Si el listado devuelve vehículos inactivos, igual que el endpoint actual,
   o si el servicio filtrará bajas lógicas.
6. Códigos gRPC para errores (`UNAVAILABLE`, `DEADLINE_EXCEEDED`, etc.).
7. Puerto y dirección donde escuchará el servicio Java.
8. Cómo se propagará la identidad del usuario cuando una operación la necesite.

## Futuras RPC, fuera del primer alcance

El contrato completo agregará las RPC anteriores y las operaciones de
administración necesarias para conservar el Hito 1, después de probar
`ListVehicles`.

## Entregables después de la aprobación

1. El equipo publica el `vehicle.proto` acordado.
2. Se generan stubs Java y Python desde ese mismo archivo.
3. Vehicle Service implementa el servidor Java.
4. El Gateway implementa `VehicleGrpcClient` usando el stub Python.
5. Se agrega `GET /vehiculos` y se prueba el flujo completo.
