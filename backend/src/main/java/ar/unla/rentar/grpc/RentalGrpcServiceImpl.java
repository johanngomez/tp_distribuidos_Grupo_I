package ar.unla.rentar.grpc;


import ar.unla.rentar.grpc.rental.v1.CancelarReservaRequest;
import ar.unla.rentar.grpc.rental.v1.CancelarReservaResponse;
import ar.unla.rentar.grpc.rental.v1.ConsultarHistorialClienteRequest;
import ar.unla.rentar.grpc.rental.v1.ConsultarHistorialClienteResponse;
import ar.unla.rentar.grpc.rental.v1.ConsultarReservasRequest;
import ar.unla.rentar.grpc.rental.v1.ConsultarReservasResponse;
import ar.unla.rentar.grpc.rental.v1.CrearReservaRequest;
import ar.unla.rentar.grpc.rental.v1.RentalServiceGrpc;
import ar.unla.rentar.grpc.rental.v1.ReservaResponse;
import ar.unla.rentar.grpc.vehicle.v1.VehicleStatus;
import ar.unla.rentar.grpc.rental.v1.EstadoReserva;
import ar.unla.rentar.grpc.rental.v1.HistorialResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;

import ar.unla.rentar.service.ReservaService;
import ar.unla.rentar.dto.ReservaCreateDTO;
import ar.unla.rentar.dto.ReservaFiltroDTO;
import ar.unla.rentar.dto.ReservaResponseDTO;
import ar.unla.rentar.model.Reserva;
import ar.unla.rentar.model.TipoVehiculo;

@GrpcService
public class RentalGrpcServiceImpl extends RentalServiceGrpc.RentalServiceImplBase {

