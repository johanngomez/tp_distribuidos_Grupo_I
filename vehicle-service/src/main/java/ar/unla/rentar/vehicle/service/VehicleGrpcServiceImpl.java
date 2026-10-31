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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@GrpcService
public class VehicleGrpcServiceImpl extends VehicleServiceGrpc.VehicleServiceImplBase {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private VehicleEventPublisher eventPublisher;

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
        vehiculo.setTipo(toDomainType(request.getTipo()));
        vehiculo.setPrecioDiario(request.getPrecioDiario());
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setActivo(true);

        Vehiculo guardado;
        try {
            guardado = vehiculoRepository.saveAndFlush(vehiculo);
        } catch (DataIntegrityViolationException error) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("La patente ya esta registrada")
                    .asRuntimeException());
            return;
        }

        eventPublisher.publicarSolicitudVerificacion(guardado.getId(), guardado.getPatente());
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
    public void updateVehicle(UpdateVehicleRequest request, StreamObserver<Vehicle> responseObserver) {
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
        vehiculo.setTipo(toDomainType(request.getTipo()));
        vehiculo.setPrecioDiario(request.getPrecioDiario());

        try {
            responseObserver.onNext(mapToProto(vehiculoRepository.saveAndFlush(vehiculo)));
            responseObserver.onCompleted();
        } catch (DataIntegrityViolationException error) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("La patente ya esta registrada")
                    .asRuntimeException());
        }
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

        //borrado lógico
        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);

        DeleteVehicleResponse response = DeleteVehicleResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void registerExternalVehicle(RegisterExternalVehicleRequest request, StreamObserver<Vehicle> responseObserver) {
        
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente(request.getPatente());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setColor(request.getColor());
        vehiculo.setTipo(toDomainType(request.getTipo()));
        vehiculo.setPrecioDiario(request.getPrecioDiario());
        
        //estado inicial para vehículos externos
        vehiculo.setEstado(EstadoVehiculo.PENDIENTE); 
        vehiculo.setActivo(true);

        Vehiculo guardado;
        try {
            guardado = vehiculoRepository.saveAndFlush(vehiculo);
        } catch (DataIntegrityViolationException error) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("La patente ya esta registrada")
                    .asRuntimeException());
            return;
        }

        //crear y publicar el evento en RabbitMQ
        ar.unla.rentar.vehicle.events.VehiculoExternoRegistrado evento = 
            new ar.unla.rentar.vehicle.events.VehiculoExternoRegistrado(
                guardado.getId(),
                request.getClienteId(), 
                guardado.getPatente()
        );
        
        eventPublisher.publishVehiculoExternoRegistrado(evento); 

        responseObserver.onNext(mapToProto(guardado));
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
                .setTipo(toProtoType(v.getTipo()))
                .setPrecioDiario(v.getPrecioDiario())
                .setEstado(toProtoStatus(v.getEstado()))
                .setActivo(v.getActivo())
                .build();
    }

    private TipoVehiculo toDomainType(VehicleType type) {
        return TipoVehiculo.valueOf(type.name().replace("VEHICLE_TYPE_", ""));
    }

    private VehicleType toProtoType(TipoVehiculo type) {
        return VehicleType.valueOf("VEHICLE_TYPE_" + type.name());
    }

    private VehicleStatus toProtoStatus(EstadoVehiculo status) {
        return VehicleStatus.valueOf("VEHICLE_STATUS_" + status.name());
    }
}
