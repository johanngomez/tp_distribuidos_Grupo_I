import pytest
import random
import string 
import logging

from fastapi.testclient import TestClient
from app.security.jwt import AuthenticatedUser
from unittest.mock import MagicMock
from app.security.dependencies import get_current_user, require_admin
from types import SimpleNamespace

from app.grpc_clients.customer_client import CustomerGrpcClient
from app.grpc_clients.rental_client import RentalGrpcClient
from app.grpc_clients.vehicle_client import VehicleGrpcClient

from app.main import app

# Creamos un usuario falso con privilegios de administrador para las pruebas
fake_admin_user = MagicMock()
fake_admin_user.email = "admin@rentar.com"
fake_admin_user.roles = ["ADMIN"]


app.dependency_overrides[get_current_user] = lambda: fake_admin_user
app.dependency_overrides[require_admin] = lambda: fake_admin_user

# Cliente de pruebas HTTP de FastAPI
client = TestClient(app)

GLOBAL_PATENTE = "".join(
    random.choices(string.ascii_uppercase + string.digits, k=6)
)
VEHICULO_TEST_ID = (
    None  # Aquí guardaremos el ID real que devuelva el backend
)

CLIENTE_TEST_ID = (
    None  # Aquí guardaremos el ID real que devuelva el backend
)

RESERVA_TEST_ID = (
    None  # Aquí guardaremos el ID real que devuelva el backend
)

# Cabecera simulada con un token falso para los endpoints protegidos
AUTH_HEADER = {"Authorization": "Bearer fake-token"}

 # ==================== PRUEBAS DE CLIENTES (CUSTOMER) ====================
def test_crear_cliente_con_fecha_invalida():
    """
    Prueba que el endpoint POST /api/clientes frene la petición 
    si se envía una fecha de nacimiento vacía. 
    Nota: Este endpoint es público (no requiere AUTH_HEADER).
    """
    response = client.post("/api/clientes", json={
        "nombre": "Test",
        "apellido": "User",
        "documento": "12345678",
        "email": "test@mail.com",
        "fechaNacimiento": "" # Campo inválido intencional
    })
    # Esperamos error de validación de Pydantic (422) o error controlado (400/500)
    assert response.status_code in [400, 422, 500]
    data = response.json()
    assert "detail" in data


def test_crear_cliente_exitoso():
    """
    Prueba el alta exitosa de un cliente con datos únicos para evitar duplicados.
    """
    random_id = random.randint(1000, 9999)
    email_cliente = f"cliente.test.{random_id}@rentar.com"
    password = "password123"
    response = client.post("/api/clientes", json={
        "nombre": "Roberto",
        "apellido": "Pérez",
        "documento": f"8765{random_id}", # DNI único cada vez
        "email": f"roberto.perez.{random_id}@mail.com", # Email único cada vez
        "telefono": "1122334455",
        "fechaNacimiento": "1990-05-15",
        "password": "securepassword123"
    })

    res_login = client.post(
        "/api/auth/login", json={"email": email_cliente, "password": password}
    )

    token = (
    res_login.json().get("token")
    or res_login.json().get("access_token")
    or res_login.json().get("accessToken")
    )
    # Si conecta con Java y crea el registro, debe retornar 201 Created
    # Si Java no está corriendo o da error de red/gRPC, aceptamos errores controlados de servicio (502/503)
    assert response.status_code in [201, 200]


def test_listar_clientes():
    """
    Prueba el endpoint GET /api/clientes para obtener la lista completa.
    Requiere autenticación (usamos AUTH_HEADER).
    """
    response = client.get("/api/clientes", headers=AUTH_HEADER)
    # Debe retornar 200 con una lista, o error de conexión gRPC si el backend Java está apagado
    assert response.status_code in [200]
    if response.status_code == 200:
        assert isinstance(response.json(), list)


def test_obtener_cliente_actual_me():
    """
    Prueba el endpoint GET /api/clientes/me, que busca el perfil 
    del usuario logueado basándose en su email de autenticación.
    """
    response = client.get("/api/clientes/me", headers=AUTH_HEADER)
    assert response.status_code in [200]


def test_obtener_cliente_por_id():
    """
    Prueba el endpoint GET /api/clientes/{customer_id} consultando un ID específico.
    """
    customer_id = CLIENTE_TEST_ID if CLIENTE_TEST_ID else 5
    response = client.get(f"/api/clientes/{customer_id}", headers=AUTH_HEADER)
    # Puede ser 200 si existe, 404 si no existe, o error de servicio gRPC
    assert response.status_code in [200, 404]


def test_actualizar_cliente():
    """
    Prueba el endpoint PUT /api/clientes/{customer_id} para modificar datos del cliente.
    """
    customer_id = CLIENTE_TEST_ID if CLIENTE_TEST_ID else 1
    response = client.put(f"/api/clientes/{customer_id}", json={
        "nombre": "Roberto Actualizado",
        "apellido": "Pérez",
        "telefono": "9988776655",
        "fechaNacimiento": "1990-05-15"
    }, headers=AUTH_HEADER)
    assert response.status_code in [200]

