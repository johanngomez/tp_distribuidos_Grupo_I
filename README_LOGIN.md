# Rentar — Login y autenticación

> Proyecto: TP Desarrollo de Software en Sistemas Distribuidos — Grupo I, UNLa

## Objetivo

Rentar autentica a sus clientes con email y contraseña. Cuando las credenciales son válidas, el backend entrega un JWT firmado. El token identifica al cliente y contiene si posee permisos de administrador.


## Tecnologías

| Tecnología | Responsabilidad |
| --- | --- |
| Java 17 / Spring Boot | API y lógica de autenticación |
| Spring Data JPA / Hibernate | Persistencia de clientes en MySQL |
| Spring Security | Protección de endpoints y validación de JWT |
| BCrypt | Hash y verificación de contraseñas |
| JWT / HS256 | Token de sesión sin estado |
| React / Vite | Formulario de inicio de sesión |

## Arquitectura y flujo

```text
React (Login.jsx)
        |
        | POST /auth/login { email, password }
        v
AuthController -> AuthService -> ClienteRepository -> MySQL: cliente
                         |              |
                         |              +-- busca el cliente por email
                         +-- verifica activo, contraseña BCrypt y esAdmin
        |
        v
JWT HS256 { sub: email, esAdmin: boolean }
        |
        v
React guarda token y esAdmin en localStorage
```

El endpoint de login es público. El resto de los endpoints requiere un token JWT válido según la configuración actual de Spring Security.

## Modelo `Cliente`

Además de los datos personales, el modelo contiene los siguientes campos relacionados con autenticación:

| Campo Java | Columna MySQL | Uso |
| --- | --- | --- |
| `password` | `password` | Hash BCrypt de la contraseña. Nunca se guarda la contraseña en texto plano. |
| `activo` | `activo` | Si es `false`, el cliente no puede iniciar sesión. |
| `esAdmin` | `es_admin` | Indica si el cliente tiene permisos administrativos. |

Un cliente creado a través de `POST /api/clientes` siempre comienza con `esAdmin = false`; ese valor no se acepta desde el DTO de creación. Así se evita que un alta común se otorgue privilegios administrativos.

## Administrador de prueba

Al iniciar el backend, `ClienteInicializador` verifica que exista el administrador de desarrollo. Si no existe, lo crea. Si existe sin contraseña, le asigna una contraseña BCrypt válida.

```text
Email:       admin@rentar.com
Contraseña:  123456
esAdmin:     true
activo:      true
```

El valor `123456` es exclusivamente para desarrollo y pruebas. En un sistema productivo debe reemplazarse por un mecanismo seguro de alta y recuperación de contraseña.

## API de login

### `POST /auth/login`

Recibe las credenciales y devuelve un JWT si el cliente existe, está activo y la contraseña coincide con el hash guardado.

Request:

```json
{
  "email": "admin@rentar.com",
  "password": "123456"
}
```

Response:

```json
{
  "token": "eyJ...",
  "tipo": "Bearer",
  "esAdmin": true
}
```

| Campo | Descripción |
| --- | --- |
| `token` | JWT que debe enviarse en las peticiones protegidas. |
| `tipo` | Tipo de esquema de autenticación: `Bearer`. |
| `esAdmin` | `true` si el cliente autenticado es administrador. |

Errores esperables:

| Situación | Resultado |
| --- | --- |
| Email inexistente | Credenciales inválidas. |
| Contraseña incorrecta | Credenciales inválidas. |
| Cliente inactivo | El cliente no puede iniciar sesión. |

## JWT

Después de validar las credenciales, `AuthService` crea un JWT con una vigencia de dos horas.

Payload conceptual:

```json
{
  "iss": "rentar",
  "sub": "admin@rentar.com",
  "iat": 1788964187,
  "exp": 1788971387,
  "esAdmin": true
}
```

| Claim | Significado |
| --- | --- |
| `iss` | Emisor: `rentar`. |
| `sub` | Email del cliente autenticado. |
| `iat` | Instante de emisión. |
| `exp` | Instante de expiración. |
| `esAdmin` | Permiso administrativo del cliente. |

El token usa el algoritmo HS256 y la clave definida por `jwt.secret` en `application.properties`. El frontend no debe confiar únicamente en `esAdmin` de `localStorage` para proteger operaciones sensibles: la autorización final siempre debe verificarse en el backend.

## Backend: clases involucradas

