from enum import Enum
from datetime import datetime
from typing import Optional

import strawberry
from fastapi import HTTPException, Request, status
from strawberry.fastapi import GraphQLRouter
from strawberry.types import Info

from app.grpc_clients.customer_client import CustomerGrpcClient
from app.grpc_clients.rental_client import RentalGrpcClient
from app.grpc_clients.vehicle_client import VehicleGrpcClient
from app.security.jwt import AuthenticatedUser, validate_token
from jwt import InvalidTokenError


@strawberry.enum(description="Estados posibles de una reserva.")
class RentalState(Enum):
    CONFIRMADA = "CONFIRMADA"
    EN_CURSO = "EN_CURSO"
    CANCELADA = "CANCELADA"
    FINALIZADA = "FINALIZADA"


@strawberry.enum(name="TipoVehiculo", description="Tipos de vehículos disponibles para alquilar.")
class VehicleKind(Enum):
    """Tipos de vehículos que Rentar permite publicar y alquilar."""
    SEDAN = "SEDAN"
    SUV = "SUV"
    PICKUP = "PICKUP"
    COUPE = "COUPE"
    HATCHBACK = "HATCHBACK"


@strawberry.enum(description="Estados operativos del vehículo.")
class VehicleState(Enum):
    """Estados operativos de un vehículo."""
    DISPONIBLE = "DISPONIBLE"
    RESERVADO = "RESERVADO"
    EN_ALQUILER = "EN_ALQUILER"


@strawberry.type(description="Datos públicos de un cliente relacionado con una reserva.")
class CustomerNode:
    id: strawberry.ID
    documento: str
    nombre: str
    apellido: str
    email: str
    telefono: Optional[str]
    fechaNacimiento: Optional[str]
    activo: bool
    esAdmin: bool


@strawberry.type(description="Datos públicos de un vehículo y su estado operativo.")
class VehicleNode:
    id: strawberry.ID
    patente: str
    marca: str
    modelo: str
    anio: int
    color: Optional[str]
    tipo: Optional[VehicleKind]
    precioDiario: float
    estado: VehicleState
    activo: bool


@strawberry.type(description="Reserva con cliente, vehículo, fechas, importe y estado.")
class ReservationNode:
    id: strawberry.ID
    cliente: CustomerNode
    vehiculo: VehicleNode
    fechaInicio: str
    fechaFin: str
    precioDiario: float
    importeTotal: float
    estado: RentalState


@strawberry.type(description="Elemento resumido del historial de alquileres de un cliente.")
class HistoryNode:
    vehiculo: str
    patente: str
    fechaInicio: str
    fechaFin: str
    cantidadDias: int
    importeTotal: float
    estado: str


@strawberry.input(name="FiltroReservaInput")
class ReservationFilter:
    """Filtros opcionales para consultar reservas por dominio y fechas."""
    clienteId: Optional[strawberry.ID] = strawberry.field(default=None, description="Filtra por identificador de cliente.")
    vehiculoId: Optional[strawberry.ID] = strawberry.field(default=None, description="Filtra por identificador de vehículo.")
    tipo: Optional[VehicleKind] = strawberry.field(default=None, description="Filtra por tipo de vehículo.")
    estado: Optional[RentalState] = strawberry.field(default=None, description="Filtra por estado de la reserva.")
    fechaInicioDesde: Optional[str] = strawberry.field(default=None, description="Inicio del rango, formato YYYY-MM-DD.")
    fechaInicioHasta: Optional[str] = strawberry.field(default=None, description="Fin del rango, formato YYYY-MM-DD.")


def _grpc_date(value: Optional[str]) -> str:
    """Convierte fechas HTML (YYYY-MM-DD) al formato LocalDateTime del RPC."""
    if not value:
        return ""
    return value if "T" in value else f"{value}T00:00:00"


def _clients(info: Info):
    settings = info.context["request"].app.state.settings
    return (
        CustomerGrpcClient(settings.customer_grpc_target, settings.grpc_timeout_seconds),
        VehicleGrpcClient(settings.vehicle_grpc_target, settings.grpc_timeout_seconds),
        RentalGrpcClient(settings.rental_grpc_target, settings.grpc_timeout_seconds),
    )


async def _reservation_node(info: Info, reservation) -> ReservationNode:
    customer_client, vehicle_client, _ = _clients(info)
    customer, vehicle = await _gather_details(
        customer_client.get_customer(reservation.cliente_id),
        vehicle_client.get_vehicle(reservation.vehiculo_id),
    )
    return ReservationNode(
        id=strawberry.ID(str(reservation.id)),
        cliente=_customer_node(customer),
        vehiculo=_vehicle_node(vehicle),
        fechaInicio=reservation.fecha_inicio,
        fechaFin=reservation.fecha_fin,
        precioDiario=reservation.precio_diario,
        importeTotal=reservation.importe_total,
        estado=_rental_state(reservation.estado),
    )


async def _gather_details(customer_awaitable, vehicle_awaitable):
    import asyncio

    return await asyncio.gather(customer_awaitable, vehicle_awaitable)


def _customer_node(customer) -> CustomerNode:
    return CustomerNode(
        id=strawberry.ID(str(customer.id)),
        documento=customer.documento,
        nombre=customer.nombre,
        apellido=customer.apellido,
        email=customer.email,
        telefono=customer.telefono or None,
        fechaNacimiento=customer.fecha_nacimiento,
        activo=customer.activo,
        esAdmin=customer.es_admin,
    )


