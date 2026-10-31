package ar.unla.rentar.rental.service;

import ar.unla.rentar.grpc.rental.v1.CancelarReservaRequest;
import ar.unla.rentar.grpc.rental.v1.CancelarReservaResponse;
import ar.unla.rentar.grpc.rental.v1.ConsultarHistorialClienteRequest;
import ar.unla.rentar.grpc.rental.v1.ConsultarHistorialClienteResponse;
import ar.unla.rentar.grpc.rental.v1.ConsultarReservasRequest;
import ar.unla.rentar.grpc.rental.v1.ConsultarReservasResponse;
import ar.unla.rentar.grpc.rental.v1.CrearReservaRequest;
import ar.unla.rentar.grpc.rental.v1.HistorialResponse;
import ar.unla.rentar.grpc.rental.v1.ReservaResponse;
import ar.unla.rentar.grpc.rental.v1.RentalServiceGrpc;
import ar.unla.rentar.rental.dto.ReservaConfirmadaEvent;
import ar.unla.rentar.rental.model.EstadoReserva;
import ar.unla.rentar.rental.model.Reserva;
import ar.unla.rentar.rental.repository.ReservaRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Servicio RPC responsable exclusivamente de la persistencia y reglas de reservas. */
@GrpcService
public class RentalGrpcServiceImpl extends RentalServiceGrpc.RentalServiceImplBase {
    private final ReservaRepository repository;
    private final ReservaEstadoService estadoService;
    private final RentalEventPublisher eventPublisher;

