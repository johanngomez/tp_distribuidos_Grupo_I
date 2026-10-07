# Rentar API Gateway

Aplicación Python del Hito 2, desarrollada en paralelo al backend Java del Hito 1.

## Estado actual

`GET /health` responde HTTP 200 con
`{"status":"ok"}`. Indica que la aplicación HTTP responde; no comprueba
servicios gRPC ni MySQL.

La aplicación también carga y valida la configuración de los futuros clientes
gRPC. Leer estas direcciones no abre conexiones a los servicios.

Se habilitó CORS para el origen de React, configurable mediante `FRONTEND_ORIGIN`.

`GET /auth/me` valida un JWT y devuelve su email y el permiso `esAdmin`.
El login sigue en Java: el Gateway todavía no emite tokens.

La integración con gRPC, REST de los dominios y GraphQL queda para
los siguientes pasos. React sigue usando el backend Java en el puerto 8080.

## Archivos

- `app/main.py`: crea la aplicación FastAPI, configura CORS y registra sus rutas.
- `app/routes/health.py`: define el endpoint de salud.
- `app/routes/auth.py`: expone la identidad del token en `/auth/me`.
- `app/security/jwt.py`: valida firma HS256, emisor, fechas y claims del JWT.
- `app/security/dependencies.py`: exige un token válido y ofrece un control de
  administrador reutilizable para las futuras rutas.
- `app/config/settings.py`: lee el origen de React, direcciones gRPC y el tiempo de espera desde
  variables de entorno; `main.py` las guarda en `app.state.settings`.
- Los archivos `__init__.py` identifican los paquetes Python.
- `requirements.txt`: declara las versiones de FastAPI, Uvicorn y PyJWT probadas
  con Python 3.13.15. Sus dependencias transitivas no están fijadas.
- `.gitignore`: excluye el entorno virtual, cachés y el archivo `.env`.

FastAPI define la API HTTP. Uvicorn es el servidor que ejecuta la aplicación.
PyJWT verifica la firma y el contenido del token; no accede a MySQL.

## JWT: aceptar tokens del login Java

El Gateway valida el contrato observado en `AuthService.java` y `JwtConfig.java`:
algoritmo HS256, emisor `rentar`, sujeto `sub` con el email, fecha de emisión `iat`,
vencimiento `exp` y booleano `esAdmin`. Java emite tokens con dos horas de duración.
El Gateway verifica el vencimiento recibido y no lo renueva.

Para verificar la firma, ambos procesos deben tener exactamente el mismo
`JWT_SECRET`, interpretado como bytes UTF-8 (sin decodificar Base64). No guardes
la clave ni tokens en el repositorio. La configuración oculta el secreto al imprimirse.

Si la terminal del Gateway todavía no tiene la variable, antes de iniciarlo
definí `JWT_SECRET` con el mismo valor que usa tu backend Java. Este comando
contiene un marcador que debés reemplazar localmente:

```powershell
$env:JWT_SECRET = "REEMPLAZAR_POR_EL_MISMO_SECRETO_DE_JAVA"
```

Reiniciá el Gateway después de configurar la variable. Si falta la clave,
`/health` sigue disponible y una solicitud con Bearer a `/auth/me` devuelve
503: autenticación no configurada. No se usa una clave predeterminada.

### Probar desde la documentación interactiva

1. Abrí http://127.0.0.1:8000/docs.
2. Probá `GET /auth/me` sin autorizar: debe devolver 401.
3. Obtené un token vigente desde el login Java existente (`POST /auth/login`
   con JSON que contenga `email` y `password`). También podés usar el token de
   una sesión de React: herramientas del navegador, Application/Aplicación,
   Local Storage del frontend, campo `token`.
4. En `/docs`, pulsá **Authorize** y pegá solamente el token, sin `Bearer`.
5. Volvé a ejecutar `GET /auth/me`: debe responder 200 con `email` y `esAdmin`.

Swagger agrega el encabezado `Authorization: Bearer <token>` automáticamente.
Un token inválido, vencido o ausente devuelve 401 y `WWW-Authenticate: Bearer`.
El control `require_admin` devuelve 403 para un usuario válido sin permiso
de administrador; aún no hay rutas de negocio que lo utilicen.

`/auth/me` devuelve únicamente identidad declarada en un token verificado.
No reemplaza `/api/clientes/me`, que devuelve un perfil consultado en el servicio
de clientes. Tampoco comprueba cambios de rol o bajas de clientes posteriores
a la emisión del token: esa integración se acordará con Customer Service.

### Pruebas de seguridad

Desde `api-gateway/`:

```powershell
.\.venv\Scripts\python.exe -B -m unittest discover -s tests -v
```

