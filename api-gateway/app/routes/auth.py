from typing import Annotated

from fastapi import APIRouter, Depends

from app.security.dependencies import get_current_user
from app.security.jwt import AuthenticatedUser


router = APIRouter(prefix="/auth", tags=["Autenticación"])


@router.get("/me", summary="Consultar la identidad del token")
def current_identity(
    user: Annotated[AuthenticatedUser, Depends(get_current_user)],
) -> dict[str, str | bool]:
    """Comprueba el JWT; no devuelve el perfil de /api/clientes/me ni consulta Java."""
    return {"email": user.email, "esAdmin": user.es_admin}
