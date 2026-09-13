# README — Módulo de Reservas

## 1. Objetivo

Este documento explica cómo está implementado el módulo de **Reservas** del sistema Rentar y cómo utilizarlo, levantarlo, probarlo e integrarlo con el resto del proyecto.

Corresponde al **Hito 1 — Punto 4: Alta de reserva (REST)** y **Hito 1 - Punto 6: Cancelación de reserva (REST)**

El módulo permite:
- Alta de reservas asociando `Cliente` y `Vehiculo`.
- Cálculo automático del `importeTotal` según la duración y tarifa diaria del vehículo.
- Validación de disponibilidad de vehículo evitando el solapamiento de fechas.
- Validación de coherencia de fechas (`fechaInicio` anterior a `fechaFin`).
- Estado inicial automático (`CONFIRMADA`).
- Persistencia mediante Spring Data JPA/Hibernate en MySQL.
- Documentación y pruebas mediante Swagger.

---

## 2. Tecnologías

### Backend

- **Java**: 17
- **Framework**: Spring Boot 4.1.1
- **Componentes**: Spring Web, Spring Data JPA, Spring Validation
- **ORM**: Hibernate
- **Base de Datos**: MySQL 8.4
- **Documentación**: SpringDoc OpenAPI / Swagger
- **Gestor de Dependencias**: Maven

### Frontend

- **Librería/Framework**: React
- **Herramienta de Construcción**: Vite
- **Lenguaje**: JavaScript
- **Entorno**: Node.js 22 & npm

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

##4. Archivos del módulo de Reservas

