import grpc

from app.generated import customer_pb2
from app.generated import customer_pb2_grpc
from app.grpc_clients.errors import GrpcServiceError
from app.schemas.customers import (
    CustomerOperationResponse,
    CustomerResponse,
    CustomerValidationResponse,
)


class CustomerGrpcClient:
    def __init__(self, target: str, timeout_seconds: float):
        self.target = target
        self.timeout_seconds = timeout_seconds

    async def login(self, email: str, password: str) -> dict[str, str | bool]:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = customer_pb2_grpc.CustomerServiceStub(channel)
                response = await stub.Login(
                    customer_pb2.LoginRequest(email=email, password=password),
                    timeout=self.timeout_seconds,
                )
                return {
                    "token": response.token,
                    "tipo": response.tipo,
                    "esAdmin": response.es_admin,
                }
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    async def get_customer(self, customer_id: int) -> CustomerResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = customer_pb2_grpc.CustomerServiceStub(channel)
                response = await stub.GetCliente(
                    customer_pb2.ClienteRequest(id=customer_id),
                    timeout=self.timeout_seconds,
                )
                return CustomerResponse(
                    id=response.id,
                    documento=response.documento,
                    nombre=response.nombre,
                    apellido=response.apellido,
                    email=response.email,
                    telefono=response.telefono,
                    fecha_nacimiento=(response.fecha_nacimiento or None),
                    activo=response.activo,
                    es_admin=response.es_admin,
                )
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Customer Service",
            ) from error

    async def list_customers(self) -> list[CustomerResponse]:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = customer_pb2_grpc.CustomerServiceStub(channel)
                response = await stub.ListarClientes(
                    customer_pb2.ListarClientesRequest(),
                    timeout=self.timeout_seconds,
                )
                return [self._to_response(item) for item in response.clientes]
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    async def get_customer_by_email(self, email: str) -> CustomerResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = customer_pb2_grpc.CustomerServiceStub(channel)
                response = await stub.ObtenerClientePorEmail(
                    customer_pb2.ClienteEmailRequest(email=email),
                    timeout=self.timeout_seconds,
                )
                return self._to_response(response)
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    async def create_customer(self, data: dict[str, str]) -> CustomerResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = customer_pb2_grpc.CustomerServiceStub(channel)
                response = await stub.CrearCliente(
                    customer_pb2.CrearClienteRequest(**data),
                    timeout=self.timeout_seconds,
                )
                return self._to_response(response)
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    async def update_customer(self, customer_id: int, data: dict[str, str]) -> CustomerResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = customer_pb2_grpc.CustomerServiceStub(channel)
                response = await stub.ModificarCliente(
                    customer_pb2.ModificarClienteRequest(id=customer_id, **data),
                    timeout=self.timeout_seconds,
                )
                return self._to_response(response)
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    async def deactivate_customer(self, customer_id: int) -> CustomerOperationResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = customer_pb2_grpc.CustomerServiceStub(channel)
                response = await stub.DarDeBajaCliente(
                    customer_pb2.DarDeBajaClienteRequest(id=customer_id),
                    timeout=self.timeout_seconds,
                )
                return CustomerOperationResponse(
                    exito=response.exito,
                    mensaje=response.mensaje,
                )
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    @staticmethod
    def _to_response(response) -> CustomerResponse:
        return CustomerResponse(
            id=response.id,
            documento=response.documento,
            nombre=response.nombre,
            apellido=response.apellido,
            email=response.email,
            telefono=response.telefono,
            fecha_nacimiento=(response.fecha_nacimiento or None),
            activo=response.activo,
            es_admin=response.es_admin,
        )

    async def validate_customer(
        self,
        customer_id: int,
    ) -> CustomerValidationResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = customer_pb2_grpc.CustomerServiceStub(channel)
                response = await stub.ValidarCliente(
                    customer_pb2.ClienteRequest(id=customer_id),
                    timeout=self.timeout_seconds,
                )
                return CustomerValidationResponse(
                    existe=response.existe,
                    activo=response.activo,
                    puede_alquilar=response.puede_alquilar,
                )
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Customer Service",
            ) from error


def _service_error(error: grpc.aio.AioRpcError) -> GrpcServiceError:
    return GrpcServiceError(
        code=error.code(),
        details=error.details() or "Error al comunicarse con Customer Service",
    )
