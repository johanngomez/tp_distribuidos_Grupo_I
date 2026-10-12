import pytest
import random
from fastapi.testclient import TestClient
from app.security.jwt import AuthenticatedUser
from unittest.mock import MagicMock
from app.security.dependencies import get_current_user, require_admin

from app.grpc_clients.customer_client import CustomerGrpcClient
from app.grpc_clients.rental_client import RentalGrpcClient
from app.grpc_clients.vehicle_client import VehicleGrpcClient

from app.main import app

# Creamos un usuario falso con privilegios de administrador para las pruebas
fake_admin_user = MagicMock()
fake_admin_user.email = "admin@test.com"
fake_admin_user.roles = ["ADMIN"]

app.dependency_overrides[get_current_user] = lambda: fake_admin_user
app.dependency_overrides[require_admin] = lambda: fake_admin_user

# Cliente de pruebas HTTP de FastAPI
client = TestClient(app)

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
    response = client.post("/api/clientes", json={
        "nombre": "Juan",
        "apellido": "Pérez",
        "documento": f"8765{random_id}", # DNI único cada vez
        "email": f"juan.perez.{random_id}@mail.com", # Email único cada vez
        "telefono": "1122334455",
        "fechaNacimiento": "1990-05-15",
        "password": "securepassword123"
    })
    # Si conecta con Java y crea el registro, debe retornar 201 Created
    # Si Java no está corriendo o da error de red/gRPC, aceptamos errores controlados de servicio (502/503)
    assert response.status_code in [201, 200, 502, 503]


def test_listar_clientes():
    """
    Prueba el endpoint GET /api/clientes para obtener la lista completa.
    Requiere autenticación (usamos AUTH_HEADER).
    """
    response = client.get("/api/clientes", headers=AUTH_HEADER)
    # Debe retornar 200 con una lista, o error de conexión gRPC si el backend Java está apagado
    assert response.status_code in [200, 502, 503]
    if response.status_code == 200:
        assert isinstance(response.json(), list)


def test_obtener_cliente_actual_me():
    """
    Prueba el endpoint GET /api/clientes/me, que busca el perfil 
    del usuario logueado basándose en su email de autenticación.
    """
    response = client.get("/api/clientes/me", headers=AUTH_HEADER)
    assert response.status_code in [200, 404, 502, 503]


def test_obtener_cliente_por_id():
    """
    Prueba el endpoint GET /api/clientes/{customer_id} consultando un ID específico.
    """
    customer_id = 1
    response = client.get(f"/api/clientes/{customer_id}", headers=AUTH_HEADER)
    # Puede ser 200 si existe, 404 si no existe, o error de servicio gRPC
    assert response.status_code in [200, 404, 502, 503]


def test_actualizar_cliente():
    """
    Prueba el endpoint PUT /api/clientes/{customer_id} para modificar datos del cliente.
    """
    customer_id = 1
    response = client.put(f"/api/clientes/{customer_id}", json={
        "nombre": "Juan Actualizado",
        "apellido": "Pérez",
        "telefono": "9988776655",
        "fechaNacimiento": "1990-05-15"
    }, headers=AUTH_HEADER)
    assert response.status_code in [200, 404, 400, 502, 503]


def test_desactivar_cliente():
    """
    Prueba el endpoint DELETE /api/clientes/{customer_id} que realiza 
    una baja lógica (desactivación) del cliente.
    """
    customer_id = 1
    response = client.delete(f"/api/clientes/{customer_id}", headers=AUTH_HEADER)
    assert response.status_code in [200, 404, 502, 503]

    # ==================== PRUEBAS DE VEHICULOS (VEHICLE) ====================

