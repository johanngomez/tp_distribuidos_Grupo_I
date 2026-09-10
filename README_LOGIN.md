# 🔐 Rentar — Login y Autenticación

> **Rama:** `feature/creamoslogin`  
> **Proyecto:** TP Desarrollo de Software en Sistemas Distribuidos — Grupo I  
> **Universidad:** Universidad Nacional de Lanús (UNLa)  
> **Funcionalidad:** Login + autenticación JWT + roles  
> **Estado:** Funcional y probado localmente

---

# 1. Descripción

Esta rama incorpora la primera versión funcional del sistema de autenticación de **Rentar**, el sistema web de alquiler de vehículos del Trabajo Práctico de Desarrollo de Software en Sistemas Distribuidos.

La implementación agrega un flujo de login entre:

- **Frontend:** React + Vite.
- **Backend:** Java + Spring Boot.
- **Seguridad:** Spring Security.
- **Autenticación:** JWT.
- **Firma del token:** HS256.
- **Contraseñas:** BCrypt.
- **Roles:** `ADMINISTRADOR` y `CLIENTE`.
- **Persistencia actual del usuario:** temporal, sin conexión definitiva a la tabla de usuarios.

El objetivo de esta versión es dejar disponible una base de autenticación funcional que posteriormente pueda integrarse con el modelo definitivo de usuarios/clientes y con las diferentes pantallas del sistema.

---

# 2. Tecnologías utilizadas

## Backend

| Tecnología | Uso |
|---|---|
| Java 17 | Lenguaje principal |
| Spring Boot 4.1.1 | Framework backend |
| Maven | Gestión y construcción |
| Spring Security | Seguridad y autenticación |
| Spring Security OAuth2 Resource Server | Validación de JWT |
| Nimbus JOSE JWT | Firma y procesamiento JWT |
| BCrypt | Hash y validación de contraseñas |
| Spring Web MVC | API REST |
| MySQL 8.4 | Base de datos del sistema |
| Spring Data JPA / Hibernate | Persistencia |
| Spring Validation | Validaciones |
| SpringDoc OpenAPI / Swagger | Documentación de API |

## Frontend

| Tecnología | Uso |
|---|---|
| React | Interfaz de usuario |
| Vite | Servidor de desarrollo y build |
| JavaScript | Lenguaje |
| HTML/CSS | Interfaz del login |
| Fetch API | Comunicación con backend |
| localStorage | Almacenamiento temporal del token y rol |

---

# 3. Arquitectura del Login

```text
┌───────────────────────────────┐
│          FRONTEND             │
│       React + Vite            │
│                               │
│  Email + Contraseña            │
└───────────────┬───────────────┘
                │
                │ POST /auth/login
                ▼
┌───────────────────────────────┐
│           BACKEND             │
│        Spring Boot            │
│                               │
│       AuthController          │
│             ↓                 │
│         AuthService           │
│             ↓                 │
│    UsuarioRepository          │
└───────────────┬───────────────┘
                │
                │ valida contraseña
                │ mediante BCrypt
                ▼
┌───────────────────────────────┐
│        Generación JWT         │
│                               │
│        HS256 + secreto        │
│                               │
│  sub = email                  │
│  rol = ADMINISTRADOR/CLIENTE  │
└───────────────┬───────────────┘
                │
                │ JWT
                ▼
┌───────────────────────────────┐
│          FRONTEND             │
│                               │
│ localStorage                  │
│  ├── token                    │
│  └── rol                      │
└───────────────────────────────┘
```

---

# 4. Endpoint de Login

## POST `/auth/login`

Este endpoint recibe las credenciales del usuario y devuelve un JWT cuando las credenciales son válidas.

### Request

```json
{
  "email": "admin@rentar.com",
  "password": "123456"
}
```

### Response

```json
{
  "token": "eyJ...",
  "tipo": "Bearer",
  "rol": "ADMINISTRADOR"
}
```

### Campos de la respuesta

| Campo | Descripción |
|---|---|
| `token` | JWT utilizado para autenticar futuras peticiones |
| `tipo` | Tipo de autenticación. Actualmente `Bearer` |
| `rol` | Rol del usuario autenticado |

---

# 5. Usuario temporal actual

Para poder desarrollar y probar el login antes de tener implementado el modelo definitivo de usuarios, esta versión utiliza un usuario temporal.

### Credenciales de prueba

