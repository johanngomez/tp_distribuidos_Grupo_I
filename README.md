#### hito 1 ####

# TP Desarrollo de Software en Sistemas Distribuidos — Grupo I

Sistema web de alquiler de vehículos para la empresa **Rentar**, desarrollado para el Trabajo Práctico de la materia **Desarrollo de Software en Sistemas Distribuidos — Universidad Nacional de Lanús (UNLa)**.

---

## Tecnologías utilizadas

### Backend
- Java 17
- Spring Boot 4.1.1
- Maven 3.9.16
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL 8.4
- Spring for GraphQL
- Spring Validation
- SpringDoc OpenAPI / Swagger

### Frontend
- React
- Vite
- JavaScript
- Node.js 22
- npm
- ESLint

### Control de versiones
- Git
- GitHub

---

# 1. Requisitos previos

Antes de clonar y ejecutar el proyecto, cada integrante debe tener instalados:

- Git
- Java JDK 17
- Maven
- MySQL Server 8
- Node.js 22
- npm
- Visual Studio Code (recomendado)

## Versiones utilizadas

```text
Java:       17
Maven:      3.9.16
MySQL:      8.4
Node.js:    22.23.2
npm:        10.9.8
Git:        2.55+
```

---

# 2. Clonar el proyecto

Desde una terminal:

```bash
git clone https://github.com/johanngomez/tp_distribuidos_Grupo_I.git
```

Ingresar al proyecto:

```bash
cd tp_distribuidos_Grupo_I
```

Estructura principal:

```text
tp_distribuidos_Grupo_I/
├── backend/
├── frontend/
└── README.md
```

---

# 3. Configurar MySQL

El backend utiliza MySQL como base de datos.

Asegurarse de que el servidor MySQL esté iniciado.

Ingresar a MySQL:

```bash
mysql -u root -p
```

Crear la base de datos:

```sql
CREATE DATABASE rentar;
```

Verificar:

```sql
SHOW DATABASES;
```

Debe aparecer:

```text
rentar
```

Salir:

```sql
exit;
```

> **Importante:** cada integrante debe tener MySQL instalado y configurado en su propia computadora.

---

# 4. Configurar variables de enterno

El archivo application.properties utiliza las siguientes variables de entorno para funcionar:

DB_PORT: <puerto de la base de datos>
DB_NAME: <nombre de la base de datos>
DB_USER: <nombre de usuario de la bd>
DB_PASSWORD: <contraseña del usuario de la bd>
JWT_SECRET: <clave del servidor para autenticar inicio de sesión de usuario> (se puede dejar el valor por defecto)


Hay varias formas de configurar variables de entorno, pero en Visual Studio Code pueden apretar CTRL+SHIFT+P e ir a la opción "Open User Settings (JSON)". De ahí poner los datos previos en "terminal.integrated.env.windows".

Ejemplo:

    "terminal.integrated.env.windows": {
    "DB_USER": "ejemplouser",
    "DB_PASSWORD": "ejemplopass",
    "DB_NAME": "grupoi_db",
    "DB_PORT": "3306"
    }

---

# 5. Ejecutar y probar el Backend

Desde la carpeta del proyecto:

```bash
cd backend
```

En Windows:

```powershell
.\mvnw.cmd clean test
```

Si todo está correctamente configurado debe aparecer:

```text
BUILD SUCCESS
```

---

# 6. Levantar el Backend

Desde:

```text
tp_distribuidos_Grupo_I/backend
```

ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

El backend se ejecutará en:

```text
http://localhost:8080
```

Para detenerlo:

```text
Ctrl + C
```

---

# 7. Swagger / OpenAPI

Los endpoints REST serán documentados mediante Swagger/OpenAPI.

Una vez que el backend esté ejecutándose:

```text
http://localhost:8080/swagger-ui.html
```

Especificación OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

> Estas rutas estarán disponibles a medida que se incorporen los controladores REST.

---

# 8. GraphQL

