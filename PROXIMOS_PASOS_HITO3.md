# Próximos pasos — Hito 3

Este documento organiza el trabajo pendiente del Hito 3 del proyecto Rentar.
El objetivo es incorporar comunicación asíncrona basada en eventos utilizando
RabbitMQ, conservando todas las funcionalidades de los Hitos 1 y 2.

La base de RabbitMQ y los contratos iniciales ya fueron preparados en la rama
feature/agregamosRabbit. Las clases esqueleto tienen comentarios TODO, pero
no implementan todavía la lógica de negocio. Cada integrante debe completar el
código de su flujo y agregar las pruebas correspondientes.

## 1. Qué cambia entre el Hito 2 y el Hito 3

El Hito 2 utiliza principalmente comunicación síncrona:


Frontend → API Gateway → gRPC → microservicio → respuesta inmediata


El Hito 3 mantiene REST, GraphQL y gRPC, pero agrega procesos asíncronos:


Microservicio → publica evento → RabbitMQ → consumidor procesa el evento


El productor no debe quedar bloqueado esperando el resultado del consumidor.
RabbitMQ se utilizará para:

- Verificar vehículos contra un registro policial simulado.
- Validar vehículos de propietarios externos.
- Solicitar mantenimiento preventivo después de confirmar una reserva.

El frontend continúa comunicándose con el API Gateway mediante REST y GraphQL.
No se conecta directamente a RabbitMQ.

## 2. Broker elegido

El grupo utilizará un único broker:


RabbitMQ 3.13 con panel de administración


Configuración local:


AMQP:        localhost:5672
Panel web:   http://localhost:15672
Usuario:     rentar
Contraseña:  rentar
Virtual host: /


El archivo de configuración es:


docker-compose.yml


## 3. Requisitos previos

Cada integrante debe tener instalado:

- Git.
- Java JDK 17.
- Maven.
- MySQL 8.
- Docker Desktop instalado e iniciado. Incluye Docker Compose v2; no hace falta
  instalar Compose por separado.
- Node.js y npm para ejecutar el frontend.

Verificar la instalación desde PowerShell:

powershell

docker compose version


Si alguno de los comandos no existe, instalar Docker Desktop desde
<https://www.docker.com/products/docker-desktop/> y reiniciarlo antes de
ejecutar RabbitMQ.

## 4. Crear las bases de datos

El Hito 2 y el Hito 3 utilizan dos bases MySQL separadas:


rentar              → Customer Service y Rental Service
rentar_vehiculos    → Vehicle Service


Desde MySQL ejecutar:

sql
CREATE DATABASE IF NOT EXISTS rentar;
CREATE DATABASE IF NOT EXISTS rentar_vehiculos;
SHOW DATABASES;


Como alternativa, se puede ejecutar script_rentar_V2.sql desde la raíz del
proyecto. El script crea rentar y rentar_vehiculos, selecciona rentar y
restaura sus tablas y datos de prueba. Vehicle Service crea sus propias tablas
en rentar_vehiculos al iniciar. Como el dump elimina y vuelve a crear las
tablas de rentar, debe utilizarse solamente cuando se quiera restaurar esa
base local.

powershell
cd E:\TpDistribuidos
Get-Content -Raw script_rentar_V2.sql | mysql -u root -p


El mismo script_rentar_V2.sql también crea la tabla de Vehicle Service y copia
los vehículos históricos a rentar_vehiculos. Por lo tanto, al instalar el
proyecto desde cero no hace falta ejecutar otro script.

Si rentar ya existe y solo se necesita actualizar Vehicle Service, se puede
ejecutar el script específico:

powershell
Get-Content -Raw scripts\seed_vehicle_service.sql | mysql -u root -p


Este segundo script conserva los IDs de los vehículos históricos para que las
reservas existentes sigan siendo válidas. Es idempotente: si una patente o un
ID ya existe en rentar_vehiculos, conserva el registro existente.

No hace falta crear manualmente las tablas. Hibernate las crea o actualiza al
iniciar cada servicio porque está configurado con ddl-auto=update.

## 5. Iniciar RabbitMQ

Desde la raíz del proyecto:

