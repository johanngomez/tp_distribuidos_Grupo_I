import grpc
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Request, status
from pydantic import BaseModel

from app.grpc_clients.customer_client import CustomerGrpcClient
from app.grpc_clients.errors import GrpcServiceError
from app.security.dependencies import get_current_user
from app.security.jwt import AuthenticatedUser


router = APIRouter(prefix="/auth", tags=["Autenticación"])


class LoginRequest(BaseModel):
    email: str
    password: str


@router.post("/login")
async def login(data: LoginRequest, request: Request) -> dict[str, str | bool]:
    settings = request.app.state.settings
    if settings.jwt_secret is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="JWT no configurado en el Gateway",
        )
    try:
        return await CustomerGrpcClient(
            settings.customer_grpc_target,
            settings.grpc_timeout_seconds,
        ).login(data.email, data.password)
    except GrpcServiceError as error:
        if error.code == grpc.StatusCode.UNAUTHENTICATED:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Credenciales inválidas",
            ) from error
        if error.code == grpc.StatusCode.UNAVAILABLE:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Customer Service no disponible",
            ) from error
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=error.details,
        ) from error


@router.get("/me", summary="Consultar la identidad del token")
def current_identity(
    user: Annotated[AuthenticatedUser, Depends(get_current_user)],
) -> dict[str, str | bool]:
    """Comprueba el JWT; no devuelve el perfil del cliente ni consulta Java."""
    return {"email": user.email, "esAdmin": user.es_admin}