```text
Email:      admin@rentar.com
Contraseña: 123456
Rol:        ADMINISTRADOR
Estado:     ACTIVO
```

Este usuario permite comprobar el flujo:

```text
Formulario
    ↓
POST /auth/login
    ↓
Validación
    ↓
JWT
    ↓
Frontend
```

> ⚠️ Este usuario es solamente de desarrollo. No debe considerarse el modelo definitivo de usuarios del sistema.

---

# 6. Persistencia temporal del usuario

Actualmente el login utiliza un `UsuarioRepository` temporal.

La estructura utilizada es:

```text
Usuario
├── id
├── email
├── password
├── rol
└── activo
```

Los roles disponibles son:

```text
ADMINISTRADOR
CLIENTE
```

La contraseña no se compara directamente en texto plano. Se utiliza:

```text
BCryptPasswordEncoder
```

para validar la contraseña.

---

# 7. Integración futura con Usuarios y MySQL

## Estado actual

```text
React
  ↓
POST /auth/login
  ↓
AuthService
  ↓
UsuarioRepository temporal
  ↓
Usuario de prueba
  ↓
JWT
```

## Estado esperado

```text
React
  ↓
POST /auth/login
  ↓
AuthService
  ↓
UsuarioRepository
  ↓
MySQL
  ↓
Tabla de usuarios/clientes
  ↓
BCrypt
  ↓
JWT
```

El `UsuarioRepository` temporal deberá eliminarse y reemplazarse por un repositorio persistente.

La información del usuario deberá provenir del modelo definitivo acordado por el grupo.

---

# 8. Roles

Actualmente se definieron dos roles:

```text
ADMINISTRADOR
CLIENTE
```

## ADMINISTRADOR

Posteriormente tendrá acceso a funcionalidades como:

- ABM de vehículos.
- ABM de clientes.
- Consulta de reservas de todos los clientes.
- Funcionalidades administrativas.

## CLIENTE

Posteriormente tendrá acceso a:

- Consulta de disponibilidad.
- Creación de reservas.
- Consulta de sus propias reservas.
- Cancelación de reservas.
- Consulta de historial.

La autorización final de cada endpoint se deberá completar cuando se integren los módulos correspondientes.

---

# 9. JWT

Después de validar las credenciales, el backend genera un JSON Web Token.

El token contiene información del usuario.

Ejemplo conceptual:

```json
{
  "iss": "rentar",
  "sub": "admin@rentar.com",
  "iat": 1788964187,
  "exp": 1788971387,
  "rol": "ADMINISTRADOR"
}
```

## Claims

| Claim | Descripción |
|---|---|
| `iss` | Emisor del token: `rentar` |
| `sub` | Usuario autenticado, actualmente el email |
| `iat` | Fecha/hora de emisión |
| `exp` | Fecha/hora de expiración |
| `rol` | Rol del usuario |

---

# 10. Algoritmo utilizado

El JWT se firma mediante:

```text
HS256
```

Además se utiliza un identificador de clave:

```text
rentar-key
```

Header conceptual:

```json
{
  "kid": "rentar-key",
  "alg": "HS256"
}
```

---

# 11. Duración del JWT

Actualmente el token tiene una duración de:

```text
2 horas
```

Una vez expirado, el token deja de ser válido.

En una implementación posterior puede agregarse:

- Renovación de sesión.
- Refresh token.
- Redirección automática al login.
- Logout.
- Manejo de expiración desde React.

---

# 12. Spring Security

La aplicación utiliza Spring Security para proteger los recursos.

La configuración establece:

- `/auth/**` → acceso público.
- Swagger/OpenAPI → acceso público.
- Resto de endpoints → requieren autenticación.
- Sesiones HTTP → `STATELESS`.
- CSRF → deshabilitado para la API.
- JWT → utilizado como mecanismo de autenticación.
- CORS → habilitado para la comunicación con React.

Flujo:

```text
POST /auth/login
        ↓
     público
        ↓
      JWT
        ↓
Authorization: Bearer <token>
        ↓
Endpoint protegido
```

---

# 13. CORS

Durante el desarrollo:

```text
Frontend → localhost:5173
Backend  → localhost:8080
```

Como son orígenes diferentes, se configuró CORS.

Actualmente se permite:

```text
http://localhost:5173
```

Métodos habilitados:

```text
GET
POST
PUT
DELETE
OPTIONS
```