powershell
cd E:\TpDistribuidos
docker compose up -d rabbitmq
docker compose ps


Debe aparecer:


rentar-rabbitmq   Up ... (healthy)


El contenedor queda ejecutándose en segundo plano. Para detenerlo:

powershell
docker compose down


Para detenerlo y borrar también los mensajes persistidos durante el desarrollo:

powershell
docker compose down -v


Panel web:


http://localhost:15672


El valor amqp://rentar:rentar@localhost:5672 es una dirección para las
aplicaciones; no se abre en el navegador.

## 6. Exchange, colas y bindings

El exchange común es:


Nombre: rentar.events
Tipo: topic
Durable: true


Se declara automáticamente al iniciar cualquiera de los servicios Java. Las
colas también están declaradas en RabbitMqConfig.java.

| Servicio | Cola | Routing key |
|---|---|---|
| Vehicle | rentar.vehicle.verification | vehiculos.verificacion.solicitada |
| Vehicle | rentar.vehicle.verification.result | vehiculos.verificacion.resultado |
| Vehicle | rentar.external.vehicle.patent.request | vehiculos.externos.registrados |
| Vehicle | rentar.external.vehicle.completed | vehiculos.externos.validacion.completada |
| Customer | rentar.external.vehicle.ownership.request | vehiculos.externos.registrados |
| Rental | rentar.maintenance.request | reserva.confirmada |
| Rental | rentar.maintenance.result | mantenimiento.resultado |

Las dos colas vinculadas a vehiculos.externos.registrados son necesarias:
RabbitMQ entrega una copia del evento a cada cola. Si se utilizara una única
cola con dos consumidores, los mensajes se repartirían entre ellos y no se
ejecutarían las dos validaciones para cada vehículo.

## 7. Cómo verificar el exchange y las colas

Después de iniciar un servicio Java, abrir:


http://localhost:15672/#/exchanges


Debe aparecer rentar.events como exchange topic y durable.

Las colas se pueden revisar en:


http://localhost:15672/#/queues


También se puede consultar el exchange desde PowerShell:

powershell
$cred = [Convert]::ToBase64String([.Encoding]::ASCII.GetBytes("rentar:rentar"))
Invoke-RestMethod 
  -Uri "http://localhost:15672/api/exchanges/%2F/rentar.events" 
  -Headers @{ Authorization = "Basic $cred" }


## 8. Envoltorio común de los eventos

Todos los mensajes deben respetar este formato:

json
{
  "eventId": "uuid-del-evento",
  "eventType": "reserva.confirmada",
  "correlationId": "uuid-del-proceso",
  "occurredAt": "2026-10-23T18:00:00Z",
  "payload": {}
}


Reglas:

- eventId identifica un mensaje único.
- eventType indica el evento publicado.
- correlationId relaciona mensajes del mismo proceso.
- occurredAt debe estar en formato ISO-8601.
- payload contiene los datos específicos del evento.
- Los consumidores deben ser idempotentes.
- El mensaje se confirma con ack después de procesarse correctamente.
- Los errores recuperables deben poder reintentarse.

## 9. Eventos definidos

### Verificación de vehículos

Solicitud:


vehiculos.verificacion.solicitada


json
{
  "vehiculoId": 15,
  "patente": "ABC123"
}


Resultado:


vehiculos.verificacion.resultado


json
{
  "vehiculoId": 15,
  "patente": "ABC123",
  "resultado": "VALIDADO"
}


Resultados permitidos: VALIDADO, ROBADO.

Si el resultado es ROBADO, el vehículo queda inhabilitado para nuevas
operaciones. ROBADO no se agrega como estado operativo; los estados siguen
siendo DISPONIBLE y EN_ALQUILER.

### Vehículos de propietarios externos

Inicio:


vehiculos.externos.registrados


json
{
  "vehiculoExternoId": 50,
  "clienteId": 89,
  "patente": "DEF456"
}


Validación de patente:


vehiculos.externos.validacion.patente


Validación de titularidad:


vehiculos.externos.validacion.titularidad


Los resultados posibles son VALIDADO y RECHAZADO.

Si ambas validaciones son satisfactorias:


vehiculos.externos.validacion.completada


