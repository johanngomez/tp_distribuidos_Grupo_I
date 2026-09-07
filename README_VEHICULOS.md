# README — Módulo de Vehículos

## 1. Objetivo

Este documento explica cómo está implementado el módulo de **Vehículos** del sistema Rentar y cómo utilizarlo, levantarlo, probarlo e integrarlo con el resto del proyecto.

Corresponde al **Hito 1 — Punto 1: Gestión de vehículos mediante REST**.

El módulo permite:

- Alta de vehículos.
- Consulta de todos los vehículos.
- Consulta por ID.
- Modificación.
- Baja lógica.
- Validación de datos obligatorios.
- Patente única y no modificable.
- Estado inicial `DISPONIBLE`.
- Estado inicial `activo = true`.
- Persistencia mediante Spring Data JPA/Hibernate en MySQL.
- Documentación y pruebas mediante Swagger/OpenAPI.

---

## 2. Tecnologías

### Backend

- Java 17
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL 8.4
- Spring Validation
- SpringDoc OpenAPI / Swagger
- Maven

### Frontend

- React
- Vite
- JavaScript
- Node.js 22
- npm

---

## 3. Estructura general del proyecto

```text
tp_distribuidos_Grupo_I/
│
├── backend/
│   ├── .mvn/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ar/unla/rentar/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── graphql/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── RentarApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── index.css
│   │   └── main.jsx
│   ├── eslint.config.js
│   ├── index.html
│   ├── package.json
│   ├── package-lock.json
│   └── vite.config.js
│
└── README.md
```

La estructura anterior refleja el proyecto actual. La carpeta `graphql` queda preparada para las funcionalidades GraphQL posteriores.

---

# 4. Archivos del módulo de Vehículos

```text
backend/src/main/java/ar/unla/rentar/
│
├── controller/
│   └── VehiculoController.java
│
├── dto/
│   ├── VehiculoCreateDTO.java
│   ├── VehiculoResponseDTO.java
│   └── VehiculoUpdateDTO.java
│
├── model/
│   ├── EstadoVehiculo.java
│   ├── TipoVehiculo.java
│   └── Vehiculo.java
│
├── repository/
│   └── VehiculoRepository.java
│
└── service/
    └── VehiculoService.java
```

## `Vehiculo.java`

Es la entidad JPA que representa un vehículo.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | PK autogenerada |
| `patente` | `String` | Obligatoria, única y no modificable |
| `marca` | `String` | Obligatoria |
| `modelo` | `String` | Obligatoria |
| `anio` | `Integer` | Año |
| `color` | `String` | Color |
| `tipo` | `TipoVehiculo` | Tipo del vehículo |
| `precioDiario` | `double` | Precio diario |
| `estado` | `EstadoVehiculo` | Estado operativo |
| `activo` | `Boolean` | Activo/Inactivo |

## `TipoVehiculo.java`

Tipos permitidos:

```text
SEDAN
SUV
PICKUP
COUPE
HATCHBACK
```

## `EstadoVehiculo.java`

Estados permitidos:

```text
DISPONIBLE
RESERVADO
EN_ALQUILER
```

## `VehiculoCreateDTO.java`

Se utiliza en:

```http
POST /vehiculos
```

Recibe:

```json
{
  "patente": "AB456CD",
  "marca": "Ford",
  "modelo": "EcoSport",
  "anio": 2024,
  "color": "Negro",
  "tipo": "SUV",
  "precioDiario": 55000
}
```

No recibe `id`, `estado` ni `activo`: esos valores son administrados por el backend.

## `VehiculoUpdateDTO.java`

Se utiliza en:

```http
PUT /vehiculos/{id}
```

No contiene `patente`, por lo que la patente no se puede modificar.

Ejemplo:

```json
{
  "marca": "Toyota",
  "modelo": "Corolla Cross",
  "anio": 2025,
  "color": "Blanco",
  "tipo": "SUV",
  "precioDiario": 65000,
  "estado": "DISPONIBLE"
}
```

## `VehiculoResponseDTO.java`

Representa la respuesta de la API:

```text
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
```

## `VehiculoRepository.java`

Accede a la entidad mediante Spring Data JPA.

Incluye:

```java
Optional<Vehiculo> findByPatente(String patente);

boolean existsByPatente(String patente);
```

`existsByPatente()` se utiliza para evitar patentes duplicadas.

## `VehiculoService.java`

Contiene la lógica de negocio:

