# Login y seguridad

[Volver al índice](../README.md).

## Flujo

```text
POST /auth/login
 → AuthController
 → AuthService
 → ClienteRepository.findByEmail
 → comprobación de activo y BCrypt.matches
 → JwtEncoder
 → token JWT
```

El servicio implementa el login propio de Rentar. Usa componentes de Spring Security; no es la página de login predeterminada de Spring ni un servidor OAuth completo.

BCrypt almacena un hash, no una contraseña cifrada recuperable. `passwordEncoder.matches(entrada, hash)` verifica el valor. `ClienteService` y el inicializador usan `encode` al guardar contraseñas.

## Probar desde Swagger

En una base nueva, `ClienteInicializador` crea el administrador `admin@rentar.com` con contraseña de prueba `123456`. Si encuentra un administrador existente con hash, no restablece su contraseña: usar las credenciales de esa base.

`POST /auth/login`:

```json
{
  "email": "admin@rentar.com",
  "password": "123456"
}
```

Respuesta representativa, con el token abreviado como marcador:

```json
{
  "token": "TOKEN_JWT_RECIBIDO",
  "tipo": "Bearer",
  "esAdmin": true
}
```

Copiar el token, pulsar **Authorize** en Swagger y pegarlo sin agregar `Bearer` manualmente. El esquema de seguridad HTTP de Swagger agrega el prefijo. En curl sí se escribe el encabezado completo:

```bash
curl -i http://localhost:8080/api/clientes/me \
  -H 'Authorization: Bearer TOKEN_JWT_RECIBIDO'
```

`GET /auth/me` pertenece al Gateway, no a este monolito. Aquí se consulta el perfil con `/api/clientes/me`.

## JWT

`AuthService` emite tokens HS256 con `iss=rentar`, `sub=email`, `iat`, `exp` a dos horas y el claim booleano `esAdmin`. `JwtConfig` construye la clave simétrica desde `jwt.secret`, el encoder Nimbus y el decoder configurado para HS256. Header y payload del token son legibles; la firma no cifra su contenido.

El secreto usado para verificar debe coincidir con el de emisión. No publicar tokens vigentes ni claves reales en capturas, documentación o commits.

## Qué protege `SecurityConfig`

La política declara públicas `/auth/**`, `/error`, Swagger/OpenAPI, `/graphql/**` y `/graphiql/**`. El resto requiere un JWT reconocido por Spring Security. La sesión HTTP es `STATELESS`: no se conserva una sesión de login en el servidor entre solicitudes.

CORS admite `http://localhost:5173`. Es una política del navegador y no reemplaza autenticación. CSRF está deshabilitado en esta configuración de API.

El claim `esAdmin` no se transforma en una regla general de administrador en `SecurityFilterChain`. No hay que interpretar `authenticated()` como `hasRole("ADMIN")`.

La query `reservas` busca el cliente por el email autenticado y restringe a los clientes normales a sus reservas. Por eso conviene enviar token aun cuando `/graphql` sea público. Sin identidad de cliente puede fallar esa consulta. El historial recibe `clienteId` sin validar allí que sea el titular; disponibilidad no exige usuario en su resolver.

El login original lanza excepciones de ejecución y su controller no implementa la traducción explícita a 401 que sí existe en el Gateway. No asumir que las credenciales inválidas devuelven el mismo formato/código en ambos módulos.

Fuentes: [SecurityConfig](../src/main/java/ar/unla/rentar/config/SecurityConfig.java), [JwtConfig](../src/main/java/ar/unla/rentar/config/JwtConfig.java), [AuthService](../src/main/java/ar/unla/rentar/service/AuthService.java) y [AuthController](../src/main/java/ar/unla/rentar/controller/AuthController.java).
