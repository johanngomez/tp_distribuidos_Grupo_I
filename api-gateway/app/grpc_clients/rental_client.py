import grpc

from app.generated import rental_pb2
from app.generated import rental_pb2_grpc
from app.grpc_clients.errors import GrpcServiceError
from app.schemas.rentals import (
    RentalCancellationResponse,
    RentalHistoryResponse,
    RentalResponse,
    EstadoReserva,
)

MAPEO_ESTADOS_GRPC = {
    0: "SIN_ESPECIFICAR",
    1: "EN_CURSO",
    2: "CONFIRMADA",
    3: "FINALIZADA",
    4: "CANCELADA"
}


def _rental_response(response) -> RentalResponse:

    texto_estado = MAPEO_ESTADOS_GRPC[response.estado]

    return RentalResponse(
        id=response.id,
        cliente_id=response.cliente_id,
        vehiculo_id=response.vehiculo_id,
        fecha_inicio=response.fecha_inicio,
        fecha_fin=response.fecha_fin,
        precio_diario=response.precio_diario,
        importe_total=response.importe_total,
        estado=EstadoReserva(texto_estado),
    )


class RentalGrpcClient:
    def __init__(self, target: str, timeout_seconds: float):
        self.target = target
        self.timeout_seconds = timeout_seconds

    async def create_rental(
        self,
        customer_id: int,
        vehicle_id: int,
        start_date: str,
        end_date: str,
    ) -> RentalResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = rental_pb2_grpc.RentalServiceStub(channel)
                response = await stub.CrearReserva(
                    rental_pb2.CrearReservaRequest(
                        cliente_id=customer_id,
                        vehiculo_id=vehicle_id,
                        fecha_inicio=start_date,
                        fecha_fin=end_date,
                    ),
                    timeout=self.timeout_seconds,
                )
                return _rental_response(response)
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    async def list_rentals(
        self,
        *,
        email: str = "",
        customer_id: int = 0,
        vehicle_id: int = 0,
        start_date: str = "",
        end_date: str = "",
    ) -> list[RentalResponse]:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = rental_pb2_grpc.RentalServiceStub(channel)
                response = await stub.ConsultarReservas(
                    rental_pb2.ConsultarReservasRequest(
                        email_cliente=email,
                        cliente_id=customer_id,
                        vehiculo_id=vehicle_id,
                        fecha_inicio=start_date,
                        fecha_fin=end_date,
                    ),
                    timeout=self.timeout_seconds,
                )
                print("RESPUESTA GRPC COMPLETA:", repr(response), flush=True)

                for item in response.reservas:
                    print(
                        f"Reserva ID={item.id}, "
                        f"cliente_id={item.cliente_id}, "
                        f"vehiculo_id={item.vehiculo_id}",
                        flush=True,
                    )
                return [_rental_response(item) for item in response.reservas]
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    async def cancel_rental(
        self,
        rental_id: int,
        customer_email: str,
    ) -> RentalCancellationResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = rental_pb2_grpc.RentalServiceStub(channel)
                response = await stub.CancelarReserva(
                    rental_pb2.CancelarReservaRequest(
                        reserva_id=rental_id,
                        cliente_email=customer_email,
                    ),
                    timeout=self.timeout_seconds,
                )
                return RentalCancellationResponse(
                    exito=response.exito,
                    reserva=(
                        _rental_response(response.reserva)
                        if response.HasField("reserva")
                        else None
                    ),
                )
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error

    async def customer_history(
        self,
        customer_id: int,
    ) -> list[RentalHistoryResponse]:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = rental_pb2_grpc.RentalServiceStub(channel)
                response = await stub.ConsultarHistorialCliente(
                    rental_pb2.ConsultarHistorialClienteRequest(
                        cliente_id=customer_id,
                    ),
                    timeout=self.timeout_seconds,
                )
                return [
                    RentalHistoryResponse(
                        vehiculo=item.vehiculo,
                        patente=item.patente,
                        fecha_inicio=item.fecha_inicio,
                        fecha_fin=item.fecha_fin,
                        cantidad_dias=item.cantidad_dias,
                        importe_total=item.importe_total,
                        estado=EstadoReserva(MAPEO_ESTADOS_GRPC[item.estado]),
                    )
                    for item in response.reservas
                ]
        except grpc.aio.AioRpcError as error:
            raise _service_error(error) from error


def _service_error(error: grpc.aio.AioRpcError) -> GrpcServiceError:
    return GrpcServiceError(
        code=error.code(),
        details=error.details() or "Error al comunicarse con Rental Service",
    )
