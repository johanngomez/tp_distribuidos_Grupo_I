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

# 4. Configurar la conexión del Backend

El archivo se encuentra en:

```text
backend/src/main/resources/application.properties
```

Ejemplo:

```properties
spring.application.name=rentar

spring.datasource.url=jdbc:mysql://localhost:3306/rentar

spring.datasource.username=root

spring.datasource.password=CAMBIAR_PASSWORD ### Cada integrante debe reemplazar esto por su propia contraseña de MySQL

spring.jpa.hibernate.ddl-auto=update

spring.jpa.show-sql=true

spring.jpa.properties.hibernate.format_sql=true
```

Cada integrante debe reemplazar `CAMBIAR_PASSWORD` por su propia contraseña de MySQL.

La base de datos debe llamarse:

```text
rentar
```

y estar disponible en:

```text
localhost:3306
```

> **Seguridad:** no utilizar una contraseña real dentro del README. El valor anterior es solamente un ejemplo.

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
