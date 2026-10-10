from pydantic import BaseModel


class RentalResponse(BaseModel):
    id: int
    cliente_id: int
    vehiculo_id: int
    fecha_inicio: str
    fecha_fin: str
    precio_diario: float
    importe_total: float
    estado: str


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
    estado: str


class RentalCancellationResponse(BaseModel):
    exito: bool
    reserva: RentalResponse | None
