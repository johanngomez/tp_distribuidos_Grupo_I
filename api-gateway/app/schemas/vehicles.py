from pydantic import BaseModel, ConfigDict, Field


class VehicleResponse(BaseModel):
    """Representación pública de un vehículo."""

    model_config = ConfigDict(json_schema_extra={"example": {
        "id": 1, "patente": "AAA111", "marca": "Toyota", "modelo": "Corolla",
        "anio": 2022, "color": "Gris", "tipo": "SEDAN", "precioDiario": 45000,
        "estado": "DISPONIBLE", "activo": True,
    }})
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


class VehicleWriteRequest(BaseModel):
    """Datos necesarios para alta o actualización de un vehículo."""

    model_config = ConfigDict(json_schema_extra={"example": {
        "patente": "AAA999", "marca": "Toyota", "modelo": "Corolla",
        "anio": 2024, "color": "Gris", "tipo": "SEDAN", "precioDiario": 50000,
    }})

    patente: str = Field(..., description="Patente única del vehículo.")
    marca: str = Field(..., description="Marca del vehículo.")
    modelo: str = Field(..., description="Modelo del vehículo.")
    anio: int = Field(..., ge=1900, description="Año de fabricación.")
    color: str | None = Field(default=None, description="Color del vehículo.")
    tipo: str = Field(..., description="Tipo: SEDAN, SUV, PICKUP, COUPE o HATCHBACK.")
    precioDiario: float = Field(..., gt=0, description="Precio diario de alquiler.")
