from pydantic import BaseModel, ConfigDict, Field


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
    """Datos para crear una reserva; las fechas usan ISO-8601."""
    model_config = ConfigDict(json_schema_extra={"example": {
        "clienteId": 1, "vehiculoId": 1,
        "fechaInicio": "2026-11-10T10:00:00", "fechaFin": "2026-11-15T10:00:00",
    }})
    clienteId: int = Field(..., gt=0, description="Identificador del cliente.")
    vehiculoId: int = Field(..., gt=0, description="Identificador del vehículo.")
    fechaInicio: str = Field(..., description="Inicio en formato YYYY-MM-DDTHH:MM:SS.")
    fechaFin: str = Field(..., description="Fin en formato YYYY-MM-DDTHH:MM:SS.")


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