def test_desactivar_cliente():
    """
    Prueba el endpoint DELETE /api/clientes/{customer_id} que realiza 
    una baja lógica (desactivación) del cliente.
    """
    rand = random.randint(1000, 9999)

    res_creacion = client.post(
      "/api/clientes",
      json={
          "nombre": "ClienteParaBorrar",
          "apellido": "Test",
          "documento": f"4433{rand}",
          "email": f"borrar.{rand}@mail.com",
          "telefono": "1122334455",
          "fechaNacimiento": "1990-01-01",
          "password": "password123",
      },
    )

    data = res_creacion.json() if res_creacion.status_code in [200, 201] else {}
    customer_id = data.get("id") or data.get("cliente_id")

    if not customer_id:
        pytest.skip("No se pudo crear el cliente temporal para la prueba de baja.")

    response = client.delete(f"/api/clientes/{customer_id}", headers=AUTH_HEADER)
    assert response.status_code in [200, 404]

    # ==================== PRUEBAS DE VEHICULOS (VEHICLE) ====================

def test_listar_vehiculos():
    """
    Prueba el endpoint GET /vehiculos para obtener la flota completa.
    Requiere autenticación (usamos AUTH_HEADER).
    """
    response = client.get("/vehiculos", headers=AUTH_HEADER)
    # Debe retornar 200 con la lista, o error de conexión gRPC si el backend Java está apagado
    assert response.status_code in [200]
    if response.status_code == 200:
        assert isinstance(response.json(), list)


def test_crear_vehiculo_invalido():
    """
    Prueba el endpoint POST /vehiculos enviando datos incorrectos 
    (como un tipo de vehículo inválido o campos vacíos) para forzar un error de validación.
    Nota: Exige rol de administrador (`require_admin`), cubierto por el mock.
    """
    response = client.post("/vehiculos", json={
        "patente": "",
        "modelo": "",
        "marca": "",
        "anio": 0,
        "tipo": "TIPO_INEXISTENTE", # Forzamos error en la conversión de protobuf
        "precioDiario": 100.0
    }, headers=AUTH_HEADER)
    # Esperamos error de Pydantic (422) o error controlado por tipo inválido (400)
    assert response.status_code in [400, 422, 500, 502, 503]
    data = response.json()
    assert "detail" in data


def test_crear_vehiculo_exitoso():
    """
    Prueba el alta exitosa de un vehículo mediante POST /vehiculos con datos válidos.
    """
    global VEHICULO_TEST_ID
    response = client.post("/vehiculos", json={
        "patente": GLOBAL_PATENTE,
        "marca": "Toyota",
        "modelo": "Corolla",
        "anio": 2023,
        "color": "Blanco",
        "tipo": "SEDAN",
        "precioDiario": 50000.0
    }, headers=AUTH_HEADER)
    # Retorna 201 Created si Java procesa bien
    assert response.status_code in [201, 200]
    VEHICULO_TEST_ID = response.json().get("id")


def test_actualizar_vehiculo():
    """
    Prueba el endpoint PUT /vehiculos/{vehicle_id} para modificar la información de un vehículo.
    """
    vehicle_id = VEHICULO_TEST_ID if VEHICULO_TEST_ID else 1
    response = client.put(f"/vehiculos/{vehicle_id}", json={
        "patente": GLOBAL_PATENTE,
        "marca": "Toyota",
        "modelo": "Corolla Actualizado",
        "anio": 2023,
        "color": "Negro",
        "tipo": "SEDAN",
        "precioDiario": 55000.0
    }, headers=AUTH_HEADER)
    # Puede ser 200 si se actualiza, 404 si no existe, o error de servicio gRPC
    assert response.status_code in [200]

def test_eliminar_vehiculo():
    """
    Prueba el endpoint DELETE /vehiculos/{vehicle_id} para dar de baja un vehículo.
    """
    vehicle_id = VEHICULO_TEST_ID if VEHICULO_TEST_ID else 1
    response = client.delete(f"/vehiculos/{vehicle_id}", headers=AUTH_HEADER)
    # Debe retornar un diccionario con {"success": True/False} o error controlado
    assert response.status_code in [200]

    # ==================== PRUEBAS DE RESERVAS (RENTAL) ====================
