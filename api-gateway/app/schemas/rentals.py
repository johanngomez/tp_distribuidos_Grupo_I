from pydantic import BaseModel
from enum import Enum

class EstadoReserva(str, Enum):
    SIN_ESPECIFICAR = "SIN_ESPECIFICAR"
    EN_CURSO = "EN_CURSO"
    CONFIRMADA = "CONFIRMADA"
    FINALIZADA = "FINALIZADA"
    CANCELADA = "CANCELADA"



class RentalResponse(BaseModel):
    id: int
    cliente_id: int
    vehiculo_id: int
    fecha_inicio: str
    fecha_fin: str
    precio_diario: float
    importe_total: float
    estado: EstadoReserva


class RentalCreateRequest(BaseModel):
    clienteId: int
    vehiculoId: int
    fechaInicio: str
    fechaFin: str


class RentalCancelRequest(BaseModel):
    clienteEmail: str | None = None


class RentalHistoryResponse(BaseModel):
    vehiculo: str
    patente: str
    fecha_inicio: str
    fecha_fin: str
    cantidad_dias: int
    importe_total: float
    estado: EstadoReserva


class RentalCancellationResponse(BaseModel):
    exito: bool
    reserva: RentalResponse | None
