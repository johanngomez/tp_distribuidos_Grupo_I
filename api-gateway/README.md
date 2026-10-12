# Rentar API Gateway

API Gateway del Hito 2 del Trabajo Práctico de Desarrollo de Software en Sistemas Distribuidos de la Universidad Nacional de Lanús.

El Gateway está implementado en Python y es el único punto de entrada que utiliza el frontend. Expone REST y GraphQL hacia React y se comunica con los servicios internos mediante gRPC.

## Arquitectura

text
React + Vite
      |
      | REST / GraphQL
      v
API Gateway (Python / FastAPI)
      |
      | gRPC
      +-------------------+-------------------+
      |                   |                   |
      v                   v                   v
Vehicle Service      Customer Service     Rental Service
Java + Spring        Java + Spring        Java + Spring
      |                   |                   |
      v                   v                   v
MySQL                 MySQL                MySQL


El Gateway no accede directamente a las tablas de vehículos, clientes o reservas. Cada servicio interno mantiene la lógica y la persistencia de su dominio.

El backend Java original del Hito 1 se conserva para no romper la aplicación existente. Los servicios del Hito 2 se ejecutan como aplicaciones independientes.

## Responsabilidades del Gateway

- Recibir solicitudes REST y GraphQL desde React.
- Validar JWT, roles y permisos.
- Mantener los contratos públicos utilizados por el frontend.
- Seleccionar el servicio interno correspondiente.
- Convertir solicitudes HTTP/GraphQL en mensajes gRPC.
- Convertir respuestas gRPC en respuestas HTTP/GraphQL.
- Traducir errores gRPC a códigos HTTP apropiados.
- Coordinar operaciones que involucran varios servicios.
- Aplicar CORS para el origen del frontend.

En la creación de una reserva el Gateway coordina este flujo:

text
POST /api/reservas
      |
      +--> Customer Service: validar cliente
      |
      +--> Vehicle Service: verificar existencia, estado y disponibilidad
      |
      +--> Rental Service: crear reserva


## Tecnologías

- Python 3.13+
- FastAPI
- Uvicorn
- grpcio
- grpcio-tools
- Strawberry GraphQL
- PyJWT
- Java 17 para los servicios internos
- Spring Boot
- MySQL

## Estructura

text
api-gateway/
├── app/
│   ├── main.py
│   ├── config/settings.py
│   ├── generated/                 # stubs Python generados desde .proto
│   ├── graphql/schema.py
│   ├── grpc_clients/
│   │   ├── customer_client.py
│   │   ├── rental_client.py
│   │   ├── vehicle_client.py
│   │   └── errors.py
│   ├── routes/
│   │   ├── auth.py
│   │   ├── customers.py
│   │   ├── health.py
│   │   ├── rentals.py
│   │   └── vehicles.py
│   ├── schemas/                    # modelos públicos REST
│   └── security/                   # JWT y dependencias de autorización
├── proto/
│   ├── customer.proto
│   ├── rental.proto
│   └── vehicle.proto
├── scripts/generate_stubs.ps1
├── tests/test_security.py
├── requirements.txt
└── README.md


Cada cliente gRPC es independiente. Las rutas no acceden a repositorios Java ni a MySQL.

## Requisitos previos

- Python 3.13 o compatible.
- Java JDK 17.
- Maven 3.9 o compatible.
- MySQL 8.
- Node.js y npm para ejecutar el frontend.

MySQL debe estar iniciado antes de ejecutar los servicios Java. Las bases utilizadas son:

text
rentar              clientes y reservas
rentar_vehiculos    vehículos


Si todavía no existen, crearlas desde MySQL:

sql
CREATE DATABASE rentar;
CREATE DATABASE rentar_vehiculos;


## Instalación del Gateway

Desde la raíz del repositorio:

powershell
cd api-gateway
py -3.13 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt


No es necesario activar el entorno virtual si se utiliza directamente .\.venv\Scripts\python.exe.

## Configuración

El Gateway utiliza variables de entorno:

| Variable | Valor predeterminado | Uso |
|---|---|---|
| FRONTEND_ORIGIN | http://localhost:5173 | Origen permitido por CORS |
| VEHICLE_GRPC_TARGET | localhost:50051 | Vehicle Service |
| CUSTOMER_GRPC_TARGET | localhost:50052 | Customer Service |
| RENTAL_GRPC_TARGET | localhost:50053 | Rental Service |
| GRPC_TIMEOUT_SECONDS | 5 | Tiempo máximo de cada llamada gRPC |
| JWT_SECRET | sin valor predeterminado | Clave para verificar JWT |

