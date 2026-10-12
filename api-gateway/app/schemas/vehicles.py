from pydantic import BaseModel
from enum import Enum

class TipoVehiculo(str, Enum):
    VEHICULO_TIPO_SIN_ESPECIFICAR = "VEHICULO_TIPO_SIN_ESPECIFICAR"
    SEDAN = "SEDAN"
    SUV = "SUV"
    PICKUP = "PICKUP"
    COUPE = "COUPE"
    HATCHBACK = "HATCHBACK"

class EstadoVehiculo(str, Enum):
    VEHICULO_ESTADO_SIN_ESPECIFICAR = "VEHICULO_ESTADO_SIN_ESPECIFICAR"
    DISPONIBLE = "DISPONIBLE"
    RESERVADO = "RESERVADO"
    EN_ALQUILER = "EN_ALQUILER"

class VehicleResponse(BaseModel):
    id: int
    patente: str
    marca: str
    modelo: str
    anio: int
    color: str | None
    tipo: TipoVehiculo
    precioDiario: float
    estado: EstadoVehiculo
    activo: bool


class VehicleWriteRequest(BaseModel):
    patente: str
    marca: str
    modelo: str
    anio: int
    color: str | None = None
    tipo: TipoVehiculo
    precioDiario: float
