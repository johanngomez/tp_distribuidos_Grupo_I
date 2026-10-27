from typing import Annotated

import grpc
from fastapi import APIRouter, Depends, HTTPException, Request, status

from app.grpc_clients.customer_client import CustomerGrpcClient
from app.grpc_clients.errors import GrpcServiceError
from app.schemas.customers import (
    CustomerCreateRequest,
    CustomerOperationResponse,
    CustomerResponse,
    CustomerUpdateRequest,
)
from app.security.dependencies import get_current_user
from app.security.jwt import AuthenticatedUser


router = APIRouter(prefix="/api/clientes", tags=["Clientes"])
CurrentUser = Annotated[AuthenticatedUser, Depends(get_current_user)]


def _customer_client(request: Request) -> CustomerGrpcClient:
    settings = request.app.state.settings
    return CustomerGrpcClient(
        target=settings.customer_grpc_target,
        timeout_seconds=settings.grpc_timeout_seconds,
    )


def _map_error(error: GrpcServiceError) -> HTTPException:
    code = error.code
    code_to_http = {
        grpc.StatusCode.NOT_FOUND: status.HTTP_404_NOT_FOUND,
        grpc.StatusCode.INVALID_ARGUMENT: status.HTTP_400_BAD_REQUEST,
        grpc.StatusCode.UNAVAILABLE: status.HTTP_503_SERVICE_UNAVAILABLE,
        grpc.StatusCode.DEADLINE_EXCEEDED: status.HTTP_504_GATEWAY_TIMEOUT,
    }
    detail = "Customer Service no disponible" if code == grpc.StatusCode.UNAVAILABLE else error.details
    return HTTPException(
        status_code=code_to_http.get(code, status.HTTP_502_BAD_GATEWAY),
        detail=detail,
    )


@router.get(
    "", response_model=list[CustomerResponse], summary="Listar clientes",
    description="Obtiene los clientes registrados. Requiere autenticación.",
    responses={401: {"description": "Token ausente o inválido."}, 503: {"description": "Customer Service no disponible."}},
)
async def list_customers(request: Request, _user: CurrentUser) -> list[CustomerResponse]:
    try:
        return await _customer_client(request).list_customers()
    except GrpcServiceError as error:
        raise _map_error(error) from error


@router.get("/me", response_model=CustomerResponse, summary="Obtener cliente autenticado", description="Obtiene el perfil asociado al JWT actual.")
async def current_customer(request: Request, user: CurrentUser) -> CustomerResponse:
    try:
        return await _customer_client(request).get_customer_by_email(user.email)
    except GrpcServiceError as error:
        raise _map_error(error) from error


@router.get("/{customer_id}", response_model=CustomerResponse, summary="Obtener cliente por ID", description="Busca un cliente por su identificador.", responses={404: {"description": "Cliente inexistente."}})
async def get_customer(
    customer_id: int,
    request: Request,
    _user: CurrentUser,
) -> CustomerResponse:
    try:
        return await _customer_client(request).get_customer(customer_id)
    except GrpcServiceError as error:
        raise _map_error(error) from error


@router.post(
    "", response_model=CustomerResponse, status_code=status.HTTP_201_CREATED,
    summary="Registrar cliente", description="Crea un nuevo cliente.",
    responses={201: {"description": "Cliente creado."}, 400: {"description": "Datos inválidos."}, 503: {"description": "Customer Service no disponible."}},
)
async def create_customer(data: CustomerCreateRequest, request: Request) -> CustomerResponse:
    try:
        return await _customer_client(request).create_customer(
            {
                "documento": data.documento,
                "nombre": data.nombre,
                "apellido": data.apellido,
                "email": data.email,
                "telefono": data.telefono,
                "fecha_nacimiento": data.fechaNacimiento or "",
                "password": data.password,
            }
        )
    except GrpcServiceError as error:
        raise _map_error(error) from error


@router.put("/{customer_id}", response_model=CustomerResponse, summary="Actualizar cliente", description="Actualiza los datos editables de un cliente.", responses={404: {"description": "Cliente inexistente."}})
async def update_customer(
    customer_id: int,
    data: CustomerUpdateRequest,
    request: Request,
    _user: CurrentUser,
) -> CustomerResponse:
    try:
        return await _customer_client(request).update_customer(
            customer_id,
            {
                "nombre": data.nombre,
                "apellido": data.apellido,
                "telefono": data.telefono,
                "fecha_nacimiento": data.fechaNacimiento or "",
            },
        )
    except GrpcServiceError as error:
        raise _map_error(error) from error


@router.delete("/{customer_id}", response_model=CustomerOperationResponse, summary="Dar de baja cliente", description="Realiza la baja lógica de un cliente sin eliminar su historial.", responses={404: {"description": "Cliente inexistente."}, 200: {"description": "Baja realizada."}})
async def deactivate_customer(
    customer_id: int,
    request: Request,
    _user: CurrentUser,
) -> CustomerOperationResponse:
    try:
        return await _customer_client(request).deactivate_customer(customer_id)
    except GrpcServiceError as error:
        raise _map_error(error) from error
