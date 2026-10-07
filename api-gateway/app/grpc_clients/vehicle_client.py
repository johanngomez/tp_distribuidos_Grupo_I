import grpc

from app.generated import vehicle_pb2
from app.generated import vehicle_pb2_grpc
from app.grpc_clients.errors import GrpcServiceError
from app.schemas.vehicles import VehicleResponse


class VehicleGrpcClient:
    def __init__(self, target: str, timeout_seconds: float):
        self.target = target
        self.timeout_seconds = timeout_seconds

    async def list_vehicles(self) -> list[VehicleResponse]:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)

                response = await stub.ListVehicles(
                    vehicle_pb2.ListVehiclesRequest(),
                    timeout=self.timeout_seconds,
                )

                return [
                    VehicleResponse(
                        id=vehicle.id,
                        patente=vehicle.patente,
                        marca=vehicle.marca,
                        modelo=vehicle.modelo,
                        anio=vehicle.anio,
                        color=(
                            vehicle.color
                            if vehicle.HasField("color")
                            else None
                        ),
                        tipo=vehicle_pb2.VehicleType.Name(vehicle.tipo),
                        precioDiario=vehicle.precio_diario,
                        estado=vehicle_pb2.VehicleStatus.Name(vehicle.estado),
                        activo=vehicle.activo,
                    )
                    for vehicle in response.vehicles
                ]

        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details()
                or "Error al comunicarse con Vehicle Service",
            ) from error