def test_crear_reserva_cliente_inexistente():
    """
    Prueba que el Gateway frene la creación de la reserva con un error 404 
    cuando el servicio de Clientes indica que el ID del cliente no existe.
    """
    response = client.post("/api/reservas", json={
        "clienteId": 99999999999, # ID falso que no existe
        "vehiculoId": 1,
        "fechaInicio": "2026-12-01T10:00:00",
        "fechaFin": "2026-12-05T18:00:00"
    }, headers=AUTH_HEADER)
    
    # Esperamos 404 Not Found (según la validación del gateway) 
    # o error de gRPC si el backend no responde (502/503)
    assert response.status_code in [404, 502, 503]
    data = response.json()
    assert "detail" in data


def test_crear_reserva_vehiculo_no_disponible():
    """
    Prueba que el Gateway rechace la reserva (error 409 Conflict) 
    si el vehículo seleccionado no se encuentra en estado 'DISPONIBLE' 
    o está inactivo.
    """
    response = client.post("/api/reservas", json={
        "clienteId": 1,
        "vehiculoId": 2, # Supongamos un vehículo ocupado o inactivo
        "fechaInicio": "2026-12-01T10:00:00",
        "fechaFin": "2026-12-05T18:00:00"
    }, headers=AUTH_HEADER)
    
    # Esperamos 409 Conflict por reglas de negocio, o error de servicio gRPC
    assert response.status_code in [409, 404, 502, 503]
    data = response.json()
    assert "detail" in data


def test_listar_reservas_con_filtros():
    """
    Prueba el endpoint GET /api/reservas enviando parámetros de consulta opcionales 
    (filtros por cliente, vehículo y rango de fechas).
    """
    # Pasamos parámetros query string en la petición GET
    params = {
        "cliente_id": CLIENTE_TEST_ID if CLIENTE_TEST_ID else 1,
        "vehiculo_id": VEHICULO_TEST_ID if VEHICULO_TEST_ID else 1,
        "fecha_inicio": "2026-12-05T18:00:00",
        "fecha_fin": "2027-12-05T18:00:00"
    }
    response = client.get("/api/reservas", params=params, headers=AUTH_HEADER)
    
    assert response.status_code in [200]
    if response.status_code == 200:
        assert isinstance(response.json(), list)


def test_cancelar_reserva_exitoso_o_inexistente():
    """
    Prueba el endpoint PUT /api/reservas/{rental_id}/cancelar 
    para dar de baja una reserva activa o evaluar un ID inexistente.
    """
    rental_id = 99999 # ID de reserva de prueba
    response = client.put(f"/api/reservas/{rental_id}/cancelar", headers=AUTH_HEADER)
    
    # Puede retornar 404 si no existe, 400/409 si no se puede cancelar, o 200 si se cancela con éxito.
    assert response.status_code in [200, 400, 404]
    data = response.json()
    assert "detail" in data or "success" in data or isinstance(data, dict)

    # al finalizar las pruebas, eliminamos el cliente y vehículo de prueba


def test_crear_y_cancelar_reserva_como_cliente():

    try:
        rand = random.randint(1000, 9999)

        email_cliente = f"cliente.test.{rand}@rentar.com"
        password = "password123"
        fake_cliente_user = SimpleNamespace(email=email_cliente, roles=["CLIENT"])
        app.dependency_overrides[get_current_user] = lambda: fake_cliente_user
        response = client.post("/api/clientes", json={
            "nombre": "Ismael",
            "apellido": "Argañaraz",
            "documento": f"8765{rand}", # DNI único cada vez
            "email": email_cliente, # Email único cada vez
            "telefono": "1122334455",
            "fechaNacimiento": "1990-05-15",
            "password": "securepassword69"
        })

        patente = "".join(
            random.choices(string.ascii_uppercase + string.digits, k=6)
        )

        #Creamos el vehículo
        res_vehiculo = client.post(
            "/vehiculos",
            json={
                "patente": patente,
                "marca": "Toyota",
                "modelo": "Corolla",
                "anio": 2023,
                "color": "Blanco",
                "tipo": "SEDAN",
                "precioDiario": 40000.0,
            },
            headers=AUTH_HEADER,
        )
        vehiculo_id = res_vehiculo.json().get("id") or res_vehiculo.json().get(
            "vehicle_id"
        )

        cliente_id = response.json().get("id") or response.json().get(
            "cliente_id"
        )


        #Creamos la reserva con el cliente
        res_creacion = client.post(
            "/api/reservas",
            json={
                "clienteId": cliente_id,
                "vehiculoId": vehiculo_id,
                "fechaInicio": "2028-06-09T10:00:00",
                "fechaFin": "2028-06-10T18:00:00",
            },
            headers=AUTH_HEADER,
        )

        assert res_creacion.status_code in [200, 201]
        reserva_id = res_creacion.json().get("id") or res_creacion.json().get(
            "reserva_id"
        )

        #Cancelamos la reserva
        response = client.put(
            f"/api/reservas/{reserva_id}/cancelar", headers=AUTH_HEADER
        )


    finally:
        print("Respuesta cancelación exitosa:", response.status_code, flush=True)
        assert response.status_code in [200, 201]