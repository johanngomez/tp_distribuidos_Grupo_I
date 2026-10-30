import grpc

from app.generated.rentar.vehicle.v1 import vehicle_pb2
from app.generated.rentar.vehicle.v1 import vehicle_pb2_grpc
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
                        tipo=_enum_label(vehicle_pb2.VehicleType.Name(vehicle.tipo), "VEHICLE_TYPE_"),
                        precioDiario=vehicle.precio_diario,
                        estado=_enum_label(vehicle_pb2.VehicleStatus.Name(vehicle.estado), "VEHICLE_STATUS_"),
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

    async def create_vehicle(self, data: dict) -> VehicleResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)
                vehicle = await stub.CreateVehicle(
                    vehicle_pb2.CreateVehicleRequest(**data),
                    timeout=self.timeout_seconds,
                )
                return self._to_response(vehicle)
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Vehicle Service",
            ) from error

    async def get_vehicle(self, vehicle_id: int) -> VehicleResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)
                vehicle = await stub.GetVehicle(
                    vehicle_pb2.GetVehicleRequest(id=vehicle_id),
                    timeout=self.timeout_seconds,
                )
                return VehicleResponse(
                    id=vehicle.id,
                    patente=vehicle.patente,
                    marca=vehicle.marca,
                    modelo=vehicle.modelo,
                    anio=vehicle.anio,
                    color=vehicle.color if vehicle.HasField("color") else None,
                    tipo=_enum_label(vehicle_pb2.VehicleType.Name(vehicle.tipo), "VEHICLE_TYPE_"),
                    precioDiario=vehicle.precio_diario,
                    estado=_enum_label(vehicle_pb2.VehicleStatus.Name(vehicle.estado), "VEHICLE_STATUS_"),
                    activo=vehicle.activo,
                )
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Vehicle Service",
            ) from error

    async def update_vehicle(self, vehicle_id: int, data: dict) -> VehicleResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)
                vehicle = await stub.UpdateVehicle(
                    vehicle_pb2.UpdateVehicleRequest(id=vehicle_id, **data),
                    timeout=self.timeout_seconds,
                )
                return self._to_response(vehicle)
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Vehicle Service",
            ) from error

    async def delete_vehicle(self, vehicle_id: int) -> bool:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)
                response = await stub.DeleteVehicle(
                    vehicle_pb2.DeleteVehicleRequest(id=vehicle_id),
                    timeout=self.timeout_seconds,
                )
                return response.success
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Vehicle Service",
            ) from error

    async def register_external_vehicle(self, data: dict) -> VehicleResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)
                vehicle = await stub.RegisterExternalVehicle(
                    vehicle_pb2.RegisterExternalVehicleRequest(**data),
                    timeout=self.timeout_seconds,
                )
                return self._to_response(vehicle)
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al registrar vehiculo externo",
            ) from error

    @staticmethod
    def _to_response(vehicle) -> VehicleResponse:
        return VehicleResponse(
            id=vehicle.id,
            patente=vehicle.patente,
            marca=vehicle.marca,
            modelo=vehicle.modelo,
            anio=vehicle.anio,
            color=vehicle.color if vehicle.HasField("color") else None,
            tipo=_enum_label(vehicle_pb2.VehicleType.Name(vehicle.tipo), "VEHICLE_TYPE_"),
            precioDiario=vehicle.precio_diario,
            estado=_enum_label(vehicle_pb2.VehicleStatus.Name(vehicle.estado), "VEHICLE_STATUS_"),
            activo=vehicle.activo,
        )


def _enum_label(value: str, prefix: str) -> str:
    return value.removeprefix(prefix)
