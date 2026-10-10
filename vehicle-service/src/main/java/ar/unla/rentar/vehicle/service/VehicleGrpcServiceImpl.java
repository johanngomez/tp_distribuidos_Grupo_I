package ar.unla.rentar.vehicle.service;

import ar.unla.rentar.grpc.vehicle.v1.*;
import ar.unla.rentar.vehicle.model.EstadoVehiculo;
import ar.unla.rentar.vehicle.model.TipoVehiculo;
import ar.unla.rentar.vehicle.model.Vehiculo;
import ar.unla.rentar.vehicle.repository.VehiculoRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class VehicleGrpcServiceImpl extends VehicleServiceGrpc.VehicleServiceImplBase {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Override
    public void listVehicles(ListVehiclesRequest request, StreamObserver<ListVehiclesResponse> responseObserver) {
        List<Vehiculo> vehiculos = vehiculoRepository.findAll();
        ListVehiclesResponse.Builder responseBuilder = ListVehiclesResponse.newBuilder();

        for (Vehiculo v : vehiculos) {
            responseBuilder.addVehicles(mapToProto(v));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createVehicle(CreateVehicleRequest request, StreamObserver<Vehicle> responseObserver) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente(request.getPatente());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setColor(request.getColor());
        vehiculo.setTipo(TipoVehiculo.valueOf(request.getTipo().name()));
        vehiculo.setPrecioDiario(request.getPrecioDiario());
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setActivo(true);

        Vehiculo guardado = vehiculoRepository.save(vehiculo);

        responseObserver.onNext(mapToProto(guardado));
        responseObserver.onCompleted();
    }

    @Override
    public void getVehicle(GetVehicleRequest request, StreamObserver<Vehicle> responseObserver) {
        Vehiculo vehiculo = vehiculoRepository.findById(request.getId()).orElse(null);

        if (vehiculo == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Vehículo no encontrado con ID: " + request.getId())
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(mapToProto(vehiculo));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteVehicle(DeleteVehicleRequest request, StreamObserver<DeleteVehicleResponse> responseObserver) {
        Vehiculo vehiculo = vehiculoRepository.findById(request.getId()).orElse(null);

        if (vehiculo == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Vehículo no encontrado con ID: " + request.getId())
                    .asRuntimeException());
            return;
        }

        // Borrado lógico
        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);

        DeleteVehicleResponse response = DeleteVehicleResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Vehicle mapToProto(Vehiculo v) {
        return Vehicle.newBuilder()
                .setId(v.getId())
                .setPatente(v.getPatente())
                .setMarca(v.getMarca())
                .setModelo(v.getModelo())
                .setAnio(v.getAnio())
                .setColor(v.getColor() != null ? v.getColor() : "")
                .setTipo(VehicleType.valueOf(v.getTipo().name()))
                .setPrecioDiario(v.getPrecioDiario())
                .setEstado(VehicleStatus.valueOf(v.getEstado().name()))
                .setActivo(v.getActivo())
                .build();
    }
}