JWT_SECRET debe coincidir con la clave utilizada por Customer Service y por el backend Java que emite los tokens. No guardar claves ni tokens en el repositorio.

Para consultar la configuración sin conectarse a los servicios:

powershell
.\.venv\Scripts\python.exe -c "from app.config.settings import load_settings; print(load_settings())"


## Ejecución completa en Windows

Cada proceso debe ejecutarse en una terminal diferente. MySQL debe estar iniciado previamente.

### 1. Vehicle Service

powershell
cd vehicle-service
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://localhost:3306/rentar_vehiculos --spring.datasource.username=root --spring.datasource.password=TU_CLAVE_MYSQL"


Escucha gRPC en localhost:50051.

### 2. Customer Service

powershell
cd customer-service
$env:DB_PORT="3306"
$env:DB_NAME="rentar"
$env:DB_USER="root"
$env:DB_PASSWORD="TU_CLAVE_MYSQL"
$env:JWT_SECRET="RentarClaveSecreta2026ParaJWT123456789"
mvn spring-boot:run


Escucha gRPC en localhost:50052.

### 3. Rental Service

powershell
cd rental-service
$env:DB_PORT="3306"
$env:DB_NAME="rentar"
$env:DB_USER="root"
$env:DB_PASSWORD="TU_CLAVE_MYSQL"
$env:CUSTOMER_GRPC_TARGET="localhost:50052"
$env:VEHICLE_GRPC_TARGET="localhost:50051"
mvn spring-boot:run


Escucha gRPC en localhost:50053.

### 4. API Gateway

powershell
cd api-gateway
$env:JWT_SECRET="RentarClaveSecreta2026ParaJWT123456789"
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload


Expone REST y GraphQL en http://127.0.0.1:8000.

### 5. Frontend

powershell
cd frontend
npm install
npm run dev


Vite normalmente publica la aplicación en http://localhost:5173.

## Verificación rápida

Comprobar el estado del Gateway:

powershell
Invoke-RestMethod http://127.0.0.1:8000/health


La respuesta esperada es:

json
{"status":"ok"}


Comprobar los puertos:

powershell
netstat -ano | findstr ":50051 :50052 :50053 :8000"


La ruta /health solo comprueba que el proceso HTTP del Gateway responde. Las operaciones de negocio también requieren que los servicios gRPC y MySQL estén activos.

## REST

La documentación interactiva está disponible en:

text
http://127.0.0.1:8000/docs


Rutas principales:

| Método | Ruta | Servicio interno |
|---|---|---|
| POST | /auth/login | Customer Service |
| GET | /auth/me | Validación local del JWT |
| GET | /api/vehiculos | Vehicle Service |
| POST | /api/vehiculos | Vehicle Service |
| PUT | /api/vehiculos/{id} | Vehicle Service |
| DELETE | /api/vehiculos/{id} | Vehicle Service |
| GET | /api/clientes | Customer Service |
| GET | /api/clientes/me | Customer Service |
| POST | /api/clientes | Customer Service |
| PUT | /api/clientes/{id} | Customer Service |
| DELETE | /api/clientes/{id} | Customer Service |
| POST | /api/reservas | Customer + Vehicle + Rental |
| GET | /api/reservas | Rental Service |
| PUT | /api/reservas/{id}/cancelar | Rental Service |

Las rutas protegidas esperan:

text
Authorization: Bearer <JWT>


La creación, modificación y baja de vehículos requieren permisos de administrador. Las respuestas de disponibilidad, validación o caída de un servicio se traducen a códigos HTTP como 400, 401, 403, 404, 409, 502 y 503, según corresponda.

Para crear una reserva, las fechas deben enviarse en formato ISO-8601:

json
{
  "clienteId": 1,
  "vehiculoId": 1,
  "fechaInicio": "2026-10-10T00:00:00",
  "fechaFin": "2026-10-15T00:00:00"
}


## GraphQL

El endpoint GraphQL es:

text
http://127.0.0.1:8000/graphql


El schema mantiene el tipo público FiltroReservaInput utilizado por el frontend.

Ejemplo de consulta de reservas:

graphql
query($filtro: FiltroReservaInput) {
  reservas(filtro: $filtro) {
    id
    fechaInicio
    fechaFin
    precioDiario
    importeTotal
    estado
    cliente { id nombre apellido }
    vehiculo { id patente marca modelo }
  }
}