Si Vite cambia de puerto, deberá actualizarse la configuración de CORS.

---

# 14. Estructura del Backend relacionada con Login

```text
backend/
└── src/
    └── main/
        └── java/
            └── ar/
                └── unla/
                    └── rentar/
                        ├── config/
                        │   ├── SecurityConfig.java
                        │   └── JwtConfig.java
                        │
                        ├── controller/
                        │   └── AuthController.java
                        │
                        ├── dto/
                        │   ├── LoginRequestDTO.java
                        │   └── LoginResponseDTO.java
                        │
                        ├── model/
                        │   ├── Usuario.java
                        │   └── Rol.java
                        │
                        ├── repository/
                        │   └── UsuarioRepository.java
                        │
                        └── service/
                            └── AuthService.java
```

---

# 15. Responsabilidad de las clases

### `AuthController`

Expone:

```text
POST /auth/login
```

Recibe `LoginRequestDTO` y devuelve `LoginResponseDTO`.

### `AuthService`

Contiene la lógica:

1. Buscar usuario.
2. Verificar que exista.
3. Verificar que esté activo.
4. Comparar contraseña con BCrypt.
5. Crear claims JWT.
6. Firmar JWT.
7. Devolver respuesta.

### `UsuarioRepository`

Actualmente es temporal. Su responsabilidad futura será buscar usuarios reales en MySQL.

### `Usuario`

Modelo temporal:

```text
id
email
password
rol
activo
```

### `Rol`

Enum:

```java
ADMINISTRADOR
CLIENTE
```

### `LoginRequestDTO`

Representa:

```json
{
  "email": "...",
  "password": "..."
}
```

### `LoginResponseDTO`

Representa:

```json
{
  "token": "...",
  "tipo": "Bearer",
  "rol": "ADMINISTRADOR"
}
```

### `JwtConfig`

Configura:

- Clave de firma.
- Algoritmo HS256.
- `JwtEncoder`.
- `JwtDecoder`.
- Clave `rentar-key`.

### `SecurityConfig`

Configura:

- Spring Security.
- CORS.
- CSRF.
- Sesiones.
- Endpoints públicos.
- Endpoints protegidos.
- JWT Resource Server.

---

# 16. Frontend del Login

La funcionalidad utiliza:

```text
frontend/
└── src/
    ├── components/
    │   └── Login.jsx
    │
    ├── services/
    │   └── authService.js
    │
    ├── App.jsx
    ├── App.css
    ├── index.css
    └── main.jsx
```

---

# 17. `Login.jsx`

El componente se encarga de:

- Mostrar el formulario.
- Capturar email.
- Capturar contraseña.
- Mostrar errores.
- Mostrar estado de carga.
- Llamar al servicio de autenticación.
- Guardar el JWT.
- Guardar el rol.

Flujo:

```text
Usuario completa formulario
          ↓
      Ingresar
          ↓
     authService
          ↓
 POST /auth/login
          ↓
       Backend
          ↓
       JWT + rol
          ↓
     localStorage
```

---

# 18. `authService.js`

El archivo:

```text
frontend/src/services/authService.js
```

centraliza la comunicación con el backend.

Endpoint:

```text
http://localhost:8080/auth/login
```

Realiza:

```http
POST /auth/login
```

enviando:

```json
{
  "email": "...",
  "password": "..."
}
```

---

# 19. Almacenamiento del token

Después de un login exitoso, React guarda:

```text
localStorage
├── token
└── rol
```

Conceptualmente:

```javascript
localStorage.setItem("token", data.token);
localStorage.setItem("rol", data.rol);
```

Esto permitirá que las futuras pantallas recuperen el token.

---

# 20. Uso del JWT en futuras peticiones

Las peticiones protegidas deberán enviar:

```http
Authorization: Bearer <JWT>
```

Ejemplo:

```javascript
fetch("http://localhost:8080/endpoint", {
    headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
    }
});
```

El endpoint concreto dependerá de los módulos implementados por el equipo.

---

# 21. Autenticación vs autorización

## Autenticación

Responde:

> ¿Quién es el usuario?

```text
Email + contraseña
       ↓
     JWT
```

## Autorización

Responde:

> ¿Qué puede hacer ese usuario?

```text
ADMINISTRADOR
CLIENTE
```

La seguridad definitiva debe validarse en el backend y no depender únicamente de ocultar botones en React.

---