    @Autowired
    private ReservaService reservaService;

    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<ReservaResponse> responseObserver) {
    try {

            if (request.getClienteId() <= 0) {
                throw new IllegalArgumentException("El ID de cliente debe ser un valor válido.");
            }

            if (request.getVehiculoId() <= 0) {
                throw new IllegalArgumentException("El ID de vehículo debe ser un valor válido.");
            }

            if (request.getFechaInicio() == null || request.getFechaInicio().isBlank()) {
                throw new IllegalArgumentException("El campo 'fechaInicio' no puede estar vacío.");
            }

            if (request.getFechaFin() == null || request.getFechaFin().isBlank()) {
                throw new IllegalArgumentException("El campo 'fechaFin' no puede estar vacío.");
            }


        ReservaCreateDTO dto = new ReservaCreateDTO();

        dto.setClienteId(request.getClienteId());
        dto.setVehiculoId(request.getVehiculoId());
        dto.setFechaInicio(LocalDateTime.parse(request.getFechaInicio()));
        dto.setFechaFin(LocalDateTime.parse(request.getFechaFin()));

        ReservaResponseDTO reserva = reservaService.crearReserva(dto);

        ReservaResponse response = ReservaResponse.newBuilder()
                .setId(reserva.getId())
                .setFechaInicio(reserva.getFechaInicio().toString())
                .setFechaFin(reserva.getFechaFin().toString())
                .setPrecioDiario(reserva.getPrecioDiario())
                .setImporteTotal(reserva.getImporteTotal())
                .setEstado(EstadoReserva.forNumber(reserva.getEstado().ordinal()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
        responseObserver.onError(
            Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException()
        );
    } catch (Exception e) {
        responseObserver.onError(
            Status.INTERNAL
                .withDescription("Error al crear la reserva: " + e.getMessage())
                .asRuntimeException()
        );
    }
}

@Override
public void cancelarReserva(CancelarReservaRequest request, StreamObserver<CancelarReservaResponse> responseObserver) {
    try {
        if (request.getReservaId() <= 0) {
                throw new IllegalArgumentException("El ID de reserva debe ser un valor válido.");
            }

        Long reservaId = request.getReservaId();
        
        ReservaResponseDTO reserva = reservaService.cancelarReserva(reservaId);

        ReservaResponse response = ReservaResponse.newBuilder()
                .setId(reserva.getId())
                .setFechaInicio(reserva.getFechaInicio().toString())
                .setFechaFin(reserva.getFechaFin().toString())
                .setPrecioDiario(reserva.getPrecioDiario())
                .setImporteTotal(reserva.getImporteTotal())
                .setEstado(EstadoReserva.forNumber(reserva.getEstado().ordinal()))
                .build();




        CancelarReservaResponse cancelarResponse = CancelarReservaResponse.newBuilder()
                .setExito(true)
                .setReserva(response)
                .build();

                responseObserver.onNext(cancelarResponse);
                responseObserver.onCompleted();


            } catch (IllegalArgumentException e) {
        responseObserver.onError(
            Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asRuntimeException()
        );
    } catch (Exception e) {
        responseObserver.onError(
            Status.INTERNAL
                .withDescription("Error al cancelar reserva: " + e.getMessage())
                .asRuntimeException()
        );
    }
}

@Override
public void consultarReservas(ConsultarReservasRequest request, StreamObserver<ConsultarReservasResponse> responseObserver) {
    try {



        ReservaFiltroDTO filtro = new ReservaFiltroDTO();

        filtro.setClienteId(request.getClienteId());
        filtro.setVehiculoId(request.getVehiculoId());
        filtro.setTipoVehiculo(request.getTipoVehiculoValue() != 0 && !request.getTipoVehiculo().name().isBlank() && !(request.getTipoVehiculoValue() == VehicleStatus.VEHICLE_STATUS_UNSPECIFIED_VALUE) ? TipoVehiculo.valueOf(request.getTipoVehiculo().name()) : null);
        filtro.setEstado(request.getEstado() != null && !request.getEstado().name().isBlank() && !(request.getEstadoValue() == EstadoReserva.SIN_ESPECIFICAR_VALUE) ? ar.unla.rentar.model.EstadoReserva.valueOf(request.getEstado().name()) : null);
        filtro.setFechaInicioDesde(request.getFechaInicio() != null && !request.getFechaInicio().isBlank() ? LocalDateTime.parse(request.getFechaInicio()) : null);
        filtro.setFechaInicioHasta(request.getFechaFin() != null && !request.getFechaFin().isBlank() ? LocalDateTime.parse(request.getFechaFin()) : null);


        String email = request.getEmailCliente() != null && !request.getEmailCliente().isBlank() ? request.getEmailCliente() : null;

        List<Reserva> reservas = reservaService.consultarReservas(filtro, email);

        

        List<ReservaResponse> reservasGrpc = reservas.stream().map(reserva -> ReservaResponse.newBuilder()
                .setId(reserva.getId())
                .setFechaInicio(reserva.getFechaInicio().toString())
                .setFechaFin(reserva.getFechaFin().toString())
                .setPrecioDiario(reserva.getPrecioDiario())
                .setImporteTotal(reserva.getImporteTotal())
                .setEstado(EstadoReserva.forNumber(reserva.getEstado().ordinal()))
                .build()).toList();

               ConsultarReservasResponse response = ConsultarReservasResponse.newBuilder()
                .addAllReservas(reservasGrpc)
                .build();


        responseObserver.onNext(response);
        responseObserver.onCompleted();

    } catch (IllegalArgumentException e) {
        responseObserver.onError(
            Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asRuntimeException()
        );
        } catch (NoSuchElementException e) {
    responseObserver.onError(
        Status.NOT_FOUND
            .withDescription(e.getMessage())
            .asRuntimeException()
    );
    } catch (Exception e) {
        e.printStackTrace();
        responseObserver.onError(
            Status.INTERNAL
                .withDescription("Error al consultar reserva: " + e.getMessage())
                .asRuntimeException()
        );
    }



    
}

@Override
public void consultarHistorialCliente(ConsultarHistorialClienteRequest request, StreamObserver<ConsultarHistorialClienteResponse> responseObserver) {

            try {

                if (request.getClienteId() <= 0) {
                throw new IllegalArgumentException("El ID de cliente debe ser un valor válido.");
                }



            List<Reserva> reservas = reservaService.obtenerPorClienteId(request.getClienteId());

            ConsultarHistorialClienteResponse.Builder responseBuilder = 
                ConsultarHistorialClienteResponse.newBuilder();

            for (Reserva reserva : reservas) {
                HistorialResponse item = HistorialResponse.newBuilder()
                    .setVehiculo(reserva.getVehiculo().getMarca()  != null || reserva.getVehiculo().getModelo() != null ? reserva.getVehiculo().getMarca() + " " + reserva.getVehiculo().getModelo() : "N/A")
                    .setPatente(reserva.getVehiculo().getPatente() != null ? reserva.getVehiculo().getPatente() : "ERROR en la patente")
                    .setFechaInicio(reserva.getFechaInicio().toString())
                    .setFechaFin(reserva.getFechaFin().toString())
                    .setCantidadDias(ChronoUnit.DAYS.between(reserva.getFechaInicio(), reserva.getFechaFin()))
                    .setImporteTotal(reserva.getImporteTotal())
                    .setEstado(reserva.getEstado().name())
                    .build();

                    responseBuilder.addReservas(item);
            }
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

            } catch (IllegalArgumentException e) {
        responseObserver.onError(
            Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException()
        );
    } catch (Exception e) {
        responseObserver.onError(
            Status.INTERNAL
                .withDescription("Error al consultar el historial: " + e.getMessage())
                .asRuntimeException()
        );
    }
}
}

