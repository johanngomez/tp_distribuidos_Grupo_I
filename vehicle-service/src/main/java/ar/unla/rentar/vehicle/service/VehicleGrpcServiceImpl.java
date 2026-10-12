package ar.unla.rentar.vehicle.service;

import ar.unla.rentar.grpc.vehicle.v1.*;
import ar.unla.rentar.vehicle.model.Vehiculo;
import ar.unla.rentar.vehicle.model.EstadoVehiculo;
import ar.unla.rentar.vehicle.model.TipoVehiculo;
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
    public void listarVehiculos(ListarVehiculosRequest request, StreamObserver<ListarVehiculosResponse> responseObserver) {
        List<Vehiculo> vehiculos = vehiculoRepository.findAll();
        ListarVehiculosResponse.Builder responseBuilder = ListarVehiculosResponse.newBuilder();

        for (Vehiculo v : vehiculos) {
            responseBuilder.addVehiculos(mapToProto(v));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void crearVehiculo(CrearVehiculoRequest request, StreamObserver<ar.unla.rentar.grpc.vehicle.v1.Vehiculo> responseObserver) {
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
    public void obtenerVehiculo(ObtenerVehiculoRequest request, StreamObserver<ar.unla.rentar.grpc.vehicle.v1.Vehiculo> responseObserver) {
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
    public void actualizarVehiculo(ActualizarVehiculoRequest request, StreamObserver<ar.unla.rentar.grpc.vehicle.v1.Vehiculo> responseObserver) {
        Vehiculo vehiculo = vehiculoRepository.findById(request.getId()).orElse(null);

        if (vehiculo == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Vehículo no encontrado con ID: " + request.getId())
                    .asRuntimeException());
            return;
        }

        vehiculo.setPatente(request.getPatente());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setColor(request.getColor());
        vehiculo.setTipo(TipoVehiculo.valueOf(request.getTipo().name()));
        vehiculo.setPrecioDiario(request.getPrecioDiario());

        responseObserver.onNext(mapToProto(vehiculoRepository.save(vehiculo)));
        responseObserver.onCompleted();
    }

    @Override
    public void eliminarVehiculo(EliminarVehiculoRequest request, StreamObserver<ar.unla.rentar.grpc.vehicle.v1.EliminarVehiculoResponse> responseObserver) {
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

        ar.unla.rentar.grpc.vehicle.v1.EliminarVehiculoResponse response = ar.unla.rentar.grpc.vehicle.v1.EliminarVehiculoResponse.newBuilder()
                .setExito(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private ar.unla.rentar.grpc.vehicle.v1.Vehiculo mapToProto(Vehiculo v) {
        return ar.unla.rentar.grpc.vehicle.v1.Vehiculo.newBuilder()
                .setId(v.getId())
                .setPatente(v.getPatente())
                .setMarca(v.getMarca())
                .setModelo(v.getModelo())
                .setAnio(v.getAnio())
                .setColor(v.getColor() != null ? v.getColor() : "")
                .setTipo(ar.unla.rentar.grpc.vehicle.v1.TipoVehiculo.valueOf(v.getTipo().name()))
                .setPrecioDiario(v.getPrecioDiario())
                .setEstado(ar.unla.rentar.grpc.vehicle.v1.EstadoVehiculo.valueOf(v.getEstado().name()))
                .setActivo(v.getActivo())
                .build();
    }
}