def _vehicle_node(vehicle) -> VehicleNode:
    return VehicleNode(
        id=strawberry.ID(str(vehicle.id)),
        patente=vehicle.patente,
        marca=vehicle.marca,
        modelo=vehicle.modelo,
        anio=vehicle.anio,
        color=vehicle.color,
        tipo=VehicleKind(vehicle.tipo) if vehicle.tipo in VehicleKind._value2member_map_ else None,
        precioDiario=vehicle.precioDiario,
        estado=VehicleState(vehicle.estado),
        activo=vehicle.activo,
    )


def _rental_state(value: str) -> RentalState:
    return RentalState(value) if value in RentalState._value2member_map_ else RentalState.CONFIRMADA


@strawberry.type
class Query:
    @strawberry.field(description="Consulta reservas visibles para el usuario autenticado y aplica filtros opcionales.")
    async def reservas(
        self,
        info: Info,
        filtro: Optional[ReservationFilter] = None,
    ) -> list[ReservationNode]:
        """Consulta reservas visibles para el usuario autenticado y aplica filtros opcionales."""
        user: AuthenticatedUser = info.context["user"]
        _, _, rental_client = _clients(info)
        reservations = await rental_client.list_rentals(
            email="" if user.es_admin else user.email,
            customer_id=int(filtro.clienteId) if filtro and filtro.clienteId else 0,
            vehicle_id=int(filtro.vehiculoId) if filtro and filtro.vehiculoId else 0,
            start_date=_grpc_date(filtro.fechaInicioDesde if filtro else None),
            end_date=_grpc_date(filtro.fechaInicioHasta if filtro else None),
        )
        if filtro and filtro.estado:
            reservations = [item for item in reservations if item.estado == filtro.estado.value]
        nodes = []
        for reservation in reservations:
            node = await _reservation_node(info, reservation)
            if filtro and filtro.tipo and node.vehiculo.tipo != filtro.tipo:
                continue
            nodes.append(node)
        return nodes

    @strawberry.field(description="Devuelve vehículos disponibles para alquilar en un intervalo de fechas.")
    async def consultarDisponibilidad(
        self,
        info: Info,
        fechaInicio: str,
        fechaFin: str,
        tipo: Optional[VehicleKind] = None,
        marca: Optional[str] = None,
        modelo: Optional[str] = None,
        precioMin: Optional[float] = None,
        precioMax: Optional[float] = None,
    ) -> list[VehicleNode]:
        """Devuelve vehículos disponibles para alquilar en un intervalo de fechas."""
        _, vehicle_client, rental_client = _clients(info)
        import asyncio

        vehicles, reservations = await asyncio.gather(
            vehicle_client.list_vehicles(),
            rental_client.list_rentals(
                start_date=fechaInicio,
                end_date=fechaFin,
            ),
        )
        requested_start = datetime.fromisoformat(fechaInicio)
        requested_end = datetime.fromisoformat(fechaFin)
        occupied_ids = {
            reservation.vehiculo_id
            for reservation in reservations
            if reservation.estado in {"CONFIRMADA", "EN_CURSO"}
            and _intervals_overlap(
                requested_start,
                requested_end,
                datetime.fromisoformat(reservation.fecha_inicio),
                datetime.fromisoformat(reservation.fecha_fin),
            )
        }
        return [
            _vehicle_node(vehicle)
            for vehicle in vehicles
            if vehicle.activo
            and vehicle.id not in occupied_ids
            and (tipo is None or vehicle.tipo == tipo.value)
            and (marca is None or vehicle.marca.lower() == marca.lower())
            and (modelo is None or vehicle.modelo.lower() == modelo.lower())
            and (precioMin is None or vehicle.precioDiario >= precioMin)
            and (precioMax is None or vehicle.precioDiario <= precioMax)
            and vehicle.estado == VehicleState.DISPONIBLE.value
        ]

    @strawberry.field(description="Obtiene el historial de alquileres de un cliente.")
    async def historialAlquileres(self, info: Info, clienteId: strawberry.ID) -> list[HistoryNode]:
        """Obtiene el historial de alquileres de un cliente."""
        _, _, rental_client = _clients(info)
        history = await rental_client.customer_history(int(clienteId))
        return [
            HistoryNode(
                vehiculo=item.vehiculo,
                patente=item.patente,
                fechaInicio=item.fecha_inicio,
                fechaFin=item.fecha_fin,
                cantidadDias=item.cantidad_dias,
                importeTotal=item.importe_total,
                estado=item.estado,
            )
            for item in history
        ]


async def graphql_context(request: Request) -> dict:
    secret = request.app.state.settings.jwt_secret
    if secret is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="JWT no configurado")
    authorization = request.headers.get("authorization", "")
    if not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token ausente")
    try:
        user = validate_token(authorization[7:].strip(), secret)
    except InvalidTokenError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token inválido") from None
    return {"request": request, "user": user}


schema = strawberry.Schema(query=Query)
router = GraphQLRouter(schema, context_getter=graphql_context)


def _intervals_overlap(start_a, end_a, start_b, end_b) -> bool:
    return start_a < end_b and start_b < end_a
