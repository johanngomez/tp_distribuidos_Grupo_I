from typing import Annotated

import grpc
from fastapi import APIRouter, Depends, HTTPException, Request, status

from app.generated import vehicle_pb2
from app.grpc_clients.errors import GrpcServiceError
from app.grpc_clients.vehicle_client import VehicleGrpcClient
from app.schemas.vehicles import VehicleResponse, VehicleWriteRequest
from app.security.dependencies import get_current_user, require_admin
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


@router.post("", response_model=VehicleResponse, status_code=status.HTTP_201_CREATED)
async def create_vehicle(
    data: VehicleWriteRequest,
    request: Request,
    _user: Annotated[AuthenticatedUser, Depends(require_admin)],
) -> VehicleResponse:
    try:
        return await _vehicle_client(request).create_vehicle(_vehicle_payload(data))
    except GrpcServiceError as error:
        raise _map_grpc_error(error) from error


@router.put("/{vehicle_id}", response_model=VehicleResponse)
async def update_vehicle(
    vehicle_id: int,
    data: VehicleWriteRequest,
    request: Request,
    _user: Annotated[AuthenticatedUser, Depends(require_admin)],
) -> VehicleResponse:
    try:
        return await _vehicle_client(request).update_vehicle(
            vehicle_id,
            _vehicle_payload(data),
        )
    except GrpcServiceError as error:
        raise _map_grpc_error(error) from error


@router.delete("/{vehicle_id}")
async def delete_vehicle(
    vehicle_id: int,
    request: Request,
    _user: Annotated[AuthenticatedUser, Depends(require_admin)],
) -> dict[str, bool]:
    try:
        return {"success": await _vehicle_client(request).delete_vehicle(vehicle_id)}
    except GrpcServiceError as error:
        raise _map_grpc_error(error) from error


def _vehicle_payload(data: VehicleWriteRequest) -> dict:
    try:
        vehicle_type = vehicle_pb2.VehicleType.Value(data.tipo.upper())
    except ValueError as error:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Tipo de vehículo inválido: {data.tipo}",
        ) from error
    return {
        "patente": data.patente,
        "marca": data.marca,
        "modelo": data.modelo,
        "anio": data.anio,
        "color": data.color or "",
        "tipo": vehicle_type,
        "precio_diario": data.precioDiario,
    }