El proyecto utiliza **Spring for GraphQL**.

Endpoint:

```text
http://localhost:8080/graphql
```

Las operaciones GraphQL serán incorporadas durante el desarrollo del Hito 1.

---

# 9. Configurar el Frontend

Abrir otra terminal.

Desde la raíz:

```bash
cd frontend
```

Instalar dependencias:

```bash
npm install
```

> **Importante:** `node_modules` no se sube a GitHub. Se genera automáticamente ejecutando `npm install`.

---

# 10. Ejecutar el Frontend

Desde:

```text
tp_distribuidos_Grupo_I/frontend
```

ejecutar:

```bash
npm run dev
```

Vite mostrará una dirección similar a:

```text
http://localhost:5173/
```

Abrirla en el navegador.

Para detenerlo:

```text
Ctrl + C
```

---

# 11. Ejecutar el proyecto completo

Se deben utilizar **dos terminales**.

### Terminal 1 — Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8080
```

### Terminal 2 — Frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173/
```

---

# 12. Estructura del Backend

```text
backend/
└── src/
    └── main/
        ├── java/
        │   └── ar/
        │       └── unla/
        │           └── rentar/
        │               ├── RentarApplication.java
        │               ├── controller/
        │               ├── graphql/
        │               ├── model/
        │               ├── repository/
        │               └── service/
        └── resources/
            └── application.properties
```

### `model/`
Entidades y modelos principales.

### `repository/`
Acceso a datos mediante Spring Data JPA.

### `service/`
Lógica de negocio.

### `controller/`
Controladores REST.

### `graphql/`
Implementaciones relacionadas con GraphQL.

---

# 13. Estructura del Frontend

```text
frontend/
├── public/
├── src/
│   ├── assets/
│   ├── App.jsx
│   ├── App.css
│   ├── index.css
│   └── main.jsx
├── package.json
├── vite.config.js
└── eslint.config.js
```

La interfaz se desarrollará incrementalmente junto con los requerimientos del TP.

---

# 14. Modelo general del sistema

## Administrador

Puede:

- Realizar ABM de vehículos.
- Realizar ABM de clientes.
- Consultar las reservas de todos los clientes.

## Cliente

Puede:

- Consultar disponibilidad de vehículos.
- Crear reservas.
- Consultar sus propias reservas.
- Cancelar reservas.
- Consultar su historial.

---

# 15. Entidades principales

Relación general:

```text
Cliente
   │
   │ 1
   │
   │ N
Reserva
   │
   │ N
   │
   │ 1
Vehiculo
```

## Cliente

- ID
- Documento
- Nombre
- Apellido
- Email
- Teléfono
- Fecha de nacimiento
- Activo/Inactivo

## Vehículo

- ID
- Patente
- Marca
- Modelo
- Año
- Color
- Tipo
- Precio diario
- Estado
- Activo/Inactivo

Tipos:

```text
SEDAN
SUV
PICKUP
COUPE
HATCHBACK
```

Estados:

```text
DISPONIBLE
RESERVADO
EN_ALQUILER
```

## Reserva

- ID
- Cliente
- Vehículo
- Fecha/hora de inicio
- Fecha/hora de fin
- Precio diario
- Total
- Estado

Estados:

```text
CONFIRMADA
CANCELADA
```

---

# 16. Hitos del Trabajo Práctico

El desarrollo será incremental.

## Hito 1 — REST / GraphQL

Se implementarán:

- Gestión de vehículos mediante REST.
- Consulta de disponibilidad mediante GraphQL.
- Gestión de clientes mediante REST.
- Creación de reservas mediante REST.
- Consulta de reservas mediante GraphQL.
- Cancelación de reservas mediante REST.
- Consulta del historial mediante GraphQL.
- Interfaz web.
- Documentación Swagger/OpenAPI.
- Pruebas.

## Hito 2 — RPC

Se adaptará la arquitectura para incorporar RPC según los requerimientos del TP.

