# API GraphQL del API Gateway

GraphQL está expuesto públicamente en http://127.0.0.1:8000/graphql para facilitar la demostración y exploración del esquema. No requiere JWT. Las operaciones REST continúan protegidas mediante Authorization: Bearer <token>.

El esquema también puede explorarse desde la interfaz GraphiQL del endpoint cuando el token está configurado en el cliente.

## Consultar reservas

Consulta reservas visibles para el usuario autenticado. Los administradores pueden consultar todas las reservas; los clientes reciben únicamente las propias.

graphql
query Reservas($filtro: FiltroReservaInput) {
  reservas(filtro: $filtro) {
    id
    fechaInicio
    fechaFin
    precioDiario
    importeTotal
    estado
    cliente { id nombre apellido documento }
    vehiculo { id patente marca modelo tipo estado }
  }
}


Variables para filtrar por tipo, estado y rango de fechas:

json
{
  "filtro": {
    "tipo": "SUV",
    "estado": "CONFIRMADA",
    "fechaInicioDesde": "2026-10-10",
    "fechaInicioHasta": "2026-10-31"
  }
}


Respuesta resumida:

json
{
  "data": {
    "reservas": [{
      "id": "18",
      "fechaInicio": "2026-10-12T12:02:00",
      "fechaFin": "2026-10-15T12:02:00",
      "precioDiario": 62000,
      "importeTotal": 186000,
      "estado": "CONFIRMADA",
      "vehiculo": {"id": "1", "patente": "BBB223", "marca": "Honda", "modelo": "HR-V", "tipo": "SUV", "estado": "DISPONIBLE"}
    }]
  }
}


## Consultar disponibilidad

Devuelve vehículos activos y disponibles en un período. Se pueden aplicar filtros opcionales por tipo, marca, modelo y precio. Si no se indican fechas, se utiliza el período de demostración 2026-11-10T00:00:00 a 2026-11-15T00:00:00.

graphql
query Disponibilidad {
  consultarDisponibilidad(
    fechaInicio: "2026-11-10T00:00:00"
    fechaFin: "2026-11-15T00:00:00"
    tipo: SUV
    precioMax: 100000
  ) {
    id patente marca modelo anio color tipo precioDiario estado activo
  }
}


## Consultar historial

graphql
query Historial {
  historialAlquileres(clienteId: "1") {
    vehiculo
    patente
    fechaInicio
    fechaFin
    cantidadDias
    importeTotal
    estado
  }
}


## Tipos y convenciones

- TipoVehiculo: SEDAN, SUV, PICKUP, COUPE, HATCHBACK.
- VehicleState: DISPONIBLE, RESERVADO, EN_ALQUILER.
- RentalState: CONFIRMADA, EN_CURSO, CANCELADA, FINALIZADA.
- Las fechas se representan como texto ISO-8601: YYYY-MM-DDTHH:MM:SS. Los filtros de fecha de FiltroReservaInput aceptan YYYY-MM-DD y el Gateway los normaliza internamente.

GraphQL devuelve errores en el campo errors cuando un servicio interno no está disponible o una consulta contiene datos inválidos.

El esquema actual expone consultas de lectura (Query) y no define mutaciones (Mutation). Las operaciones de alta, modificación, baja, creación de reservas y cancelación se realizan mediante los endpoints REST documentados en Swagger.