- Listar.
- Buscar por ID.
- Crear.
- Modificar.
- Dar de baja lógicamente.
- Validar patente duplicada.
- Establecer `DISPONIBLE` al crear.
- Establecer `activo = true` al crear.
- Convertir `Vehiculo` a `VehiculoResponseDTO`.

## `VehiculoController.java`

Expone los endpoints REST bajo:

```text
/vehiculos
```

Recibe las solicitudes HTTP y delega la lógica al service.

---

# 5. Base de datos MySQL

## Crear la base

Abrir MySQL:

```bash
mysql -u root -p
```

Crear:

```sql
CREATE DATABASE rentar;
```

Verificar:

```sql
SHOW DATABASES;
```

Luego:

```sql
USE rentar;
```

## ¿Hay que crear manualmente la tabla `vehiculo`?

**No.**

El proyecto utiliza:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate crea/actualiza las tablas a partir de las entidades JPA.

Después de iniciar el backend se puede comprobar:

```sql
SHOW TABLES;
```

y:

```sql
SELECT * FROM vehiculo;
```

La tabla tendrá conceptualmente:

| Columna | Tipo conceptual | Restricciones |
|---|---|---|
| `id` | BIGINT | PK, autogenerado |
| `patente` | VARCHAR | NOT NULL, UNIQUE |
| `marca` | VARCHAR | NOT NULL |
| `modelo` | VARCHAR | NOT NULL |
| `anio` | INTEGER | obligatorio |
| `color` | VARCHAR | opcional |
| `tipo` | VARCHAR | obligatorio |
| `precio_diario` | DOUBLE | mayor a 0 |
| `estado` | VARCHAR | obligatorio |
| `activo` | BOOLEAN | obligatorio |

Los enums se almacenan como texto mediante `EnumType.STRING`.

---

# 6. `application.properties`

Ubicación:

```text
backend/src/main/resources/application.properties
```

Configuración:

