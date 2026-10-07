from pydantic import BaseModel


class VehicleResponse(BaseModel):
    id: int
    patente: str
    marca: str
    modelo: str
    anio: int
    color: str | None
    tipo: str
    precioDiario: float
    estado: str
    activo: bool
