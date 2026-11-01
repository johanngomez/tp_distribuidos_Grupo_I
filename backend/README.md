# Backend monolítico — Hito 1 de Rentar

Este módulo implementa la API original en Java: REST para autenticación, vehículos, clientes y reservas; GraphQL para disponibilidad, reservas e historial. Accede directamente a una base MySQL mediante JPA/Hibernate.

En los hitos posteriores, el frontend utiliza el [API Gateway Python](../api-gateway/README.md), que llama a los servicios Java por gRPC. Este módulo se conserva para estudiar y demostrar el Hito 1: **sus ejemplos usan HTTP 8080, no el Gateway en 8000**.

## Documentación

| Guía | Contenido |
|---|---|
| [Arquitectura y código](docs/arquitectura.md) | Carpetas, capas, entidades, DTO, repositorios y reglas |
| [REST y ejemplos](docs/rest.md) | Todas las rutas implementadas y cuerpos JSON para Swagger/curl |
| [GraphQL y consultas](docs/graphql.md) | Esquema, filtros, variables y consultas copiables |
| [Login y seguridad](docs/autenticacion.md) | BCrypt, JWT, permisos y uso del token |
| [Demostración para la defensa](docs/demostracion.md) | Orden de pruebas y resultados que observar |

Los documentos describen el código actual. Señalan las limitaciones del monolito para distinguir una regla prevista de una comprobación efectivamente implementada. No documentan funciones de RabbitMQ como si pertenecieran a este backend.

## Preparar una demostración aislada

Requisitos: Java 17, Maven, MySQL y un usuario con permisos sobre la base elegida. Las versiones declaradas están en [pom.xml](pom.xml).

Para evitar modificar los datos de los microservicios, puede usarse una base separada y vacía para el monolito. Desde la raíz del repositorio:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS rentar_hito1_demo CHARACTER SET utf8mb4;"
cd backend
export DB_PORT=3306
export DB_NAME=rentar_hito1_demo
export DB_USER=root
export DB_PASSWORD='TU_CLAVE_MYSQL'
export JWT_SECRET='ClaveLocalDeDemostracionConMasDe32Bytes123456'
mvn spring-boot:run '-Dspring-boot.run.arguments=--server.port=8080 --grpc.server.port=50054'
```

Reemplazar la contraseña antes de ejecutar. El secreto es un ejemplo local, no una credencial de producción. No requiere compartirlo con el Gateway si este no participa de la demostración.

Hibernate tiene `ddl-auto=update`: intenta crear las tablas faltantes en la base seleccionada. `ClienteInicializador` prepara el administrador de prueba. Esta guía no afirma que el monolito haya sido arrancado y probado integralmente durante la redacción; los ejemplos se contrastaron con sus controllers, DTO y esquema GraphQL.

El puerto gRPC 50054 se fuerza para no ocupar el 50053 de Rental Service: `application.properties` del monolito también declara gRPC 50053. Los controllers HTTP de esta guía no necesitan que se ejecuten los tres microservicios ni RabbitMQ.

No ejecutar `script_rentar_V2.sql` sobre bases con datos para preparar esta demo: contiene operaciones de borrado y recarga. Para usar una base existente, comprobar antes que su esquema coincida con las entidades de este módulo. El dump legacy utiliza `tipo_vehiculo`, mientras que la entidad actual declara `tipo`; no se debe asumir que la migración de datos entre ambas columnas sea automática.

## URLs

| Interfaz | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| GraphiQL | http://localhost:8080/graphiql |
| Endpoint GraphQL | http://localhost:8080/graphql |
| Login | `POST http://localhost:8080/auth/login` |

El `/health` documentado por el Gateway no está implementado como controller en este módulo.

Si se quiere probar React contra el monolito, iniciar el frontend con `VITE_API_URL=http://localhost:8080 npm run dev` desde `frontend/`. Eso cambia su destino durante esa ejecución; los flujos incorporados para los hitos 2/3, como vehículos externos, no existen en este backend. CORS admite `http://localhost:5173`.
