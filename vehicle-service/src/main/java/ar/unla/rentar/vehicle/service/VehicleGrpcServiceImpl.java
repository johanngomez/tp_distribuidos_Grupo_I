package ar.unla.rentar.vehicle.service;

import ar.unla.rentar.grpc.vehicle.v1.*;
import ar.unla.rentar.vehicle.model.EstadoVehiculo;
import ar.unla.rentar.vehicle.model.TipoVehiculo;
import ar.unla.rentar.vehicle.model.Vehiculo;
import ar.unla.rentar.vehicle.repository.VehiculoRepository;
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
            Vehicle vehicleProto = Vehicle.newBuilder()
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

            responseBuilder.addVehicles(vehicleProto);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createVehicle(CreateVehicleRequest request, StreamObserver<Vehicle> responseObserver) {
        // 1. Mapear request gRPC a Entidad JPA
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

        // 2. Guardar en MySQL
        Vehiculo guardado = vehiculoRepository.save(vehiculo);

        // 3. Mapear Entidad JPA a respuesta gRPC
        Vehicle response = Vehicle.newBuilder()
                .setId(guardado.getId())
                .setPatente(guardado.getPatente())
                .setMarca(guardado.getMarca())
                .setModelo(guardado.getModelo())
                .setAnio(guardado.getAnio())
                .setColor(guardado.getColor() != null ? guardado.getColor() : "")
                .setTipo(VehicleType.valueOf(guardado.getTipo().name()))
                .setPrecioDiario(guardado.getPrecioDiario())
                .setEstado(VehicleStatus.valueOf(guardado.getEstado().name()))
                .setActivo(guardado.getActivo())
                .build();

        // 4. Enviar respuesta y finalizar
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}