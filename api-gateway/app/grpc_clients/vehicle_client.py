import grpc

from app.generated import vehicle_pb2
from app.generated import vehicle_pb2_grpc
from app.grpc_clients.errors import GrpcServiceError
from app.schemas.vehicles import VehicleResponse, TipoVehiculo, EstadoVehiculo

MAPEO_ESTADO_VEHICULO_GRPC = {
    0: "VEHICULO_ESTADO_SIN_ESPECIFICAR",
    1: "DISPONIBLE",
    2: "RESERVADO",
    3: "EN_ALQUILER"
}

MAPEO_TIPO_VEHICULO_GRPC = {
    0: "VEHICULO_TIPO_SIN_ESPECIFICAR",
    1: "SEDAN",
    2: "SUV",
    3: "PICKUP",
    4: "COUPE",
    5: "HATCHBACK"
}


class VehicleGrpcClient:
    def __init__(self, target: str, timeout_seconds: float):
        self.target = target
        self.timeout_seconds = timeout_seconds

    async def list_vehicles(self) -> list[VehicleResponse]:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)

                response = await stub.ListarVehiculos(
                    vehicle_pb2.ListarVehiculosRequest(),
                    timeout=self.timeout_seconds,
                )

                return [
                    VehicleResponse(
                        id=vehiculo.id,
                        patente=vehiculo.patente,
                        marca=vehiculo.marca,
                        modelo=vehiculo.modelo,
                        anio=vehiculo.anio,
                        color=(
                            vehiculo.color
                            if vehiculo.HasField("color")
                            else None
                        ),
                        tipo=TipoVehiculo(MAPEO_TIPO_VEHICULO_GRPC[vehiculo.tipo]),
                        precioDiario=vehiculo.precio_diario,
                        estado=EstadoVehiculo(MAPEO_ESTADO_VEHICULO_GRPC[vehiculo.estado]),
                        activo=vehiculo.activo,
                    )
                    for vehiculo in response.vehiculos
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
                vehiculo = await stub.CrearVehiculo(
                    vehicle_pb2.CrearVehiculoRequest(**data),
                    timeout=self.timeout_seconds,
                )
                return self._to_response(vehiculo)
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Vehicle Service",
            ) from error

    async def get_vehiculo(self, vehiculo_id: int) -> VehicleResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)
                vehiculo = await stub.ObtenerVehiculo(
                    vehicle_pb2.ObtenerVehiculoRequest(id=vehiculo_id),
                    timeout=self.timeout_seconds,
                )
                return VehicleResponse(
                    id=vehiculo.id,
                    patente=vehiculo.patente,
                    marca=vehiculo.marca,
                    modelo=vehiculo.modelo,
                    anio=vehiculo.anio,
                    color=vehiculo.color if vehiculo.HasField("color") else None,
                    tipo=TipoVehiculo(MAPEO_TIPO_VEHICULO_GRPC[vehiculo.tipo]),
                    precioDiario=vehiculo.precio_diario,
                    estado=EstadoVehiculo(MAPEO_ESTADO_VEHICULO_GRPC[vehiculo.estado]),
                    activo=vehiculo.activo,
                )
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Vehicle Service",
            ) from error

    async def update_vehiculo(self, vehiculo_id: int, data: dict) -> VehicleResponse:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)
                vehiculo = await stub.ActualizarVehiculo(
                    vehicle_pb2.ActualizarVehiculoRequest(id=vehiculo_id, **data),
                    timeout=self.timeout_seconds,
                )
                return self._to_response(vehiculo)
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Vehicle Service",
            ) from error

    async def delete_vehiculo(self, vehiculo_id: int) -> bool:
        try:
            async with grpc.aio.insecure_channel(self.target) as channel:
                stub = vehicle_pb2_grpc.VehicleServiceStub(channel)
                vehiculo = await stub.EliminarVehiculo(
                    vehicle_pb2.EliminarVehiculoRequest(id=vehiculo_id),
                    timeout=self.timeout_seconds,
                )
                return vehiculo.exito
        except grpc.aio.AioRpcError as error:
            raise GrpcServiceError(
                code=error.code(),
                details=error.details() or "Error al comunicarse con Vehicle Service",
            ) from error

    @staticmethod
    def _to_response(vehiculo) -> VehicleResponse:
        return VehicleResponse(
            id=vehiculo.id,
            patente=vehiculo.patente,
            marca=vehiculo.marca,
            modelo=vehiculo.modelo,
            anio=vehiculo.anio,
            color=vehiculo.color if vehiculo.HasField("color") else None,
            tipo=TipoVehiculo(MAPEO_TIPO_VEHICULO_GRPC[vehiculo.tipo]),
            precioDiario=vehiculo.precio_diario,
            estado=EstadoVehiculo(MAPEO_ESTADO_VEHICULO_GRPC[vehiculo.estado]),
            activo=vehiculo.activo,
        )