backend/src/main/java/ar/unla/rentar/
│
├── controller/
│   └── ReservaController.java
│
├── dto/
│   ├── ReservaCreateDTO.java
│   ├── ReservaResponseDTO.java
│   └── ReservaUpdateDTO.java
│
├── model/
│   ├── EstadoReserva.java
│   └── Reserva.java
│
├── repository/
│   └── ReservaRepository.java
│
├── service/
│   └── ReservaService.java
│
backend/src/test/java/ar/unla/rentar/
│
└── ReservaTest.java
```

### `Reserva.java`

Es la entidad JPA que representa una reserva.

### `Mapeo entre Atributos Java y Tabla MySQL (`reserva`)`

| Atributo Java (`Reserva.java`) | Columna MySQL (`reserva`) | Tipo de Datos SQL | Descripción / Restricciones |
| :--- | :--- | :--- | :--- |
| `Long id` | `id` | `BIGINT` | Clave Primaria (`AUTO_INCREMENT`) |
| `Cliente cliente` | `cliente_id` | `BIGINT` | Clave Foránea (`FOREIGN KEY`) hacia `cliente(id)` |
| `Vehiculo vehiculo` | `vehiculo_id` | `BIGINT` | Clave Foránea (`FOREIGN KEY`) hacia `vehiculo(id)` |
| `LocalDateTime fechaInicio` | `fecha_inicio` | `DATETIME(6)` | Fecha y hora de inicio de la reserva |
| `LocalDateTime fechaFin` | `fecha_fin` | `DATETIME(6)` | Fecha y hora de finalización de la reserva |
| `Double precioDiario` | `precio_diario` | `DOUBLE` | Tarifa diaria tomada del vehículo |
| `Double importeTotal` | `importe_total` | `DOUBLE` | Importe total calculado automáticamente |
| `EstadoReserva estado` | `estado` | `VARCHAR(20)` | Estado (`CONFIRMADA`, `CANCELADA`, `EN_CURSO`,  `FINALIZADA`) |

###ReservaCreateDTO
Se utiliza en POST /api/reservas.

{
  "clienteId": 1,
  "vehiculoId": 1,
  "fechaInicio": "2026-10-15T10:00:00",
  "fechaFin": "2026-10-18T10:00:00"
}

### `ReservaResponseDTO.java`
Representa la respuesta enviada al cliente:

{
  "id": 1,
  "cliente": { "id": 1, "nombre": "Juan Pérez", "email": "juan@email.com" },
  "vehiculo": { "id": 1, "marca": "Ford", "modelo": "EcoSport", "patente": "AB456CD" },
  "fechaInicio": "2026-10-15T10:00:00",
  "fechaFin": "2026-10-18T10:00:00",
  "precioDiario": 45000.0,
  "importeTotal": 135000.0,
  "estado": "CONFIRMADA"
}

###ReservaRepository.java
Accede a la entidad mediante Spring Data JPA.

###ReservaService.java
Contiene la lógica de negocio:

*Procesar el alta de la reserva.

*Validar existencia previa de Cliente y Vehiculo.

*Verificar que la fechaInicio sea anterior a la fechaFin.

*Validar disponibilidad ejecutando existeSolapamiento en el repository.

*Calcular el importeTotal basado en la diferencia en horas redondeada a días mediante Math.ceil.

*Asignar el estado inicial en CONFIRMADA.

*Realizar la conversión entre DTOs y Entidad.

### `ReservaController.java`
Expone el endpoint REST bajo /api/reservas. Recibe las solicitudes HTTP y delega la ejecución al service.

## 5. Base de datos MySQL
Tabla reserva
Aunque Hibernate crea la estructura automáticamente al iniciar la aplicación (spring.jpa.hibernate.ddl-auto=update), la tabla equivalente en MySQL es:

CREATE TABLE IF NOT EXISTS reserva (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    vehiculo_id BIGINT NOT NULL,
    fecha_inicio DATETIME(6) NOT NULL,
    fecha_fin DATETIME(6) NOT NULL,
    precio_diario DOUBLE NOT NULL,
    importe_total DOUBLE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_reserva_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

##6. Endpoints REST

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| POST | /api/reservas | Crear una nueva reserva | 201 Created |
| PATCH | /api/reservas/{id}/cancelar | Cancelar una reserva existente | 200 OK |

---

### `6.1 Crear Reserva (POST /api/reservas)`

**Ejemplo de Request Body:**
{
  "clienteId": 1,
  "vehiculoId": 1,
  "fechaInicio": "2026-10-15T10:00:00",
  "fechaFin": "2026-10-18T10:00:00"
}

**Ejemplo de Response (201 Created):**
{
  "id": 1,
  "cliente": { "id": 1, "nombre": "Juan Pérez" },
  "vehiculo": { "id": 1, "modelo": "EcoSport" },
  "fechaInicio": "2026-10-15T10:00:00",
  "fechaFin": "2026-10-18T10:00:00",
  "precioDiario": 45000.0,
  "importeTotal": 135000.0,
  "estado": "CONFIRMADA"
}

---

### `6.2 Cancelar Reserva (PATCH /api/reservas/{id}/cancelar)`

**Ejemplo de Response (200 OK):**
{
  "id": 1,
  "cliente": { "id": 1, "nombre": "Juan Pérez" },
  "vehiculo": { "id": 1, "modelo": "EcoSport" },
  "fechaInicio": "2026-10-15T10:00:00",
  "fechaFin": "2026-10-18T10:00:00",
  "precioDiario": 45000.0,
  "importeTotal": 135000.0,
  "estado": "CANCELADA"
}

---

## `7. Reglas de Negocio Aplicadas`

| Regla | Criterio | Acción |
| :--- | :--- | :--- |
| Cliente y Vehículo | Existencia y activo = true | Rechaza la solicitud si no existen o están inactivos |
| Fechas | fechaInicio futura y fechaFin > fechaInicio | Rechaza si el rango temporal es inválido |
| Solapamiento | Fechas libres en reservas CONFIRMADAS | Rechaza si el auto ya está reservado |
| Importe Total | Math.ceil(horas / 24.0) * precioDiario | Garantiza un cobro mínimo de 1 día |
| Cancelación | Solo si la reserva no comenzó | Cambia a CANCELADA y libera el auto |

---

## `8. Ejecución y Pruebas`

| Acción | Comando / URL |
| :--- | :--- |
| Ejecutar Backend | .\mvnw.cmd spring-boot:run |
| Swagger UI | http://localhost:8080/swagger-ui.html |