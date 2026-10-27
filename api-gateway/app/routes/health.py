from fastapi import APIRouter


router = APIRouter(tags=["Health"])


@router.get("/health", summary="Verificar disponibilidad del Gateway", description="Endpoint público para comprobar que el API Gateway está activo.")
def health() -> dict[str, str]:
    return {"status": "ok"}