    public RentalGrpcServiceImpl(ReservaRepository repository, ReservaEstadoService estadoService, RentalEventPublisher eventPublisher) {
        this.repository = repository;
        this.estadoService = estadoService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<ReservaResponse> responseObserver) {
        try {
            if (request.getClienteId() <= 0 || request.getVehiculoId() <= 0) {
                throw invalid("Los IDs deben ser positivos");
            }
            LocalDateTime inicio = parse(request.getFechaInicio());
            LocalDateTime fin = parse(request.getFechaFin());
            if (!fin.isAfter(inicio)) {
                throw invalid("La fecha de fin debe ser posterior a la de inicio");
            }
            if (request.getPrecioDiario() <= 0) {
                throw invalid("El precio diario debe ser mayor que cero");
            }
            if (repository.existsByVehiculoIdAndEstadoInAndFechaInicioLessThanAndFechaFinGreaterThan(
                    request.getVehiculoId(),
                    List.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO),
                    fin,
                    inicio)) {
                throw Status.ALREADY_EXISTS.withDescription("El vehículo ya está reservado").asRuntimeException();
            }

            Reserva entity = new Reserva();
            entity.setClienteId(request.getClienteId());
            entity.setVehiculoId(request.getVehiculoId());
            entity.setFechaInicio(inicio);
            entity.setFechaFin(fin);
            entity.setPrecioDiario(request.getPrecioDiario());
            entity.setImporteTotal(request.getPrecioDiario()
                    * Math.max(1, (long) Math.ceil(Duration.between(inicio, fin).toHours() / 24.0)));
            entity.setEstado(EstadoReserva.CONFIRMADA);

            entity = repository.save(entity);
            eventPublisher.publicarReservaConfirmada(new ReservaConfirmadaEvent(
                    entity.getId(), entity.getVehiculoId(), entity.getFechaInicio(), entity.getFechaFin()));

            responseObserver.onNext(toProto(entity));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException error) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(error.getMessage()).asRuntimeException());
        } catch (RuntimeException error) {
            responseObserver.onError(error);
        }
    }

    @Override
    public void consultarReservas(ConsultarReservasRequest request, StreamObserver<ConsultarReservasResponse> responseObserver) {
        try {
            estadoService.actualizarFinalizadas();
            Long customerId = request.getClienteId() > 0 ? request.getClienteId() : null;
            LocalDateTime from = request.getFechaInicio().isBlank() ? null : parse(request.getFechaInicio());
            LocalDateTime to = request.getFechaFin().isBlank() ? null : parse(request.getFechaFin());
            Long vehicleId = request.getVehiculoId() > 0 ? request.getVehiculoId() : null;

            List<ReservaResponse> result = repository.findAll().stream()
                    .filter(item -> customerId == null || item.getClienteId().equals(customerId))
                    .filter(item -> vehicleId == null || item.getVehiculoId().equals(vehicleId))
                    .filter(item -> request.getEstadoValue() == 0 || item.getEstado().ordinal() == request.getEstadoValue())
                    .filter(item -> from == null || !item.getFechaInicio().isBefore(from))
                    .filter(item -> to == null || !item.getFechaFin().isAfter(to))
                    .map(this::toProto)
                    .collect(Collectors.toList());
            responseObserver.onNext(ConsultarReservasResponse.newBuilder().addAllReservas(result).build());
            responseObserver.onCompleted();
        } catch (RuntimeException error) {
            responseObserver.onError(error);
        }
    }

    @Override
    public void cancelarReserva(CancelarReservaRequest request, StreamObserver<CancelarReservaResponse> responseObserver) {
        try {
            Reserva reservation = repository.findById(request.getReservaId())
                    .orElseThrow(() -> Status.NOT_FOUND.withDescription("Reserva inexistente").asRuntimeException());
            if (request.getClienteId() > 0 && request.getClienteId() != reservation.getClienteId()) {
                throw Status.PERMISSION_DENIED.withDescription("La reserva no pertenece al cliente").asRuntimeException();
            }
            if (reservation.getEstado() == EstadoReserva.CANCELADA) {
                throw Status.FAILED_PRECONDITION.withDescription("La reserva ya está cancelada").asRuntimeException();
            }
            if (LocalDateTime.now().isAfter(reservation.getFechaInicio())) {
                throw Status.FAILED_PRECONDITION.withDescription("La reserva ya comenzó").asRuntimeException();
            }
            reservation.setEstado(EstadoReserva.CANCELADA);
            responseObserver.onNext(CancelarReservaResponse.newBuilder()
                    .setExito(true)
                    .setReserva(toProto(repository.save(reservation)))
                    .build());
            responseObserver.onCompleted();
        } catch (RuntimeException error) {
            responseObserver.onError(error);
        }
    }

    @Override
    public void consultarHistorialCliente(ConsultarHistorialClienteRequest request,
                                           StreamObserver<ConsultarHistorialClienteResponse> responseObserver) {
        try {
            estadoService.actualizarFinalizadas();
            if (request.getClienteId() <= 0) {
                throw Status.INVALID_ARGUMENT.withDescription("ID de cliente inválido").asRuntimeException();
            }
            List<HistorialResponse> history = new ArrayList<>();
            for (Reserva item : repository.findByClienteId(request.getClienteId())) {
                history.add(HistorialResponse.newBuilder()
                        .setReservaId(item.getId())
                        .setClienteId(item.getClienteId())
                        .setVehiculoId(item.getVehiculoId())
                        .setFechaInicio(item.getFechaInicio().toString())
                        .setFechaFin(item.getFechaFin().toString())
                        .setCantidadDias(Math.max(1, ChronoUnit.DAYS.between(item.getFechaInicio(), item.getFechaFin())))
                        .setImporteTotal(item.getImporteTotal())
                        .setEstado(item.getEstado().name())
                        .build());
            }
            responseObserver.onNext(ConsultarHistorialClienteResponse.newBuilder().addAllReservas(history).build());
            responseObserver.onCompleted();
        } catch (RuntimeException error) {
            responseObserver.onError(error);
        }
    }

    private LocalDateTime parse(String value) {
        if (value == null || value.isBlank()) {
            throw invalid("Las fechas son obligatorias");
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception error) {
            throw invalid("Formato de fecha inválido");
        }
    }

    private IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }

    private ReservaResponse toProto(Reserva item) {
        return ReservaResponse.newBuilder()
                .setId(item.getId())
                .setClienteId(item.getClienteId())
                .setVehiculoId(item.getVehiculoId())
                .setFechaInicio(item.getFechaInicio().toString())
                .setFechaFin(item.getFechaFin().toString())
                .setPrecioDiario(item.getPrecioDiario())
                .setImporteTotal(item.getImporteTotal())
                .setEstado(ar.unla.rentar.grpc.rental.v1.EstadoReserva.valueOf(item.getEstado().name()))
                .build();
    }
}