# 22. Cómo levantar el Backend

Desde:

```text
E:\TpDistribuidos\backend
```

ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

También puede utilizarse:

```powershell
mvn spring-boot:run
```

si Maven está instalado.

El backend se ejecuta en:

```text
http://localhost:8080
```

Para detenerlo:

```text
Ctrl + C
```

---

# 23. Configuración de MySQL

El proyecto utiliza:

```text
MySQL 8.4
```

Base:

```text
rentar
```

Crear la base:

```sql
CREATE DATABASE rentar;
```

> La persistencia real del usuario todavía queda pendiente de integración.

---

# 24. Configuración de `application.properties`

Archivo:

```text
backend/src/main/resources/application.properties
```

Configuración conceptual:

```properties
spring.application.name=rentar

spring.datasource.url=jdbc:mysql://localhost:3306/rentar
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

jwt.secret=TU_JWT_SECRET
```

Cada desarrollador debe configurar sus propios valores locales.

> ⚠️ No subir contraseñas reales de MySQL al repositorio público.

> ⚠️ No publicar el secreto real utilizado para firmar JWT.

---

# 25. Levantar el Frontend

Abrir una segunda terminal.

Desde:

```text
E:\TpDistribuidos\frontend
```

instalar dependencias:

```powershell
npm install
```

Luego:

```powershell
npm run dev
```

Vite mostrará normalmente:

```text
http://localhost:5173
```

---

# 26. Ejecutar todo el sistema

Se necesitan dos terminales.

## Terminal 1 — Backend

```powershell
cd E:\TpDistribuidos\backend
.\mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8080
```

## Terminal 2 — Frontend

```powershell
cd E:\TpDistribuidos\frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

# 27. Abrir el Login

Con ambos procesos ejecutándose:

```text
http://localhost:5173
```

Utilizar:

```text
Email:      admin@rentar.com
Contraseña: 123456
```

Presionar:

```text
Ingresar
```

---

# 28. Resultado esperado

El flujo completo es:

```text
React
  ↓
POST /auth/login
  ↓
Spring Boot
  ↓
Validación BCrypt
  ↓
JWT HS256
  ↓
Response
  ↓
React
  ↓
localStorage
```

Respuesta:

```json
{
  "token": "eyJ...",
  "tipo": "Bearer",
  "rol": "ADMINISTRADOR"
}
```

El frontend guarda:

```text
token
rol
```

La prueba funcional realizada durante el desarrollo confirmó:

```text
Bienvenido. Rol: ADMINISTRADOR
```

---

# 29. Prueba manual con PowerShell

También se puede probar el backend sin React:

```powershell
$body = @{
    email = "admin@rentar.com"
    password = "123456"
} | ConvertTo-Json -Compress

$response = Invoke-RestMethod `
    -Uri "http://localhost:8080/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

$response
```

Respuesta esperada:

```text
token : eyJ...
tipo  : Bearer
rol   : ADMINISTRADOR
```

---

# 30. Problemas frecuentes

## `Failed to fetch`

Verificar:

```text
Backend → localhost:8080
Frontend → localhost:5173
```

También verificar CORS.

## Error 403

Verificar:

- `/auth/**` permitido.
- Backend iniciado.
- Configuración de Spring Security.
- JWT válido en endpoints protegidos.

## Error 401

Puede significar:

- No se envió JWT.
- JWT expiró.
- JWT inválido.
- Firma incorrecta.

Formato correcto:

```http
Authorization: Bearer <token>
```

## Error de conexión con MySQL

Verificar:

- MySQL ejecutándose.
- Puerto 3306.
- Base `rentar`.
- Usuario correcto.
- Contraseña correcta.

## Error de Maven

Verificar:

```powershell
java -version
```

Debe utilizarse Java 17.

Preferentemente:

```powershell
.\mvnw.cmd spring-boot:run
```

## Error de npm

Verificar:

```powershell
node -v
npm -v
```

Luego:

```powershell
npm install
npm run dev
```

---

# 31. Estado actual

## Implementado