```text
backend/src/main/java/ar/unla/rentar/
├── config/
│   ├── ClienteInicializador.java
│   ├── JwtConfig.java
│   └── SecurityConfig.java
├── controller/AuthController.java
├── dto/
│   ├── LoginRequestDTO.java
│   └── LoginResponseDTO.java
├── model/Cliente.java
├── repository/ClienteRepository.java
└── service/
    ├── AuthService.java
    └── ClienteService.java
```

| Clase | Responsabilidad |
| --- | --- |
| `AuthController` | Expone `POST /auth/login`. |
| `AuthService` | Busca el cliente, valida su estado y contraseña, y firma el JWT. |
| `ClienteRepository` | Busca clientes por email y documento en MySQL. |
| `ClienteInicializador` | Garantiza el administrador de prueba para un entorno de desarrollo. |
| `JwtConfig` | Configura el encoder y decoder JWT con HS256. |
| `SecurityConfig` | Define CORS, sesiones stateless, endpoints públicos y JWT. |
| `ClienteService` | Al crear un cliente, hashea su contraseña y le asigna `esAdmin = false`. |

## Frontend

Los archivos vinculados son:

```text
frontend/src/
├── components/Login.jsx
└── services/authService.js
```

Luego de un login exitoso, `Login.jsx` almacena:

```javascript
localStorage.setItem("token", data.token);
localStorage.setItem("esAdmin", String(data.esAdmin));
```

Para llamar a un endpoint protegido, las futuras pantallas deben enviar el token:

```javascript
fetch("http://localhost:8080/api/recurso", {
  headers: {
    Authorization: `Bearer ${localStorage.getItem("token")}`
  }
});
```

## Base de datos

El script [`script_rentar_V2.sql`](./script_rentar_V2.sql) crea las tablas `cliente`, `vehiculo` y `reserva`, junto con datos de ejemplo. Las claves primarias y foráneas usan `BIGINT`, igual que los IDs `Long` de las entidades Java.

La tabla `cliente` incluye `password` y `es_admin`. El script deja la contraseña del admin vacía para no versionar un hash de ejemplo; `ClienteInicializador` la reemplaza por el hash BCrypt de `123456` cuando el backend inicia.

Cada integrante trabaja con su propia base MySQL local. Git comparte el esquema y el código, pero no los datos que cada persona modifique después de importar el script.

## Configuración local

El archivo `backend/src/main/resources/application.properties` obtiene la conexión desde variables de entorno:

```properties
spring.datasource.url=jdbc:mysql://localhost:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

Antes de iniciar el backend, configurarlas en la misma terminal de PowerShell:

```powershell
$env:DB_PORT = "3306"
$env:DB_NAME = "rentar"
$env:DB_USER = "root"
$env:DB_PASSWORD = 'TU_CONTRASEÑA_MYSQL'
```

No se deben subir credenciales reales de MySQL ni cambiar `jwt.secret` por un secreto productivo dentro de un repositorio público.

## Inicio desde cero

### 1. Crear e importar MySQL

Desde la raíz del repositorio:

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS rentar;"
Get-Content .\script_rentar_V2.sql | mysql -u root -p rentar
```

> El script recrea sus tablas y datos de ejemplo. No ejecutarlo sobre una base con información que se quiera conservar.

### 2. Levantar backend

```powershell
cd E:\TpDistribuidos\backend

$env:DB_PORT = "3306"
$env:DB_NAME = "rentar"
$env:DB_USER = "root"
$env:DB_PASSWORD = 'TU_CONTRASEÑA_MYSQL'

.\mvnw.cmd spring-boot:run
```

Backend: `http://localhost:8080`

### 3. Levantar frontend

En una segunda terminal:

```powershell
cd E:\TpDistribuidos\frontend
npm install
npm run dev
```

Frontend: normalmente `http://localhost:5173`

Si NVM informa que no puede confiar en el ejecutable de npm, ejecutar una vez:

```powershell
nvm reshim
```

## Pruebas manuales

### Desde el navegador

Abrir `http://localhost:5173` y usar las credenciales del administrador de prueba.

### Desde PowerShell

```powershell
$body = @{
  email = "admin@rentar.com"
  password = "123456"
} | ConvertTo-Json -Compress

Invoke-RestMethod `
  -Uri "http://localhost:8080/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

La respuesta debe contener un `token`, `tipo: Bearer` y `esAdmin: true`.

## Seguridad actual y próximos pasos

Implementado:

- Login contra clientes persistidos en MySQL.
- Contraseñas almacenadas como hashes BCrypt.
- Cliente activo/inactivo.
- Indicador `esAdmin` persistido, devuelto por el login e incluido en el JWT.
- JWT HS256 con vencimiento de dos horas.
- CORS habilitado para `http://localhost:5173`.
