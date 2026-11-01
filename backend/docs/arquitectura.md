# Arquitectura y recorrido del código

[Volver al índice](../README.md).

## Organización

```text
backend/
├── pom.xml                         Dependencias y plugins Maven
├── docs/                           Documentación del módulo
└── src/
    ├── main/
    │   ├── java/ar/unla/rentar/
    │   │   ├── RentarApplication.java
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── service/
    │   ├── resources/
    │   │   ├── application.properties
    │   │   └── graphql/schema.graphqls
    │   └── proto/
    └── test/java/
```

`RentarApplication` inicia Spring Boot. El contenedor detecta componentes bajo `ar.unla.rentar`, configura los beans y les proporciona sus dependencias. La generación inicial del proyecto puede hacerse con Spring Initializr; las reglas propias de Rentar están en las clases desarrolladas sobre esa estructura.

## Responsabilidades por capa

| Carpeta | Responsabilidad y ejemplos |
|---|---|
| `controller` | `VehiculoController`, `ClienteController`, `ReservaController` y `AuthController` reciben HTTP. Los dos controllers GraphQL resuelven las queries del esquema. |
| `dto` | Estructuras de transferencia: entradas de creación/modificación, respuestas y filtros. No toda entidad persistida debe exponerse al consumidor. |
| `service` | Reglas de negocio, coordinación local y conversión a respuestas: `VehiculoService`, `ClienteService`, `ReservaService`, `AuthService`. |
| `repository` | Interfaces Spring Data para consultar/persistir entidades. `JpaRepository<Entidad, Long>` aporta operaciones CRUD. |
| `model` | Entidades JPA y enums. `Cliente` y `Vehiculo` se relacionan con muchas `Reserva`. |
| `config` | Seguridad, JWT, OpenAPI e inicialización del administrador local. |
| `resources` | Configuración del entorno y contrato GraphQL. |
| `test` | Pruebas automatizadas del backend; no son endpoints de la aplicación. |

Un `Long` representa el tipo de ID de las entidades. `Optional` permite expresar que una búsqueda puede no encontrar resultado. `@Entity` define una entidad JPA; Hibernate la mapea a SQL. `@Service` registra un componente de negocio. `@RestController` expone respuestas HTTP, mientras que `@QueryMapping` vincula un método con una query GraphQL.

## Ejemplo: alta de vehículo

```text
POST /vehiculos + JSON
 → VehiculoCreateDTO y @Valid
 → VehiculoController.crear
 → VehiculoService.crear
 → VehiculoRepository.save
 → Hibernate / JDBC / MySQL
 → VehiculoResponseDTO + HTTP 201
```

El DTO exige patente, marca y modelo no vacíos, año/tipo no nulos y precio positivo. El service comprueba patente repetida y fija `DISPONIBLE` y `activo=true`. La base mantiene además una restricción única para la patente.

Para modificar se usa `VehiculoUpdateDTO`, que no contiene patente. La entidad también declara `updatable=false` para ese campo. La baja usa `setActivo(false)` y `save`, sin borrar físicamente la fila. `findAll` devuelve también inactivos; habilitación y listado no son el mismo concepto.

Fuentes: [controller](../src/main/java/ar/unla/rentar/controller/VehiculoController.java), [service](../src/main/java/ar/unla/rentar/service/VehiculoService.java), [entidad](../src/main/java/ar/unla/rentar/model/Vehiculo.java), [repository](../src/main/java/ar/unla/rentar/repository/VehiculoRepository.java).

## Clientes

`ClienteService.crear` comprueba documento/email repetidos, codifica la contraseña con BCrypt, fija `activo=true` y `esAdmin=false`. El DTO de modificación permite nombre, apellido, teléfono y fecha de nacimiento; no expone cambio de documento, email, contraseña ni rol.

Las respuestas de `ClienteController` usan `ClienteResponseDto`, sin contraseña. El endpoint `/api/clientes/me` identifica al cliente por el nombre de la autenticación, que corresponde al email del token.

## Reservas

`ReservaService.crearReserva` está anotado con `@Transactional`. Comprueba fechas, existencia y actividad de cliente/vehículo y superposición con reservas `CONFIRMADA` o `EN_CURSO`. Toma el precio del vehículo, calcula importe, crea la reserva como `CONFIRMADA` y cambia el vehículo a `RESERVADO`. Las escrituras participan de la transacción local de este monolito.

El cálculo toma horas completas mediante `Duration.toHours()`, divide por 24 y redondea hacia arriba, con un mínimo de un día. Por ejemplo, un intervalo de exactamente 48 horas cobra dos días. No describirlo como un cálculo exacto por minuto: esa implementación descarta la fracción de hora antes de redondear días.

La cancelación comprueba que el inicio no haya pasado y cambia el estado a `CANCELADA`. Conserva la fila. El método `actualizarReserva` existe en el service, pero no hay una ruta REST ni mutation que lo exponga en los controllers actuales.

## Aspectos del código que condicionan la demostración

- La disponibilidad GraphQL del monolito excluye cualquier reserva superpuesta en su consulta JPA, sin filtrar por estado. Una reserva cancelada puede seguir ocultando el vehículo en esa consulta, aunque la validación de alta de reservas sí ignora canceladas.
- Cancelar no restablece allí el estado del vehículo. Los estados de reserva no se recalculan automáticamente según la fecha en los métodos revisados del monolito.
- El esquema GraphQL expone filtro `tipo`, pero el DTO declara `tipoVehiculo` y el service consulta esa propiedad. No presentar ese filtro como verificado funcionalmente en este módulo.
- Los listados REST de reservas devuelven entidades `Reserva`, con sus relaciones, en lugar de un DTO público acotado. `Cliente` tiene un campo `password` sin exclusión de serialización: revisar la salida antes de compartir capturas o respuestas completas. Los ejemplos GraphQL seleccionan solo campos públicos.
- La seguridad HTTP exige autenticación en la mayoría de rutas, pero no establece controles de administrador para cada ABM. Las comprobaciones de pertenencia tampoco son uniformes. Ver [seguridad](autenticacion.md).

Estos puntos describen lo existente y no se corrigieron al agregar documentación. No deben confundirse con los comportamientos del Gateway y los microservicios de los hitos siguientes.