El vehículo comienza en PENDIENTE y pasa a DISPONIBLE únicamente después de
completar correctamente las dos validaciones. Si una falla, no se incorpora.

### Mantenimiento preventivo

Evento de inicio:


reserva.confirmada


json
{
  "reservaId": 150,
  "vehiculoId": 15,
  "fechaInicio": "2026-10-25T10:00:00",
  "fechaFin": "2026-10-28T10:00:00"
}


Resultado:


mantenimiento.resultado


json
{
  "reservaId": 150,
  "vehiculoId": 15,
  "resultado": "REALIZADO"
}


Resultados permitidos: REALIZADO, NO_REALIZADO.

La confirmación de la reserva no debe esperar el resultado del mantenimiento.

## 10. Archivos esqueleto preparados

Los archivos tienen comentarios TODO y no contienen todavía lógica de
negocio. Cada integrante debe completar el código dentro de estos archivos.

### Persona 2 — Verificación de vehículos


vehicle-service/src/main/java/ar/unla/rentar/vehicle/events/
├── EventEnvelope.java
├── VehiculoVerificacionSolicitada.java
└── VehiculoVerificacionResultado.java

vehicle-service/src/main/java/ar/unla/rentar/vehicle/service/
├── VehicleEventPublisher.java
├── VehicleVerificationListener.java
├── VehicleVerificationResultListener.java
└── RegistroPolicialService.java


Debe publicar la solicitud al registrar el vehículo, consumirla, consultar el
registro policial simulado, publicar el resultado y actualizar Vehiculo.

### Persona 3 — Vehículos externos


vehicle-service/src/main/java/ar/unla/rentar/vehicle/events/
├── VehiculoExternoRegistrado.java
├── VehiculoExternoValidacionPatente.java
├── VehiculoExternoValidacionTitularidad.java
└── VehiculoExternoValidacionCompletada.java

vehicle-service/src/main/java/ar/unla/rentar/vehicle/service/
├── VehiculoExternoPatentListener.java
└── VehiculoExternoCompletedListener.java

customer-service/src/main/java/ar/unla/rentar/customer/events/
├── VehiculoExternoRegistrado.java
└── VehiculoExternoValidacionTitularidad.java

customer-service/src/main/java/ar/unla/rentar/customer/service/
├── VehiculoExternoOwnershipListener.java
└── RegistroTitularidadService.java


Debe implementar las dos validaciones independientes y correlacionar sus
resultados con correlationId.

### Persona 4 — Mantenimiento preventivo


rental-service/src/main/java/ar/unla/rentar/rental/events/
├── ReservaConfirmada.java
└── MantenimientoResultado.java

rental-service/src/main/java/ar/unla/rentar/rental/service/
├── RentalEventPublisher.java
├── MantenimientoListener.java
├── MantenimientoService.java
└── MantenimientoResultListener.java


Debe publicar reserva.confirmada, procesar el mantenimiento y asociar el
resultado con la reserva.

### Persona 5 — Integración y pruebas

Debe integrar las ramas y verificar que:

- Todos usen rentar.events.
- Las routing keys sean las documentadas.
- Los payloads respeten los contratos.
- Los consumidores sean idempotentes.
- Hito 1 y Hito 2 sigan funcionando.
- El frontend continúe usando el Gateway.

## 11. Dependencias Java ya agregadas

Los tres servicios tienen:

xml
spring-boot-starter-amqp
jackson-databind


Y sus propiedades de conexión apuntan por defecto a:

properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=rentar
spring.rabbitmq.password=rentar


Si se usa otra configuración, se pueden sobrescribir con:


RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD


## 12. Levantar los servicios

RabbitMQ debe estar iniciado antes que los servicios Java.

### Vehicle Service

powershell
cd E:\TpDistribuidos\vehicle-service
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://localhost:3306/rentar_vehiculos --spring.datasource.username=root --spring.datasource.password=TU_CLAVE_MYSQL"


Puerto gRPC: 50051.

### Customer Service

powershell
cd E:\TpDistribuidos\customer-service
$env:DB_PORT="3306"
$env:DB_NAME="rentar"
$env:DB_USER="root"
$env:DB_PASSWORD="TU_CLAVE_MYSQL"
$env:JWT_SECRET="RentarClaveSecreta2026ParaJWT123456789"
mvn spring-boot:run


