from typing import Annotated

import grpc
from fastapi import APIRouter, Depends, HTTPException, Query, Request, status

from app.grpc_clients.errors import GrpcServiceError
from app.grpc_clients.rental_client import RentalGrpcClient
from app.grpc_clients.customer_client import CustomerGrpcClient
from app.grpc_clients.vehicle_client import VehicleGrpcClient
from app.schemas.rentals import (
    RentalCancellationResponse,
    RentalCreateRequest,
    RentalResponse,
)
from app.security.dependencies import get_current_user
from app.security.jwt import AuthenticatedUser


router = APIRouter(prefix="/api/reservas", tags=["Reservas"])
CurrentUser = Annotated[AuthenticatedUser, Depends(get_current_user)]


def _rental_client(request: Request) -> RentalGrpcClient:
    settings = request.app.state.settings
    return RentalGrpcClient(
        target=settings.rental_grpc_target,
        timeout_seconds=settings.grpc_timeout_seconds,
    )


def _customer_client(request: Request) -> CustomerGrpcClient:
    settings = request.app.state.settings
    return CustomerGrpcClient(settings.customer_grpc_target, settings.grpc_timeout_seconds)


def _vehicle_client(request: Request) -> VehicleGrpcClient:
    settings = request.app.state.settings
    return VehicleGrpcClient(settings.vehicle_grpc_target, settings.grpc_timeout_seconds)


def _map_error(error: GrpcServiceError) -> HTTPException:
    code_to_http = {
        grpc.StatusCode.NOT_FOUND: status.HTTP_404_NOT_FOUND,
        grpc.StatusCode.INVALID_ARGUMENT: status.HTTP_400_BAD_REQUEST,
        grpc.StatusCode.FAILED_PRECONDITION: status.HTTP_409_CONFLICT,
        grpc.StatusCode.UNAVAILABLE: status.HTTP_503_SERVICE_UNAVAILABLE,
        grpc.StatusCode.DEADLINE_EXCEEDED: status.HTTP_504_GATEWAY_TIMEOUT,
    }
    detail = "Rental Service no disponible" if error.code == grpc.StatusCode.UNAVAILABLE else error.details
    return HTTPException(
        status_code=code_to_http.get(error.code, status.HTTP_502_BAD_GATEWAY),
        detail=detail,
    )


@router.post(
    "", response_model=RentalResponse, status_code=status.HTTP_201_CREATED,
    summary="Crear reserva",
    description="Valida cliente y disponibilidad del vehículo y crea una reserva confirmada.",
    responses={201: {"description": "Reserva creada."}, 404: {"description": "Cliente o vehículo inexistente."}, 409: {"description": "Cliente inactivo o vehículo no disponible."}, 503: {"description": "Algún servicio interno no está disponible."}},
)
async def create_rental(
    data: RentalCreateRequest,
    request: Request,
    _user: CurrentUser,
) -> RentalResponse:
    try:
        # El Gateway coordina las validaciones de los dominios antes de crear la reserva.
        customer = await _customer_client(request).validate_customer(data.clienteId)
        if not customer.existe:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Cliente inexistente")
        if not customer.activo or not customer.puede_alquilar:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Cliente inactivo o no habilitado")
        vehicle = await _vehicle_client(request).get_vehicle(data.vehiculoId)
        if not vehicle.activo:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Vehículo inactivo")
        if vehicle.estado != "DISPONIBLE":
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Vehículo no disponible")
        return await _rental_client(request).create_rental(
            customer_id=data.clienteId,
            vehicle_id=data.vehiculoId,
            start_date=data.fechaInicio,
            end_date=data.fechaFin,
        )
    except GrpcServiceError as error:
        raise _map_error(error) from error


@router.get(
    "", response_model=list[RentalResponse], summary="Consultar reservas",
    description="Lista reservas. Los administradores pueden consultar todas; los clientes solo las propias. Permite filtrar por cliente, vehículo y fechas.",
    responses={401: {"description": "Token ausente o inválido."}, 503: {"description": "Rental Service no disponible."}},
)
async def list_rentals(
    request: Request,
    user: CurrentUser,
    cliente_id: int | None = Query(default=None, description="Filtra por ID de cliente."),
    vehiculo_id: int | None = Query(default=None, description="Filtra por ID de vehículo."),
    fecha_inicio: str = Query(default="", description="Fecha/hora inicial ISO-8601. Ejemplo: 2026-11-10T00:00:00.", examples=["2026-11-10T00:00:00"]),
    fecha_fin: str = Query(default="", description="Fecha/hora final ISO-8601. Ejemplo: 2026-11-15T00:00:00.", examples=["2026-11-15T00:00:00"]),
) -> list[RentalResponse]:
    try:
        return await _rental_client(request).list_rentals(
            email="" if user.es_admin else user.email,
            customer_id=cliente_id or 0,
            vehicle_id=vehiculo_id or 0,
            start_date=fecha_inicio,
            end_date=fecha_fin,
        )
    except GrpcServiceError as error:
        raise _map_error(error) from error


@router.put(
    "/{rental_id}/cancelar", response_model=RentalCancellationResponse,
    summary="Cancelar reserva", description="Cancela una reserva si todavía no comenzó.",
    responses={200: {"description": "Reserva cancelada."}, 404: {"description": "Reserva inexistente."}, 409: {"description": "La reserva ya comenzó o ya estaba cancelada."}},
)
async def cancel_rental(
    rental_id: int,
    request: Request,
    user: CurrentUser,
) -> RentalCancellationResponse:
    try:
        return await _rental_client(request).cancel_rental(
            rental_id=rental_id,
            customer_email=user.email,
        )
    except GrpcServiceError as error:
        raise _map_error(error) from error
