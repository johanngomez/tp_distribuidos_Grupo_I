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

text
Java:       17
Maven:      3.9.16
MySQL:      8.4
Node.js:    22.23.2
npm:        10.9.8
Git:        2.55+


---

# 2. Clonar el proyecto

Desde una terminal:

bash
git clone https://github.com/johanngomez/tp_distribuidos_Grupo_I.git


Ingresar al proyecto:

bash
cd tp_distribuidos_Grupo_I


Estructura principal:

text
tp_distribuidos_Grupo_I/
├── api-gateway/
├── backend/                 # Backend monolítico del Hito 1
├── vehicle-service/         # Servicio gRPC de vehículos
├── customer-service/        # Servicio gRPC de clientes
├── rental-service/          # Servicio gRPC de reservas
├── frontend/
└── README.md


---

# 3. Configurar MySQL

El backend utiliza MySQL como base de datos.

Asegurarse de que el servidor MySQL esté iniciado.

Ingresar a MySQL:

bash
mysql -u root -p


Crear la base de datos:

sql
CREATE DATABASE rentar;


Verificar:

sql
SHOW DATABASES;


Debe aparecer:

text
rentar


Salir:

sql
exit;


> **Importante:** cada integrante debe tener MySQL instalado y configurado en su propia computadora.

!!Importante! Si tienen una base de datos antigua de rentar, es necesario para el hito 3 correr el siguiente script de SQL
USE rentar;
ALTER TABLE reserva
DROP FOREIGN KEY fk_reserva_vehiculo;

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

bash
cd backend


En Windows:

powershell
.\mvnw.cmd clean test


Si todo está correctamente configurado debe aparecer:

text
BUILD SUCCESS


---

# 6. Levantar el Backend

Desde:

text
tp_distribuidos_Grupo_I/backend


ejecutar:

powershell
.\mvnw.cmd spring-boot:run


El backend se ejecutará en:

text
http://localhost:8080


Para detenerlo:

text
Ctrl + C


---

# 7. Swagger / OpenAPI

Los endpoints REST serán documentados mediante Swagger/OpenAPI.

Una vez que el backend esté ejecutándose:

text
http://localhost:8080/swagger-ui.html


Especificación OpenAPI:

text
http://localhost:8080/v3/api-docs


> Estas rutas estarán disponibles a medida que se incorporen los controladores REST.

---

# 8. GraphQL

El proyecto utiliza **Spring for GraphQL**.

Endpoint:

text
http://localhost:8080/graphql


Las operaciones GraphQL serán incorporadas durante el desarrollo del Hito 1.

---

# 9. Configurar el Frontend

Abrir otra terminal.

Desde la raíz:

bash
cd frontend


Instalar dependencias:

bash
npm install


> **Importante:** node_modules no se sube a GitHub. Se genera automáticamente ejecutando npm install.

---

# 10. Ejecutar el Frontend

Desde:

text
tp_distribuidos_Grupo_I/frontend


ejecutar:

bash
npm run dev


Vite mostrará una dirección similar a:

text
http://localhost:5173/


Abrirla en el navegador.

Para detenerlo:

text
Ctrl + C


---

# 11. Ejecutar el Hito 1 en modo compatibilidad

Esta modalidad mantiene disponible el backend monolítico original del Hito 1.
Para ejecutar el Hito 2 se deben seguir las instrucciones de la sección
correspondiente, ya que requiere tres servicios Java, el Gateway Python y el
frontend.

Se deben utilizar **dos terminales** para el Hito 1.

### Terminal 1 — Backend

powershell
cd backend
.\mvnw.cmd spring-boot:run


Backend:

text
http://localhost:8080


### Terminal 2 — Frontend

powershell
cd frontend
npm install
npm run dev


Frontend:

text
http://localhost:5173/


---

# 12. Estructura del Backend

text
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


### model/
Entidades y modelos principales.

### repository/
Acceso a datos mediante Spring Data JPA.

### service/
Lógica de negocio.

### controller/
Controladores REST.

### graphql/
Implementaciones relacionadas con GraphQL.

---

# 13. Estructura del Frontend

text
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

text
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

text
SEDAN
SUV
PICKUP
COUPE
HATCHBACK


Estados:

text
DISPONIBLE
RESERVADO
EN_ALQUILER


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

text
CONFIRMADA
CANCELADA


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

El Hito 2 está implementado mediante un API Gateway en Python y tres servicios
internos Java independientes. El frontend continúa utilizando REST y GraphQL;
gRPC se utiliza únicamente para la comunicación interna entre el Gateway y los
servicios Java.

La arquitectura es:

text
React + Vite
      |
      | REST / GraphQL
      v
API Gateway Python
      |
      | gRPC
      +-------------------+-------------------+
      |                   |                   |
      v                   v                   v
Vehicle Service      Customer Service     Rental Service
Java + Spring        Java + Spring        Java + Spring
      |                   |                   |
      +-------------------+-------------------+
                          |
                         MySQL


El Gateway es el único punto de entrada del frontend. No accede directamente a
las tablas de vehículos, clientes o reservas. Cada servicio mantiene la lógica
y la persistencia de su dominio.

### API Gateway

El Gateway está implementado con Python, FastAPI y Uvicorn. Expone REST y
GraphQL, valida JWT y roles, aplica CORS, transforma solicitudes HTTP/GraphQL
en mensajes gRPC y convierte las respuestas gRPC en respuestas externas.

Tecnologías principales: Python 3, FastAPI, Uvicorn, grpcio, grpcio-tools,
Strawberry GraphQL, PyJWT y Protocol Buffers.

Los clientes gRPC están separados por dominio:

text
api-gateway/app/grpc_clients/
├── vehicle_client.py
├── customer_client.py
└── rental_client.py


Los contratos se encuentran en api-gateway/proto/ y los stubs Python se
generan desde esos archivos .proto.

### Servicios y puertos

| Servicio | Puerto gRPC | Base de datos |
|---|---:|---|
| Vehicle Service | 50051 | rentar_vehiculos |
| Customer Service | 50052 | rentar |
| Rental Service | 50053 | rentar |

El Gateway escucha HTTP en 8000 y el frontend Vite normalmente en 5173.

### Ejecución del Hito 2 en Windows

MySQL debe estar iniciado y las bases rentar y rentar_vehiculos deben existir.
Reemplazar TU_CLAVE_MYSQL por la contraseña local de MySQL.

sql
CREATE DATABASE rentar;
CREATE DATABASE rentar_vehiculos;


Cada proceso se ejecuta en una terminal diferente.

Vehicle Service:

powershell
## Terminal 1
cd E:\TpDistribuidos\vehicle-service
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://localhost:3306/rentar_vehiculos --spring.datasource.username=root --spring.datasource.password=TU_CLAVE_MYSQL"

Debe quedar escuchando gRPC en localhost:50051. No cerrar esta terminal.


Customer Service:

powershell
## Terminal 2
cd E:\TpDistribuidos\customer-service
$env:DB_PORT="3306"
$env:DB_NAME="rentar"
$env:DB_USER="root"
$env:DB_PASSWORD="TU_CLAVE_MYSQL"
$env:JWT_SECRET="RentarClaveSecreta2026ParaJWT123456789"
mvn spring-boot:run

Debe quedar escuchando gRPC en localhost:50052. No cerrar esta terminal.


Rental Service:

powershell
## Terminal 3
cd E:\TpDistribuidos\rental-service
$env:DB_PORT="3306"
$env:DB_NAME="rentar"
$env:DB_USER="root"
$env:DB_PASSWORD="TU_CLAVE_MYSQL"
$env:CUSTOMER_GRPC_TARGET="localhost:50052"
$env:VEHICLE_GRPC_TARGET="localhost:50051"
mvn spring-boot:run

Debe quedar escuchando gRPC en localhost:50053. No cerrar esta terminal.


API Gateway:

powershell
## Terminal 4
cd E:\TpDistribuidos\api-gateway
py -3.13 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
$env:JWT_SECRET="RentarClaveSecreta2026ParaJWT123456789"
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload

Debe aparecer Uvicorn running on http://127.0.0.1:8000.

Comprobarlo:

powershell
Invoke-WebRequest http://127.0.0.1:8000/health


Frontend:

powershell
## Terminal 5
cd E:\TpDistribuidos\frontend
npm install
npm run dev

Abrir la URL indicada por Vite, normalmente http://localhost:5173.
El frontend debe utilizar VITE_API_URL=http://127.0.0.1:8000.

### Rutas principales del Gateway

| Método | Ruta | Servicio interno |
|---|---|---|
| POST | /auth/login | Customer Service |
| GET | /auth/me | Validación local del JWT |
| GET/POST | /vehiculos | Vehicle Service |
| PUT/DELETE | /vehiculos/{id} | Vehicle Service |
| GET/POST | /api/clientes | Customer Service |
| GET/PUT/DELETE | /api/clientes/{id} | Customer Service |
| POST | /api/reservas | Customer + Vehicle + Rental |
| GET | /api/reservas | Rental Service |
| PUT | /api/reservas/{id}/cancelar | Rental Service |
| POST | /graphql | Consultas GraphQL del Gateway |