Variables de ejemplo:

json
{
  "filtro": {
    "fechaInicioDesde": "2026-10-10",
    "fechaInicioHasta": "2026-10-31"
  }
}


El Gateway convierte automáticamente esas fechas al formato LocalDateTime requerido por Rental Service. GraphQL también expone consultas de disponibilidad e historial.

## Login y JWT

POST /auth/login envía las credenciales al Customer Service mediante gRPC. El Gateway devuelve el token emitido por el servicio.

El Gateway verifica localmente:

- firma HS256;
- emisor rentar;
- sujeto con el email;
- fechas iat y exp;
- claim esAdmin.

Para probar desde Swagger:

1. Ejecutar POST /auth/login.
2. Copiar el token recibido.
3. Pulsar **Authorize**.
4. Pegar el token sin escribir manualmente Bearer.
5. Ejecutar las rutas protegidas.

## CORS

El origen predeterminado de React es http://localhost:5173.

Para cambiarlo:

powershell
$env:FRONTEND_ORIGIN="http://localhost:5173"


El origen debe ser exacto, sin una ruta adicional ni una barra final. Se permiten los métodos GET, POST, PUT, DELETE y OPTIONS, además de los encabezados Authorization y Content-Type.

Prueba preflight:

powershell
$headers = @{ Origin="http://localhost:5173"; "Access-Control-Request-Method"="GET"; "Access-Control-Request-Headers"="authorization,content-type" }
$response = Invoke-WebRequest -UseBasicParsing -Method Options -Uri "http://127.0.0.1:8000/health" -Headers $headers
$response.StatusCode
$response.Headers["Access-Control-Allow-Origin"]


Se espera 200 y http://localhost:5173.

## Contratos gRPC y stubs

Los contratos fuente están en proto/:

- vehicle.proto: operaciones de consulta y ABM de vehículos.
- customer.proto: login, consulta, ABM y validación de clientes.
- rental.proto: creación, consulta, cancelación e historial de reservas.

Los archivos *_pb2.py y *_pb2_grpc.py son generados a partir de esos contratos y utilizados por los clientes Python. Los servicios Java generan sus propias clases durante la compilación Maven.

Para regenerar los stubs Python:

powershell
.\scripts\generate_stubs.ps1


Los .proto deben mantenerse coordinados entre Gateway Python y los servicios Java. Un cambio en un mensaje o nombre de RPC requiere regenerar los stubs y recompilar los servicios.

## Pruebas

Pruebas automáticas del Gateway:

powershell
.\.venv\Scripts\python.exe -B -m unittest discover -s tests -v


Estas pruebas cubren autenticación, tokens válidos e inválidos, roles, expiración, firma incorrecta y acceso público a /health.

Pruebas de integración manuales recomendadas:

1. Login desde Swagger.
2. Consulta de vehículos.
3. Consulta de clientes.
4. Alta de una reserva con fechas ISO.
5. Consulta y filtrado de reservas desde GraphQL.
6. Cancelación de una reserva.
7. Consulta de historial.
8. Consulta de disponibilidad.
9. Detener Customer Service y comprobar que una reserva devuelve error de disponibilidad.
10. Detener Vehicle Service y comprobar el error correspondiente.

## Manejo de errores

Los clientes gRPC convierten errores de transporte en GrpcServiceError. Las rutas del Gateway los traducen a respuestas HTTP comprensibles:

- INVALID_ARGUMENT → 400.
- UNAUTHENTICATED → 401.
- PERMISSION_DENIED → 403.
- NOT_FOUND → 404.
- FAILED_PRECONDITION → 409.
- UNAVAILABLE → 503.
- DEADLINE_EXCEEDED → 504.

## Hito 1 y compatibilidad

El backend Java monolítico, sus controladores REST, resolvers GraphQL, servicios, repositorios y login no se eliminan. Permanecen disponibles para conservar el funcionamiento del Hito 1.

En el Hito 2, el frontend utiliza VITE_API_URL para comunicarse con el Gateway:

text
VITE_API_URL=http://127.0.0.1:8000


REST y GraphQL siguen siendo las interfaces externas. gRPC se utiliza únicamente entre el Gateway y los servicios internos.

## Solución de problemas

## Git

No trabajar directamente sobre main. Los cambios del Gateway se desarrollan en la rama acordada por el grupo. Antes de guardar cambios:

powershell
git status
git branch --show-current
git diff --check


Los commits, merges y pushes deben realizarse de acuerdo con la coordinación del grupo.
