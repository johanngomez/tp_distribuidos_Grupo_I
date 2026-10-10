from typing import Annotated

import grpc
from fastapi import APIRouter, Depends, HTTPException, Request, status

from app.grpc_clients.errors import GrpcServiceError
from app.grpc_clients.vehicle_client import VehicleGrpcClient
from app.schemas.vehicles import VehicleResponse
from app.security.dependencies import get_current_user
from app.security.jwt import AuthenticatedUser


router = APIRouter(prefix="/vehiculos", tags=["Vehículos"])


def _vehicle_client(request: Request) -> VehicleGrpcClient:
    settings = request.app.state.settings
    return VehicleGrpcClient(
        target=settings.vehicle_grpc_target,
        timeout_seconds=settings.grpc_timeout_seconds,
    )


def _map_grpc_error(error: GrpcServiceError) -> HTTPException:
    if error.code == grpc.StatusCode.UNAVAILABLE:
        return HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Vehicle Service no disponible",
        )
    if error.code == grpc.StatusCode.DEADLINE_EXCEEDED:
        return HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail="Vehicle Service agotó el tiempo de respuesta",
        )
    if error.code == grpc.StatusCode.NOT_FOUND:
        return HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Vehículo no encontrado",
        )
    return HTTPException(
        status_code=status.HTTP_502_BAD_GATEWAY,
        detail=f"Error de Vehicle Service: {error.details}",
    )


@router.get("", response_model=list[VehicleResponse])
async def list_vehicles(
    request: Request,
    _user: Annotated[AuthenticatedUser, Depends(get_current_user)],
) -> list[VehicleResponse]:
    """Devuelve los vehículos usando Vehicle Service mediante gRPC."""
    try:
        return await _vehicle_client(request).list_vehicles()
    except GrpcServiceError as error:
        raise _map_grpc_error(error) from error