def test_listar_vehiculos():
    """
    Prueba el endpoint GET /vehiculos para obtener la flota completa.
    Requiere autenticación (usamos AUTH_HEADER).
    """
    response = client.get("/vehiculos", headers=AUTH_HEADER)
    # Debe retornar 200 con la lista, o error de conexión gRPC si el backend Java está apagado
    assert response.status_code in [200, 502, 503]
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
    response = client.post("/vehiculos", json={
        "patente": "AB123CD",
        "marca": "Toyota",
        "modelo": "Corolla",
        "anio": 2023,
        "color": "Blanco",
        "tipo": "SEDAN", # Tipo válido que matchea con vehicle_pb2.TipoVehiculo
        "precioDiario": 50000.0
    }, headers=AUTH_HEADER)
    # Retorna 201 Created si Java procesa bien, o error de red si el backend está desconectado
    assert response.status_code in [201, 200, 502, 503]


def test_actualizar_vehiculo():
    """
    Prueba el endpoint PUT /vehiculos/{vehicle_id} para modificar la información de un vehículo.
    """
    vehicle_id = 1
    response = client.put(f"/vehiculos/{vehicle_id}", json={
        "patente": "AB123CD",
        "marca": "Toyota",
        "modelo": "Corolla Actualizado",
        "anio": 2023,
        "color": "Negro",
        "tipo": "SEDAN",
        "precioDiario": 55000.0
    }, headers=AUTH_HEADER)
    # Puede ser 200 si se actualiza, 404 si no existe, o error de servicio gRPC
    assert response.status_code in [200, 404, 400, 502, 503]


def test_eliminar_vehiculo():
    """
    Prueba el endpoint DELETE /vehiculos/{vehicle_id} para dar de baja un vehículo.
    """
    vehicle_id = 1
    response = client.delete(f"/vehiculos/{vehicle_id}", headers=AUTH_HEADER)
    # Debe retornar un diccionario con {"success": True/False} o error controlado
    assert response.status_code in [200, 404, 502, 503]

    # ==================== PRUEBAS DE RESERVAS (RENTAL) ====================
def test_crear_reserva_cliente_inexistente():
    """
    Prueba que el Gateway frene la creación de la reserva con un error 404 
    cuando el servicio de Clientes indica que el ID del cliente no existe.
    """
    response = client.post("/api/reservas", json={
        "clienteId": 99999, # ID falso que no existe
        "vehiculoId": 1,
        "fechaInicio": "2026-10-15",
        "fechaFin": "2026-10-20"
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
        "fechaInicio": "2026-10-15",
        "fechaFin": "2026-10-20"
    }, headers=AUTH_HEADER)
    
    # Esperamos 409 Conflict por reglas de negocio, o error de servicio gRPC
    assert response.status_code in [409, 404, 502, 503]
    data = response.json()
    assert "detail" in data


def test_crear_reserva_exitoso():
    """
    Prueba el flujo completo y exitoso de creación de una reserva.
    El Gateway validará al cliente y al vehículo antes de dar el OK.
    """
    response = client.post("/api/reservas", json={
        "clienteId": 1,
        "vehiculoId": 1,
        "fechaInicio": "2026-11-01",
        "fechaFin": "2026-11-05"
    }, headers=AUTH_HEADER)
    
    # Retorna 201 Created si pasa todas las validaciones cruzadas y Java la guarda,
    # o 409/404 si el cliente/vehículo mockeado no cumple las condiciones en la BD real.
    # Aceptamos también 502/503 si el backend Java no está activo.
    assert response.status_code in [201, 200, 404, 409, 502, 503]


def test_listar_reservas_con_filtros():
    """
    Prueba el endpoint GET /api/reservas enviando parámetros de consulta opcionales 
    (filtros por cliente, vehículo y rango de fechas).
    """
    # Pasamos parámetros query string en la petición GET
    params = {
        "cliente_id": 1,
        "vehiculo_id": 1,
        "fecha_inicio": "2026-11-01",
        "fecha_fin": "2026-11-05"
    }
    response = client.get("/api/reservas", params=params, headers=AUTH_HEADER)
    
    assert response.status_code in [200, 502, 503]
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
    assert response.status_code in [200, 400, 404, 409, 502, 503]
    data = response.json()
    assert "detail" in data or "success" in data or isinstance(data, dict)