```properties
spring.application.name=rentar

spring.datasource.url=jdbc:mysql://localhost:3306/rentar
spring.datasource.username=root
spring.datasource.password=CAMBIAR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Cada integrante debe colocar su propia contraseña de MySQL.

**No subir contraseñas reales al repositorio.**

La conexión utiliza:

```text
MySQL: localhost:3306
Base: rentar
Usuario: root
```

---

# 7. Levantar Spring Boot

Desde:

```bash
cd backend
```

Ejecutar tests:

```bash
.\mvnw.cmd clean test
```

Debe aparecer:

```text
BUILD SUCCESS
```

Para iniciar:

```bash
.\mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8080
```

Para detenerlo:

```text
Ctrl + C
```

---

# 8. Swagger / OpenAPI

Con Spring Boot ejecutándose:

```text
http://localhost:8080/swagger-ui.html
```

Swagger permite:

- Ver endpoints.
- Ver DTOs.
- Ejecutar requests.
- Ver responses.
- Probar validaciones.

Especificación OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

---

# 9. Endpoints REST

| Método | Endpoint | Función |
|---|---|---|
| `GET` | `/vehiculos` | Listar todos |
| `GET` | `/vehiculos/{id}` | Buscar por ID |
| `POST` | `/vehiculos` | Crear |
| `PUT` | `/vehiculos/{id}` | Modificar |
| `DELETE` | `/vehiculos/{id}` | Baja lógica |

---

## 9.1 GET `/vehiculos`

Ejemplo:

```bash
curl -X GET "http://localhost:8080/vehiculos"
```

Respuesta:

```json
[
  {
    "id": 1,
    "patente": "AB456CD",
    "marca": "Ford",
    "modelo": "EcoSport",
    "anio": 2024,
    "color": "Negro",
    "tipo": "SUV",
    "precioDiario": 55000,
    "estado": "DISPONIBLE",
    "activo": true
  }
]
```

---

## 9.2 GET `/vehiculos/{id}`

Ejemplo:

```http
GET /vehiculos/1
```

Respuesta:

```json
{
  "id": 1,
  "patente": "AB456CD",
  "marca": "Ford",
  "modelo": "EcoSport",
  "anio": 2024,
  "color": "Negro",
  "tipo": "SUV",
  "precioDiario": 55000,
  "estado": "DISPONIBLE",
  "activo": true
}
```

Si no existe el ID, actualmente el endpoint devuelve `404 Not Found`.

---

## 9.3 POST `/vehiculos`

Request:

```json
{
  "patente": "AB456CD",
  "marca": "Ford",
  "modelo": "EcoSport",
  "anio": 2024,
  "color": "Negro",
  "tipo": "SUV",
  "precioDiario": 55000
}
```

El backend establece automáticamente:

```text
estado = DISPONIBLE
activo = true
```

Response exitosa:

```json
{
  "id": 1,
  "patente": "AB456CD",
  "marca": "Ford",
  "modelo": "EcoSport",
  "anio": 2024,
  "color": "Negro",
  "tipo": "SUV",
  "precioDiario": 55000,
  "estado": "DISPONIBLE",
  "activo": true
}
```

Código esperado:

```text
201 Created
```

---

## 9.4 PUT `/vehiculos/{id}`

Ejemplo:

```http
PUT /vehiculos/1
```

Request:

```json
{
  "marca": "Toyota",
  "modelo": "Corolla Cross",
  "anio": 2025,
  "color": "Blanco",
  "tipo": "SUV",
  "precioDiario": 65000,
  "estado": "DISPONIBLE"
}
```

Response:

```json
{
  "id": 1,
  "patente": "AB456CD",
  "marca": "Toyota",
  "modelo": "Corolla Cross",
  "anio": 2025,
  "color": "Blanco",
  "tipo": "SUV",
  "precioDiario": 65000,
  "estado": "DISPONIBLE",
  "activo": true
}
```

La patente se mantiene porque no forma parte del `VehiculoUpdateDTO`.

---

## 9.5 DELETE `/vehiculos/{id}`

Ejemplo:

```http
DELETE /vehiculos/1
```

No elimina físicamente el registro.

Realiza:

```text
activo = false
```

Response:

```json
{
  "id": 1,
  "patente": "AB456CD",
  "marca": "Toyota",
  "modelo": "Corolla Cross",
  "anio": 2025,
  "color": "Blanco",
  "tipo": "SUV",
  "precioDiario": 65000,
  "estado": "DISPONIBLE",
  "activo": false
}
```

---

# 10. Reglas de negocio

### Alta

Al crear:

```text
activo = true
estado = DISPONIBLE
```

### Patente

- Obligatoria.
- Única.
- No modificable.

### Precio

Debe ser mayor que cero.

### Tipo

Solo:

```text
SEDAN
SUV
PICKUP
COUPE
HATCHBACK
```

### Estado

Solo:

```text
DISPONIBLE
RESERVADO
EN_ALQUILER
```

### Baja

Es lógica:

```text
activo = false
```

El registro permanece en MySQL.

---

# 11. Arquitectura

El módulo sigue:

```text
Frontend / HTTP
      ↓
VehiculoController
      ↓
DTO
      ↓
VehiculoService
      ↓
VehiculoRepository
      ↓
Hibernate / JPA
      ↓
MySQL
```

## Controller

Recibe las solicitudes HTTP y delega al service.

## DTO

Define los datos de entrada/salida:

```text
VehiculoCreateDTO
VehiculoUpdateDTO
VehiculoResponseDTO
```

## Service

Contiene las reglas de negocio.

## Repository

Realiza el acceso a datos mediante Spring Data JPA.

## Hibernate/JPA

Traduce las operaciones Java a operaciones SQL.

## MySQL

Almacena los datos.

---

# 12. Flujo de un alta

```text
React
  │
  │ POST /vehiculos
  ▼
VehiculoController
  │
  │ VehiculoCreateDTO
  ▼
VehiculoService
  │
  ├── valida patente
  ├── crea Vehiculo
  ├── estado = DISPONIBLE
  └── activo = true
  │
  ▼
VehiculoRepository
  │
  ▼
Hibernate / JPA
  │
  ▼
MySQL
  │
  ▼
VehiculoResponseDTO
  │
  ▼
React
```

---

# 13. Consumo desde React

React **no debe conectarse directamente a MySQL**.

La comunicación es:

```text
React
  ↓ HTTP
Spring Boot REST API
  ↓ JPA/Hibernate
MySQL
```

## Listar

```javascript
fetch("http://localhost:8080/vehiculos")
  .then(response => response.json())
  .then(data => {
    console.log(data);
  });