Puerto gRPC: 50052.

### Rental Service

powershell
cd E:\TpDistribuidos\rental-service
$env:DB_PORT="3306"
$env:DB_NAME="rentar"
$env:DB_USER="root"
$env:DB_PASSWORD="TU_CLAVE_MYSQL"
$env:CUSTOMER_GRPC_TARGET="localhost:50052"
$env:VEHICLE_GRPC_TARGET="localhost:50051"
mvn spring-boot:run


Puerto gRPC: 50053.

### API Gateway y frontend

Se levantan igual que en el Hito 2:

powershell
cd E:\TpDistribuidos\api-gateway
$env:JWT_SECRET="RentarClaveSecreta2026ParaJWT123456789"
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload


En otra terminal:

powershell
cd E:\TpDistribuidos\frontend
npm install
npm run dev


## 13. Pruebas obligatorias

### Prueba 1 — Verificación válida

1. Registrar un vehículo con una patente no denunciada.
2. Verificar que se publique vehiculos.verificacion.solicitada.
3. Verificar que el consumidor procese el mensaje.
4. Verificar el resultado VALIDADO.
5. Verificar que el vehículo pueda operar normalmente.

### Prueba 2 — Vehículo robado

1. Registrar una patente incluida en el registro policial simulado.
2. Verificar el resultado ROBADO.
3. Confirmar que el vehículo quede inhabilitado.
4. Confirmar que no se agregue ROBADO como estado operativo.

### Prueba 3 — Vehículo externo aprobado

1. Registrar un vehículo externo.
2. Confirmar el estado inicial PENDIENTE.
3. Verificar que se ejecuten las validaciones de patente y titularidad.
4. Verificar que ambas devuelvan VALIDADO.
5. Confirmar la incorporación como DISPONIBLE.

### Prueba 4 — Vehículo externo rechazado

1. Registrar un vehículo externo con patente o titularidad inválida.
2. Verificar que una validación devuelva RECHAZADO.
3. Confirmar que el vehículo no se incorpore.

### Prueba 5 — Mantenimiento no bloqueante

1. Crear y confirmar una reserva.
2. Verificar que la reserva se confirme inmediatamente.
3. Verificar la publicación de reserva.confirmada.
4. Verificar el consumo del evento de mantenimiento.
5. Verificar mantenimiento.resultado.
6. Confirmar que el resultado quede asociado a la reserva.

En cada prueba guardar capturas de la consola de RabbitMQ, logs de los
consumidores, respuesta del Gateway y resultado visible en el frontend cuando
corresponda.

## 14. Criterios para dar por terminado el Hito 3

- RabbitMQ inicia con Docker Compose.
- Las dos bases MySQL existen.
- Los tres servicios Java compilan y levantan.
- El exchange rentar.events existe.
- Las colas y bindings aparecen en RabbitMQ.
- Los tres flujos asíncronos funcionan.
- Los mensajes tienen el formato documentado.
- Se controlan duplicados y errores recuperables.
- La reserva no espera el mantenimiento.
- Un vehículo robado queda inhabilitado.
- Un vehículo externo solo pasa a DISPONIBLE con dos validaciones aprobadas.
- Hito 1 y Hito 2 continúan funcionando.
- Las pruebas tienen capturas.
- El informe identifica la tarea de cada integrante.

## 15. Trabajo con Git

Cada integrante debe trabajar en una rama propia basada en la rama que
contenga la infraestructura de RabbitMQ:

powershell
git checkout feature/agregamosRabbit
git pull
git checkout -b feature/hito3-verificacion-vehiculos


Ejemplos de ramas:


feature/hito3-rabbitmq-topologia
feature/hito3-verificacion-vehiculos
feature/hito3-vehiculos-externos
feature/hito3-mantenimiento
feature/hito3-pruebas-documentacion


Antes de hacer un commit:

powershell
git status
git diff --check


Cada integrante debe hacer commit de su tarea y abrir un Pull Request. La
integración final debe comprobar que los contratos de eventos no hayan cambiado
sin coordinación con el resto del grupo.
