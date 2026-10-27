from pydantic import BaseModel, ConfigDict, Field


class CustomerResponse(BaseModel):
    model_config = ConfigDict(json_schema_extra={"example": {
        "id": 1, "documento": "30123456", "nombre": "Ana", "apellido": "Pérez",
        "email": "ana@rentar.com", "telefono": "1122334455",
        "fecha_nacimiento": "1990-05-20", "activo": True, "es_admin": False,
    }})
    id: int
    documento: str
    nombre: str
    apellido: str
    email: str
    telefono: str
    fecha_nacimiento: str | None
    activo: bool
    es_admin: bool


class CustomerCreateRequest(BaseModel):
    """Datos para registrar un cliente."""
    model_config = ConfigDict(json_schema_extra={"example": {
        "documento": "30123456", "nombre": "Ana", "apellido": "Pérez",
        "email": "ana@rentar.com", "telefono": "1122334455",
        "fechaNacimiento": "1990-05-20", "password": "secreto123",
    }})
    documento: str = Field(..., description="Documento único del cliente.")
    nombre: str = Field(..., description="Nombre del cliente.")
    apellido: str = Field(..., description="Apellido del cliente.")
    email: str = Field(..., description="Correo utilizado para iniciar sesión.")
    telefono: str = Field(default="", description="Teléfono de contacto.")
    fechaNacimiento: str | None = Field(default=None, description="Fecha en formato YYYY-MM-DD. Ejemplo: 1990-05-20.", examples=["1990-05-20"])
    password: str = Field(..., min_length=4, description="Contraseña del cliente.")


class CustomerUpdateRequest(BaseModel):
    """Datos modificables de un cliente existente."""
    model_config = ConfigDict(json_schema_extra={"example": {
        "nombre": "Ana", "apellido": "Pérez", "telefono": "1122334455",
        "fechaNacimiento": "1990-05-20",
    }})
    nombre: str
    apellido: str
    telefono: str = ""
    fechaNacimiento: str | None = None


class CustomerValidationResponse(BaseModel):
    existe: bool
    activo: bool
    puede_alquilar: bool


class CustomerOperationResponse(BaseModel):
    exito: bool
    mensaje: str
