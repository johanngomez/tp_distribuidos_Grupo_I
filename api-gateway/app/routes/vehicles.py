from google.protobuf.json_format import MessageToDict
from fastapi import APIRouter, HTTPException, status, Response
from pydantic import BaseModel
import grpc
import proto.vehicle_pb2 as vehicle_pb2
import proto.vehicle_pb2_grpc as vehicle_pb2_grpc

router = APIRouter(prefix="/vehicles", tags=["Vehicles"])

class VehicleCreate(BaseModel):
    patente: str
    marca: str
    modelo: str
    anio: int
    color: str
    tipo: str  # Ej: SEDAN, SUV, HATCHBACK, PICKUP, COUPE
    precioDiario: float

def get_grpc_stub():
    channel = grpc.insecure_channel('localhost:50051')
    return vehicle_pb2_grpc.VehicleServiceStub(channel)

@router.get("/")
def list_vehicles():
    try:
        stub = get_grpc_stub()
        request = vehicle_pb2.ListVehiclesRequest()
        response = stub.ListVehicles(request)
        
        vehicles_list = []
        for v in response.vehicles:
            vehicles_list.append({
                "id": v.id,
                "patente": v.patente,
                "marca": v.marca,
                "modelo": v.modelo,
                "anio": v.anio,
                "precioDiario": v.precio_diario,
                "color": v.color,
                "tipo": vehicle_pb2.VehicleType.Name(v.tipo),
                "estado": vehicle_pb2.VehicleStatus.Name(v.estado),
                "activo": v.activo
            })
            
        return {"vehicles": vehicles_list}
    except grpc.RpcError as e:
        raise HTTPException(status_code=503, detail=f"Error gRPC: {e.details()}")

@router.post("/", status_code=status.HTTP_201_CREATED)
def create_vehicle(vehicle: VehicleCreate):
    try:
        stub = get_grpc_stub()
        
        try:
            enum_tipo = vehicle_pb2.VehicleType.Value(vehicle.tipo.upper())
        except ValueError:
            raise HTTPException(status_code=400, detail=f"Tipo de vehículo inválido: {vehicle.tipo}")

        request = vehicle_pb2.CreateVehicleRequest(
            patente=vehicle.patente,
            marca=vehicle.marca,
            modelo=vehicle.modelo,
            anio=vehicle.anio,
            color=vehicle.color,
            tipo=enum_tipo,
            precio_diario=vehicle.precioDiario
        )
        
        v = stub.CreateVehicle(request)
        
        return {
            "id": v.id,
            "patente": v.patente,
            "marca": v.marca,
            "modelo": v.modelo,
            "anio": v.anio,
            "precioDiario": v.precio_diario,
            "color": v.color,
            "tipo": vehicle_pb2.VehicleType.Name(v.tipo),
            "estado": vehicle_pb2.VehicleStatus.Name(v.estado),
            "activo": v.activo
        }
    except grpc.RpcError as e:
        raise HTTPException(status_code=500, detail=f"gRPC Error [{e.code()}]: {e.details()}")
    except Exception as ex:
        raise HTTPException(status_code=500, detail=str(ex))

@router.get("/{vehicle_id}")
def get_vehicle(vehicle_id: int):
    try:
        stub = get_grpc_stub()
        request = vehicle_pb2.GetVehicleRequest(id=vehicle_id)
        v = stub.GetVehicle(request)
        
        return {
            "id": v.id,
            "patente": v.patente,
            "marca": v.marca,
            "modelo": v.modelo,
            "anio": v.anio,
            "precioDiario": v.precio_diario,
            "color": v.color,
            "tipo": vehicle_pb2.VehicleType.Name(v.tipo),
            "estado": vehicle_pb2.VehicleStatus.Name(v.estado),
            "activo": v.activo
        }
    except grpc.RpcError as e:
        if e.code() == grpc.StatusCode.NOT_FOUND:
            raise HTTPException(status_code=404, detail="Vehículo no encontrado")
        raise HTTPException(status_code=500, detail=f"Error gRPC: {e.details()}")

@router.delete("/{vehicle_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_vehicle(vehicle_id: int):
    try:
        stub = get_grpc_stub()
        request = vehicle_pb2.DeleteVehicleRequest(id=vehicle_id)
        stub.DeleteVehicle(request)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except grpc.RpcError as e:
        if e.code() == grpc.StatusCode.NOT_FOUND:
            raise HTTPException(status_code=404, detail="Vehículo no encontrado")
        raise HTTPException(status_code=500, detail=f"Error gRPC: {e.details()}")