```

## Crear

```javascript
fetch("http://localhost:8080/vehiculos", {
  method: "POST",
  headers: {
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    patente: "AB456CD",
    marca: "Ford",
    modelo: "EcoSport",
    anio: 2024,
    color: "Negro",
    tipo: "SUV",
    precioDiario: 55000
  })
});
```

## Modificar

```javascript
fetch("http://localhost:8080/vehiculos/1", {
  method: "PUT",
  headers: {
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    marca: "Toyota",
    modelo: "Corolla Cross",
    anio: 2025,
    color: "Blanco",
    tipo: "SUV",
    precioDiario: 65000,
    estado: "DISPONIBLE"
  })
});
```

## Baja lógica

```javascript
fetch("http://localhost:8080/vehiculos/1", {
  method: "DELETE"
});
```

---

# 14. Estructura esperada del Frontend

El frontend actual es React + Vite:

```text
frontend/
├── public/
├── src/
│   ├── assets/
│   ├── App.jsx
│   ├── App.css
│   ├── index.css
│   └── main.jsx
├── eslint.config.js
├── index.html
├── package.json
├── package-lock.json
└── vite.config.js
```

Para organizar posteriormente Vehículos se recomienda:

```text
frontend/
└── src/
    ├── assets/
    │
    ├── components/
    │   └── vehiculos/
    │       ├── VehiculoForm.jsx
    │       ├── VehiculoTable.jsx
    │       └── VehiculoRow.jsx
    │
    ├── pages/
    │   └── vehiculos/
    │       └── VehiculosPage.jsx
    │
    ├── services/
    │   └── vehiculosService.js
    │
    ├── App.jsx
    ├── App.css
    ├── index.css
    └── main.jsx
```

**Importante:** esta organización es la estructura esperada para ordenar la funcionalidad; no significa que todos esos archivos ya estén implementados.

### `vehiculosService.js`

Centralizará:

```text
GET /vehiculos
GET /vehiculos/{id}
POST /vehiculos
PUT /vehiculos/{id}
DELETE /vehiculos/{id}
```

### `VehiculosPage.jsx`

Pantalla principal del ABM.

### `VehiculoForm.jsx`

Formulario de alta/modificación.

La patente debe ser editable solamente durante el alta.

### `VehiculoTable.jsx`

Listado de vehículos.

### `VehiculoRow.jsx`

Representación de cada vehículo.

---

# 15. Pantalla esperada

La interfaz administrativa debería permitir:

```text
┌───────────────────────────────────────────────┐
│                 VEHÍCULOS                     │
├───────────────────────────────────────────────┤
│ [ + Nuevo vehículo ]                          │
│                                               │
│ Patente | Marca | Modelo | Tipo | Precio      │
│------------------------------------------------│
│ AB456CD | Ford  | Eco... | SUV  | $55.000     │
│                         [Editar] [Dar de baja]│
└───────────────────────────────────────────────┘
```

El frontend no debe implementar una eliminación física.

Al seleccionar "Dar de baja":

```text
DELETE /vehiculos/{id}
```

y el backend establece:

```text
activo = false
```

---

# 16. Levantar el Frontend

Desde:

```bash
cd frontend
```

Instalar dependencias:

```bash
npm install
```

Ejecutar:

```bash
npm run dev
```

Vite mostrará una dirección similar a:

```text
http://localhost:5173/
```

---

# 17. CORS

Durante el desarrollo:

```text
Frontend → http://localhost:5173
Backend  → http://localhost:8080
```

Son orígenes diferentes.

Si el navegador bloquea las llamadas desde React por CORS, se deberá configurar CORS en Spring Boot para permitir el origen del frontend.

React nunca debe conectarse directamente a MySQL.

---


# 18. Prueba completa desde cero

### 1. Crear base

```sql
CREATE DATABASE rentar;
```

### 2. Configurar contraseña

Editar:

```text
backend/src/main/resources/application.properties
```

### 3. Ejecutar tests

```bash
cd backend
.\mvnw.cmd clean test
```

Esperar:

```text
BUILD SUCCESS
```

### 4. Levantar backend

```bash
.\mvnw.cmd spring-boot:run
```

### 5. Abrir Swagger

```text
http://localhost:8080/swagger-ui.html
```

### 6. Crear

```text
POST /vehiculos
```

### 7. Consultar

```text
GET /vehiculos
```

### 8. Buscar

```text
GET /vehiculos/{id}
```

### 9. Modificar

```text
PUT /vehiculos/{id}
```

Verificar que la patente no cambie.

### 10. Dar de baja

```text
DELETE /vehiculos/{id}
```

Verificar:

```json
"activo": false
```

### 11. Consultar nuevamente

```text
GET /vehiculos/{id}
```

El registro debe seguir existiendo con:

```json
"activo": false
```

---