- [x] Pantalla de login.
- [x] Campo email.
- [x] Campo contraseña.
- [x] Validación básica del formulario.
- [x] Endpoint `POST /auth/login`.
- [x] Validación de usuario.
- [x] Validación de usuario activo.
- [x] BCrypt.
- [x] Generación de JWT.
- [x] Firma HS256.
- [x] Claim de rol.
- [x] Spring Security.
- [x] JWT Resource Server.
- [x] JWT Decoder.
- [x] JWT Encoder.
- [x] CORS.
- [x] Comunicación React → Spring Boot.
- [x] Almacenamiento del token.
- [x] Almacenamiento del rol.
- [x] Usuario administrador temporal.
- [x] Prueba funcional end-to-end.

## Pendiente

- [ ] Reemplazar `UsuarioRepository` temporal por persistencia real.
- [ ] Crear/integrar entidad definitiva de usuario.
- [ ] Conectar usuarios con MySQL.
- [ ] Integrar usuarios con el modelo definitivo de Cliente.
- [ ] Definir definitivamente la relación Usuario ↔ Cliente.
- [ ] Configurar autorización específica por roles.
- [ ] Proteger endpoints según `ADMINISTRADOR` o `CLIENTE`.
- [ ] Integrar login con el router/navegación general de React.
- [ ] Implementar panel de administrador.
- [ ] Implementar panel de cliente.
- [ ] Implementar logout.
- [ ] Manejar expiración del JWT.
- [ ] Mover secretos a variables de entorno.
- [ ] Agregar pruebas específicas de autenticación.
- [ ] Integrar con vehículos, clientes y reservas.

---

# 32. Integración con el Frontend general

El login desarrollado en esta rama constituye la base de autenticación.

Actualmente se obtiene:

```javascript
data.token
data.tipo
data.rol
```

El frontend general podrá utilizar el rol para decidir qué interfaz mostrar:

```javascript
if (data.rol === "ADMINISTRADOR") {
    // Panel administrador
}

if (data.rol === "CLIENTE") {
    // Panel cliente
}
```

La implementación de las pantallas de administrador y cliente corresponde a la integración posterior con el frontend general.

---

# 33. Integración futura con el sistema completo

```text
                         ┌───────────────┐
                         │    LOGIN      │
                         └───────┬───────┘
                                 │
                         JWT + ROL
                                 │
                ┌────────────────┴────────────────┐
                │                                 │
                ▼                                 ▼
        ADMINISTRADOR                         CLIENTE
                │                                 │
        ┌───────┼───────┐                 ┌───────┼───────┐
        ▼       ▼       ▼                 ▼       ▼       ▼
    Vehículos Clientes Reservas       Dispon. Reservas Historial
```

Los permisos deberán ser validados en el backend.

---

# 35. Checklist para otro integrante

```text
[ ] Clonar el repositorio
[ ] Cambiar a feature/creamoslogin
[ ] Tener Java 17
[ ] Tener Maven
[ ] Tener Node.js
[ ] Tener npm
[ ] Tener MySQL
[ ] Crear base rentar
[ ] Configurar application.properties
[ ] Configurar jwt.secret
[ ] Levantar backend
[ ] Levantar frontend
[ ] Abrir localhost:5173
[ ] Usar admin@rentar.com
[ ] Usar 123456
[ ] Presionar Ingresar
[ ] Verificar JWT
[ ] Verificar rol ADMINISTRADOR
```

---

# 36. Comandos rápidos

## Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

## Frontend

```powershell
cd frontend
npm install
npm run dev
```

## Tests del backend

```powershell
cd backend
.\mvnw.cmd clean test
```

---

# 37. URLs

| Servicio | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend | http://localhost:8080 |
| Login | http://localhost:8080/auth/login |
| Swagger | http://localhost:8080/swagger-ui.html |
| OpenAPI | http://localhost:8080/v3/api-docs |
| GraphQL | http://localhost:8080/graphql |

---

# 38. Resumen

La rama `feature/creamoslogin` incorpora una primera implementación funcional de autenticación para Rentar.

El flujo implementado es:

```text
Usuario
   ↓
Email + contraseña
   ↓
POST /auth/login
   ↓
Spring Security
   ↓
BCrypt
   ↓
JWT HS256
   ↓
Rol
   ↓
Frontend
   ↓
localStorage
```

Actualmente funciona con un usuario administrador temporal.

La implementación queda preparada para integrarse posteriormente con:

- Usuarios reales.
- MySQL.
- Clientes.
- Autorización por roles.
- Vehículos.
- Reservas.
- Historial.
- Panel de administrador.
- Panel de cliente.

La persistencia definitiva de usuarios y la autorización completa deberán realizarse durante la integración con el resto del sistema.
