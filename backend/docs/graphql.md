# GraphQL: consultas listas para probar

[Volver al índice](../README.md). IDE: http://localhost:8080/graphiql. Endpoint HTTP: http://localhost:8080/graphql.

El contrato fuente es [schema.graphqls](../src/main/resources/graphql/schema.graphqls). Este módulo usa Spring for GraphQL, no Strawberry. Strawberry está en el Gateway Python de HTTP 8000.

## Queries y tipos

| Query | Argumentos principales | Resultado |
|---|---|---|
| `consultarDisponibilidad` | Inicio/fin obligatorios; tipo, marca, modelo y precios opcionales | `[Vehiculo]` |
| `reservas` | `filtro: FiltroReservaInput` opcional | `[Reserva]` con cliente y vehículo |
| `historialAlquileres` | `clienteId: ID!` | `[HistorialReserva]` |

Solo se define `Query`. No hay `Mutation`: la consigna asigna las operaciones de escritura a REST. `!` indica un valor no nulo en ese nivel; los corchetes representan listas. Los enums se escriben sin comillas dentro de la consulta y como strings cuando viajan en JSON de variables.

En el panel de headers de GraphiQL, cuando esté disponible, enviar:

```json
{
  "Authorization": "Bearer TOKEN_JWT_RECIBIDO"
}
```

Si la interfaz no permite configurar headers, usar curl como en el ejemplo final. La query `reservas` depende de la identidad del token, aunque la URL GraphQL sea pública en la configuración HTTP.

## 1. Disponibilidad mínima

```graphql
query Disponibilidad {
  consultarDisponibilidad(
    fechaInicio: "2030-11-10T10:00:00"
    fechaFin: "2030-11-12T10:00:00"
  ) {
    id
    patente
    marca
    modelo
    anio
    color
    tipo
    precioDiario
    estado
    activo
  }
}
```

Cambiar fechas según la demo. Una lista vacía es un resultado válido si no hay vehículos que cumplan las condiciones.

## 2. Disponibilidad con filtros y variables

```graphql
query DisponibilidadFiltrada($inicio: String!, $fin: String!, $tipo: TipoVehiculo) {
  consultarDisponibilidad(
    fechaInicio: $inicio
    fechaFin: $fin
    tipo: $tipo
    marca: "Toyota"
    modelo: "Corolla"
    precioMin: 10000
    precioMax: 100000
  ) {
    id
    patente
    marca
    modelo
    precioDiario
  }
}
```

En el panel **Variables**:

```json
{
  "inicio": "2030-11-10T10:00:00",
  "fin": "2030-11-12T10:00:00",
  "tipo": "SEDAN"
}
```

El resolver parsea fechas, adapta el enum y los precios y llama a `VehiculoRepository.findDisponibles`. Esa consulta exige actividad y ausencia de reservas superpuestas, con filtros opcionales. Marca/modelo usan la comparación de la consulta SQL y su collation; no hay normalización explícita en Java.

La consulta actual no excluye reservas canceladas del cálculo de ocupación. Tampoco valida explícitamente en ese resolver que el inicio sea futuro o que fin sea posterior. Son diferencias respecto de la validación del alta de reserva, no propiedades del lenguaje GraphQL.

## 3. Reservas visibles para el usuario

```graphql
query Reservas {
  reservas {
    id
    fechaInicio
    fechaFin
    precioDiario
    importeTotal
    estado
    cliente {
      id
      nombre
      apellido
      email
    }
    vehiculo {
      id
      patente
      marca
      modelo
    }
  }
}
```

El service identifica al cliente por el email autenticado. Un administrador puede ver todas; un cliente normal queda restringido a su ID en esta query.

## 4. Reservas filtradas

```graphql
query ReservasFiltradas($filtro: FiltroReservaInput) {
  reservas(filtro: $filtro) {
    id
    estado
    fechaInicio
    fechaFin
    importeTotal
    cliente { id nombre apellido }
    vehiculo { id patente tipo }
  }
}
```

Variables ilustrativas, con IDs a reemplazar:

```json
{
  "filtro": {
    "clienteId": "2",
    "vehiculoId": "1",
    "estado": "CONFIRMADA",
    "fechaInicioDesde": "2030-11-01T00:00:00",
    "fechaInicioHasta": "2030-11-30T23:59:59"
  }
}
```

Se pueden quitar campos del filtro. `clienteId` se aplica como filtro libre para administradores; un cliente normal conserva su restricción de identidad. El rango filtra **fecha de inicio** y el service solo lo agrega si recibe ambos extremos; no es la misma búsqueda de solapamiento de disponibilidad.

El esquema también declara `tipo`, pero `ReservaFiltroDTO` lo denomina `tipoVehiculo`. Esa diferencia requiere revisar el binding antes de presentar una prueba de filtrado de reservas por tipo. Por eso los ejemplos de filtros de reserva no lo utilizan; el filtro de tipo de disponibilidad usa otro resolver.

## 5. Historial de alquileres

```graphql
query Historial($clienteId: ID!) {
  historialAlquileres(clienteId: $clienteId) {
    vehiculo
    patente
    fechaInicio
    fechaFin
    cantidadDias
    importeTotal
    estado
  }
}
```

Variables:

```json
{
  "clienteId": "2"
}
```

Reemplazar el ID por un cliente existente. El resolver actual obtiene todas sus reservas y calcula `cantidadDias` con `ChronoUnit.DAYS`; no filtra exclusivamente finalizadas/canceladas ni actualiza estados según la fecha. Además, no comprueba allí la pertenencia del ID solicitado. Estas limitaciones deben explicarse al demostrar el monolito.

## Consulta desde curl

```bash
curl -i http://localhost:8080/graphql \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer TOKEN_JWT_RECIBIDO' \
  -d '{"query":"query { reservas { id estado vehiculo { patente } } }"}'
```

El body GraphQL puede contener `data` y `errors`: revisar ambos, no solo el estado HTTP. Si hay varios documentos de operación en un mismo request, se indica cuál ejecutar con `operationName`.

## Errores frecuentes

- Un campo que devuelve objetos necesita selección de subcampos: `reservas { id }`, no solo `reservas`.
- No usar ID vacío ni marcadores de texto donde el resolver convierte a `Long`.
- Usar `YYYY-MM-DDTHH:MM:SS` para los argumentos de fecha/hora; no el formato visual de una fecha local.
- Enviar una query a `/graphql`, no a `/graphiql`: la segunda ruta sirve la interfaz.
- Si `reservas` indica que no encuentra el cliente, comprobar token, email y base seleccionada.
- Para esta guía usar 8080. Las queries del Gateway en 8000 recorren otros resolvers y servicios.