La creación de una reserva se orquesta mediante tres llamadas gRPC: validar el
cliente, verificar la disponibilidad del vehículo y crear la reserva.

### Verificación rápida

powershell
Invoke-RestMethod http://127.0.0.1:8000/health


La respuesta esperada es {"status":"ok"}. La documentación REST está en
http://127.0.0.1:8000/docs y GraphQL en http://127.0.0.1:8000/graphql.

Las rutas protegidas utilizan Authorization: Bearer <JWT>.

## Hito 3 — Mensajería

El Hito 3 incorpora RabbitMQ como broker de mensajes para comunicación
asíncrona. La infraestructura inicial, los contratos de eventos y los archivos
esqueleto están preparados. Los próximos pasos de implementación, ejecución y
pruebas se encuentran en [PROXIMOS_PASOS_HITO3.md](PROXIMOS_PASOS_HITO3.md).
Para ejecutar RabbitMQ se necesita Docker Desktop, que ya incluye Docker
Compose v2.

---

# 17. Trabajo con Git

**No trabajar directamente sobre main.**

Cada integrante debe utilizar su propia rama.

Ejemplo:

text
main
├── feature/base-datos
├── feature/vehiculos
├── feature/clientes
├── feature/reservas
└── feature/graphql


## Antes de comenzar

bash
git checkout main
git pull


Cambiar a la rama propia:

bash
git checkout feature/nombre-de-la-rama


Actualizarla:

bash
git merge main


---

# 18. Guardar cambios

Verificar:

bash
git status


Agregar:

bash
git add .


Commit:

bash
git commit -m "Descripcion del cambio"


Subir:

bash
git push


Primera vez:

bash
git push -u origin feature/nombre-de-la-rama


---

# 19. Pull Requests

Cuando un integrante termina una tarea:

1. Hace push de su rama.
2. Abre un Pull Request en GitHub.
3. El grupo revisa los cambios.
4. Se realiza el merge hacia main.
5. Los demás integrantes actualizan sus ramas.

Después de un merge:

bash
git checkout main
git pull


Luego:

bash
git checkout feature/nombre-de-la-rama
git merge main


---

# 20. Carpetas que NO deben subirse a Git

### Frontend

text
node_modules/
dist/


### Backend

text
target/


Estas carpetas ya están incluidas en los respectivos .gitignore.

Después de clonar:

bash
cd frontend
npm install


Esto vuelve a generar node_modules.

---

# 21. Solución de problemas frecuentes

## Error de conexión con MySQL

Si aparece:

text
Access denied


verificar:

- MySQL está ejecutándose.
- El usuario es correcto.
- La contraseña es correcta.
- Existe la base de datos rentar.
- MySQL utiliza el puerto 3306.

Probar:

bash
mysql -u root -p


## No existe la base de datos

sql
CREATE DATABASE rentar;


## Error al ejecutar npm

Verificar:

bash
node -v
npm -v


Versiones utilizadas:

text
Node.js 22.23.2
npm 10.9.8


Luego:

bash
npm install


## Error al ejecutar Maven

Verificar:

bash
java -version
mvn -version


Se requiere Java 17.

También se puede utilizar el Maven Wrapper:

powershell
.\mvnw.cmd clean test


---

# 22. Flujo recomendado para comenzar a trabajar con el Hito 1

Después de clonar:

powershell
git clone https://github.com/johanngomez/tp_distribuidos_Grupo_I.git
cd tp_distribuidos_Grupo_I


Estas instrucciones corresponden al backend monolítico de compatibilidad. Para
la ejecución principal del Hito 2, utilizar la sección **Ejecución del Hito 2
en Windows**.

### Backend monolítico

powershell
cd backend
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run


### Frontend

En otra terminal:

powershell
cd frontend
npm install
npm run dev


### URLs principales

Frontend:

text
http://localhost:5173/


Backend:

text
http://localhost:8080


Swagger:

text
http://localhost:8080/swagger-ui.html


GraphQL:

text
http://localhost:8080/graphql


---

# 23. Estado actual del proyecto

El Hito 1 se encuentra implementado y el Hito 2 esta integrado

Actualmente se encuentran disponibles:

- Backend monolitico Java + Spring Boot del Hito 1.
- Frontend React + Vite.
- REST y GraphQL.
- Swagger/OpenAPI.
- API Gateway Python como punto de entrada del frontend.
- Comunicacion interna gRPC con Vehicle, Customer y Rental Service.
- Autenticacion JWT, roles y CORS.
- Orquestacion de creacion de reservas.


---
