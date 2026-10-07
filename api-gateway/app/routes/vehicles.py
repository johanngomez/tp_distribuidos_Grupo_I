from typing import Annotated

import grpc
from fastapi import APIRouter, Depends, HTTPException, Request, status

from app.grpc_clients.errors import GrpcServiceError
from app.grpc_clients.vehicle_client import VehicleGrpcClient
from app.schemas.vehicles import VehicleResponse
from app.security.dependencies import get_current_user
from app.security.jwt import AuthenticatedUser


router = APIRouter(
    prefix="/vehiculos",
    tags=["Vehículos"],
)


@router.get(
    "",
    response_model=list[VehicleResponse],
    summary="Listar vehículos",
)
async def listar_vehiculos(
    request: Request,
    user: Annotated[
        AuthenticatedUser,
        Depends(get_current_user),
    ],
) -> list[VehicleResponse]:

    settings = request.app.state.settings

    client = VehicleGrpcClient(
        target=settings.vehicle_grpc_target,
        timeout_seconds=settings.grpc_timeout_seconds,
    )

    try:
        return await client.list_vehicles()

    except GrpcServiceError as error:
        if error.code == grpc.StatusCode.UNAVAILABLE:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Vehicle Service no está disponible",
            ) from error

        if error.code == grpc.StatusCode.DEADLINE_EXCEEDED:
            raise HTTPException(
                status_code=status.HTTP_504_GATEWAY_TIMEOUT,
                detail="Vehicle Service excedió el tiempo de espera",
            ) from error

        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Error en Vehicle Service: {error.details}",
        ) from error