## Hito 3 — Mensajería

Se incorporará Apache Kafka o RabbitMQ según la decisión del grupo.

> Los Hitos 2 y 3 se implementarán posteriormente. El desarrollo actual corresponde al Hito 1.

---

# 17. Trabajo con Git

**No trabajar directamente sobre `main`.**

Cada integrante debe utilizar su propia rama.

Ejemplo:

```text
main
├── feature/base-datos
├── feature/vehiculos
├── feature/clientes
├── feature/reservas
└── feature/graphql
```

## Antes de comenzar

```bash
git checkout main
git pull
```

Cambiar a la rama propia:

```bash
git checkout feature/nombre-de-la-rama
```

Actualizarla:

```bash
git merge main
```

---

# 18. Guardar cambios

Verificar:

```bash
git status
```

Agregar:

```bash
git add .
```

Commit:

```bash
git commit -m "Descripcion del cambio"
```

Subir:

```bash
git push
```

Primera vez:

```bash
git push -u origin feature/nombre-de-la-rama
```

---

# 19. Pull Requests

Cuando un integrante termina una tarea:

1. Hace `push` de su rama.
2. Abre un Pull Request en GitHub.
3. El grupo revisa los cambios.
4. Se realiza el merge hacia `main`.
5. Los demás integrantes actualizan sus ramas.

Después de un merge:

```bash
git checkout main
git pull
```

Luego:

```bash
git checkout feature/nombre-de-la-rama
git merge main
```

---

# 20. Carpetas que NO deben subirse a Git

### Frontend

```text
node_modules/
dist/
```

### Backend

```text
target/
```

Estas carpetas ya están incluidas en los respectivos `.gitignore`.

Después de clonar:

```bash
cd frontend
npm install
```

Esto vuelve a generar `node_modules`.

---

# 21. Solución de problemas frecuentes

## Error de conexión con MySQL

Si aparece:

```text
Access denied
```

verificar:

- MySQL está ejecutándose.
- El usuario es correcto.
- La contraseña es correcta.
- Existe la base de datos `rentar`.
- MySQL utiliza el puerto `3306`.

Probar:

```bash
mysql -u root -p
```

## No existe la base de datos

```sql
CREATE DATABASE rentar;
```

## Error al ejecutar npm

Verificar:

```bash
node -v
npm -v
```

Versiones utilizadas:

```text
Node.js 22.23.2
npm 10.9.8
```

Luego:

```bash
npm install
```

## Error al ejecutar Maven

Verificar:

```bash
java -version
mvn -version
```

Se requiere Java 17.

También se puede utilizar el Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

---

# 22. Flujo recomendado para comenzar a trabajar

Después de clonar:

```powershell
git clone https://github.com/johanngomez/tp_distribuidos_Grupo_I.git
cd tp_distribuidos_Grupo_I
```

### Backend

```powershell
cd backend
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

### Frontend

En otra terminal:

```powershell
cd frontend
npm install
npm run dev
```

### URLs principales

Frontend:

```text
http://localhost:5173/
```

Backend:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

GraphQL:

```text
http://localhost:8080/graphql
```

---

# 23. Estado actual del proyecto

El proyecto se encuentra en la etapa inicial de desarrollo del **Hito 1**.

Actualmente se encuentra configurada la base tecnológica:

- Proyecto Spring Boot.
- Proyecto React + Vite.
- Conexión con MySQL.
- JPA/Hibernate.
- REST.
- GraphQL.
- Validaciones.
- Swagger/OpenAPI.
- Estructura base de carpetas.
- Configuración inicial de Git.

Las funcionalidades del sistema serán implementadas progresivamente por los integrantes del grupo.

---

# 24. Grupo I

**Universidad Nacional de Lanús — UNLa**

**Materia:** Desarrollo de Software en Sistemas Distribuidos

**Proyecto:** Rentar — Sistema de alquiler de vehículos

#### hito 2 ####

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
