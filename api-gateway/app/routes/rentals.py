from typing import Annotated

import grpc
from fastapi import APIRouter, Depends, HTTPException, Query, Request, status

from app.grpc_clients.errors import GrpcServiceError
from app.grpc_clients.rental_client import RentalGrpcClient
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


@router.post("", response_model=RentalResponse, status_code=status.HTTP_201_CREATED)
async def create_rental(
    data: RentalCreateRequest,
    request: Request,
    _user: CurrentUser,
) -> RentalResponse:
    try:
        return await _rental_client(request).create_rental(
            customer_id=data.clienteId,
            vehicle_id=data.vehiculoId,
            start_date=data.fechaInicio,
            end_date=data.fechaFin,
        )
    except GrpcServiceError as error:
        raise _map_error(error) from error


@router.get("", response_model=list[RentalResponse])
async def list_rentals(
    request: Request,
    user: CurrentUser,
    cliente_id: int | None = Query(default=None),
    vehiculo_id: int | None = Query(default=None),
    fecha_inicio: str = Query(default=""),
    fecha_fin: str = Query(default=""),
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


@router.put("/{rental_id}/cancelar", response_model=RentalCancellationResponse)
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