Las pruebas usan una clave ficticia y un servidor HTTP local temporal en un puerto
libre. Cubren tokens de cliente y administrador, firma incorrecta, algoritmo no
permitido, vencimiento, emisor incorrecto, claims ausentes o inválidos, ausencia
del secreto y acceso público a `/health`. No arrancan Java ni acceden a MySQL.
Los tokens se firman con la biblioteca estándar siguiendo el formato Java;
la prueba con un token realmente emitido por Java se realiza por separado.

## Configuración de servicios internos

Los valores predeterminados son ejemplos locales, pendientes de acuerdo con
los responsables de los servicios Java:

| Variable | Valor predeterminado | Uso futuro |
|---|---|---|
| `VEHICLE_GRPC_TARGET` | `localhost:50051` | Servicio de vehículos |
| `CUSTOMER_GRPC_TARGET` | `localhost:50052` | Servicio de clientes |
| `RENTAL_GRPC_TARGET` | `localhost:50053` | Servicio de reservas |
| `GRPC_TIMEOUT_SECONDS` | `5` | Máximo de segundos por llamada gRPC |

Una variable de entorno permite cambiar un valor sin editar código. Por ejemplo,
en la misma terminal PowerShell donde se iniciará el Gateway:

```powershell
$env:VEHICLE_GRPC_TARGET = "localhost:50061"
$env:GRPC_TIMEOUT_SECONDS = "3"
```

Si no definís variables, se usan los valores de la tabla. No se carga un archivo
`.env` automáticamente. Reiniciá el Gateway después de cambiar las variables.
Las direcciones no pueden estar vacías y el tiempo debe ser finito y mayor que cero;
si no se cumplen esas condiciones, el arranque informa el error.

Para ver la configuración desde `api-gateway/`, sin conectar ningún servicio:

```powershell
.\.venv\Scripts\python.exe -c "from app.config.settings import load_settings; print(load_settings())"
```

## CORS: permitir solicitudes desde React

El navegador identifica un origen por su protocolo, host y puerto. React en
`http://localhost:5173` y el Gateway en `http://127.0.0.1:8000` tienen orígenes
distintos. CORS indica al navegador desde qué origen puede leerse la respuesta.
No sustituye la autenticación ni impide llamadas desde otros clientes HTTP.

`FRONTEND_ORIGIN` vale `http://localhost:5173` por defecto. Para cambiarlo,
definí esta variable antes de iniciar el Gateway en la misma terminal:

```powershell
$env:FRONTEND_ORIGIN = "http://localhost:5173"
```

Usá un origen exacto, sin ruta ni barra final. `http://127.0.0.1:5173` es un
origen diferente de `http://localhost:5173`. No se permiten valores vacíos ni `*`.

Se permiten los métodos GET, POST, PUT, DELETE y OPTIONS, y los encabezados
`Content-Type` y `Authorization`. No se habilitan credenciales mediante cookies:
React envía el JWT explícitamente en `Authorization`, que está permitido.
El middleware también responde las consultas previas OPTIONS (preflight)
que realiza el navegador antes de ciertas solicitudes.

Para comprobar CORS con el Gateway iniciado, pegá cada comando en una sola línea:

```powershell
$corsResponse = Invoke-WebRequest -UseBasicParsing -Method Options -Uri http://127.0.0.1:8000/health -Headers @{Origin="http://localhost:5173"; "Access-Control-Request-Method"="GET"; "Access-Control-Request-Headers"="authorization,content-type"}
$corsResponse.StatusCode
$corsResponse.Headers["Access-Control-Allow-Origin"]
```

Se espera `200` y `http://localhost:5173`. La prueba OPTIONS comprueba el permiso
del navegador; no ejecuta una operación de negocio. Abrir `/health` directamente
en una pestaña no prueba CORS.

## Preparación del entorno (PowerShell)

Desde la raíz del repositorio, cuando se acuerde realizar la instalación:

```powershell
cd api-gateway
py -3.13 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
```

El entorno virtual mantiene las dependencias de este proyecto separadas
de las del Python global. No hace falta activar el entorno para usar estos comandos.

## Ejecutar

Desde `api-gateway/`:

```powershell
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

`app.main:app` identifica la variable `app` del módulo `app/main.py`.
`--reload` reinicia el servidor cuando cambia el código y se usa para desarrollo.

## Probar

Con el servidor iniciado, abrir en el navegador:

- Salud: http://127.0.0.1:8000/health
- Documentación interactiva: http://127.0.0.1:8000/docs

O ejecutar desde otra terminal PowerShell:

```powershell
Invoke-RestMethod http://127.0.0.1:8000/health
```

La respuesta debe contener `status` con valor `ok`.
Detener el servidor con `Ctrl+C`.
