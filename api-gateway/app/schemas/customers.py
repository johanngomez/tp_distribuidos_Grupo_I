from pydantic import BaseModel


class CustomerResponse(BaseModel):
    id_cliente: int
    documento: str
    nombre: str
    apellido: str
    email: str
    telefono: str
    fecha_nacimiento: str | None
    activo: bool
    es_admin: bool


class CustomerCreateRequest(BaseModel):
    documento: str
    nombre: str
    apellido: str
    email: str
    telefono: str = ""
    fechaNacimiento: str | None = None
    password: str


class CustomerUpdateRequest(BaseModel):
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
