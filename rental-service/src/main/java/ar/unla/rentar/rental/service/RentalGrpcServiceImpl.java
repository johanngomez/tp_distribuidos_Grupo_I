package ar.unla.rentar.rental.service;

import ar.edu.unla.rentar.customer.grpc.*;
import ar.unla.rentar.grpc.rental.v1.*;
import ar.unla.rentar.grpc.vehicle.v1.*;
import ar.unla.rentar.rental.dto.ReservaConfirmadaEvent;
import ar.unla.rentar.rental.model.*;
import ar.unla.rentar.rental.repository.ReservaRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PreDestroy;
import net.devh.boot.grpc.server.service.GrpcService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@GrpcService
public class RentalGrpcServiceImpl extends RentalServiceGrpc.RentalServiceImplBase {
  private final ReservaRepository repository;
  private final ManagedChannel customerChannel;
  private final ManagedChannel vehicleChannel;
  private final CustomerServiceGrpc.CustomerServiceBlockingStub customers;
  private final VehicleServiceGrpc.VehicleServiceBlockingStub vehicles;
  @Autowired
  private RentalEventPublisher eventPublisher;

  public RentalGrpcServiceImpl(ReservaRepository repository,
      @Value("${CUSTOMER_GRPC_TARGET:localhost:50052}") String customerTarget,
      @Value("${VEHICLE_GRPC_TARGET:localhost:50051}") String vehicleTarget) {
    this.repository = repository;
    this.customerChannel = ManagedChannelBuilder.forTarget(customerTarget).usePlaintext().build();
    this.vehicleChannel = ManagedChannelBuilder.forTarget(vehicleTarget).usePlaintext().build();
    this.customers = CustomerServiceGrpc.newBlockingStub(customerChannel);
    this.vehicles = VehicleServiceGrpc.newBlockingStub(vehicleChannel);
  }

  @Override public void crearReserva(CrearReservaRequest r, StreamObserver<ReservaResponse> out) {
    try {
      if (r.getClienteId() <= 0 || r.getVehiculoId() <= 0) throw invalid("Los IDs deben ser positivos");
      LocalDateTime inicio = parse(r.getFechaInicio()), fin = parse(r.getFechaFin());
      if (!fin.isAfter(inicio)) throw invalid("La fecha de fin debe ser posterior a la de inicio");
      ValidacionResponse cliente = customers.validarCliente(ClienteRequest.newBuilder().setIdCliente(r.getClienteId()).build());
      if (!cliente.getExiste() || !cliente.getActivo() || !cliente.getPuedeAlquilar()) throw Status.FAILED_PRECONDITION.withDescription("Cliente inexistente o inactivo").asRuntimeException();
      Vehicle v = vehicles.getVehicle(GetVehicleRequest.newBuilder().setId(r.getVehiculoId()).build());
      if (!v.getActivo() || v.getEstado() != VehicleStatus.DISPONIBLE) throw Status.FAILED_PRECONDITION.withDescription("Vehículo no disponible").asRuntimeException();
      if (repository.existsByVehiculoIdAndEstadoInAndFechaInicioLessThanAndFechaFinGreaterThan(r.getVehiculoId(), List.of(ar.unla.rentar.rental.model.EstadoReserva.CONFIRMADA, ar.unla.rentar.rental.model.EstadoReserva.EN_CURSO), fin, inicio)) throw Status.ALREADY_EXISTS.withDescription("El vehículo ya está reservado").asRuntimeException();
      Reserva entity = new Reserva(); entity.setClienteId(r.getClienteId()); entity.setVehiculoId(r.getVehiculoId()); entity.setFechaInicio(inicio); entity.setFechaFin(fin); entity.setPrecioDiario(v.getPrecioDiario()); entity.setImporteTotal(v.getPrecioDiario() * Math.max(1, (long)Math.ceil(Duration.between(inicio, fin).toHours() / 24.0))); entity.setEstado(ar.unla.rentar.rental.model.EstadoReserva.CONFIRMADA);

      entity = repository.save(entity);

      ReservaConfirmadaEvent evento = new ReservaConfirmadaEvent(
            entity.getId(),
            entity.getVehiculoId(),
            entity.getFechaInicio(),
            entity.getFechaFin()
          );

          eventPublisher.publicarReservaConfirmada(evento);

      out.onNext(toProto(entity));
      out.onCompleted();
    } catch (RuntimeException e) {
       e.printStackTrace();
        out.onError(io.grpc.Status.INTERNAL
            .withDescription("Error interno: " + e.getMessage())
            .asRuntimeException());
    }
  }

  @Override public void consultarReservas(ConsultarReservasRequest r, StreamObserver<ConsultarReservasResponse> out) {
    try {
      Long customerId = r.getClienteId() > 0 ? r.getClienteId() : null;
      if (customerId == null && !r.getEmailCliente().isBlank()) customerId = customers.obtenerClientePorEmail(ClienteEmailRequest.newBuilder().setEmail(r.getEmailCliente()).build()).getIdCliente();
      LocalDateTime from = r.getFechaInicio().isBlank() ? null : parse(r.getFechaInicio());
      LocalDateTime to = r.getFechaFin().isBlank() ? null : parse(r.getFechaFin());
      Long cid = customerId; final Long vid = r.getVehiculoId() > 0 ? r.getVehiculoId() : null;
      List<ReservaResponse> result = repository.findAll().stream().filter(x -> cid == null || x.getClienteId().equals(cid)).filter(x -> vid == null || x.getVehiculoId().equals(vid)).filter(x -> r.getEstadoValue() == 0 || x.getEstado().ordinal() == r.getEstadoValue()).filter(x -> from == null || !x.getFechaInicio().isBefore(from)).filter(x -> to == null || !x.getFechaFin().isAfter(to)).map(this::toProto).collect(Collectors.toList());
      out.onNext(ConsultarReservasResponse.newBuilder().addAllReservas(result).build()); out.onCompleted();
    } catch (RuntimeException e) { out.onError(e); }
  }

  @Override public void cancelarReserva(CancelarReservaRequest r, StreamObserver<CancelarReservaResponse> out) {
    try {
      Reserva x = repository.findById(r.getReservaId()).orElseThrow(() -> Status.NOT_FOUND.withDescription("Reserva inexistente").asRuntimeException());
      if (!r.getClienteEmail().isBlank() && customers.obtenerClientePorEmail(ClienteEmailRequest.newBuilder().setEmail(r.getClienteEmail()).build()).getIdCliente() != x.getClienteId()) throw Status.PERMISSION_DENIED.withDescription("La reserva no pertenece al cliente").asRuntimeException();
      if (x.getEstado() == ar.unla.rentar.rental.model.EstadoReserva.CANCELADA) throw Status.FAILED_PRECONDITION.withDescription("La reserva ya está cancelada").asRuntimeException();
      if (LocalDateTime.now().isAfter(x.getFechaInicio())) throw Status.FAILED_PRECONDITION.withDescription("La reserva ya comenzó").asRuntimeException();
      x.setEstado(ar.unla.rentar.rental.model.EstadoReserva.CANCELADA); out.onNext(CancelarReservaResponse.newBuilder().setExito(true).setReserva(toProto(repository.save(x))).build()); out.onCompleted();
    } catch (RuntimeException e) { out.onError(e); }
  }

  @Override public void consultarHistorialCliente(ConsultarHistorialClienteRequest r, StreamObserver<ConsultarHistorialClienteResponse> out) {
    try { if (r.getClienteId() <= 0) throw invalid("ID de cliente inválido");
      List<HistorialResponse> list = new ArrayList<>(); for (Reserva x : repository.findByClienteId(r.getClienteId())) { Vehicle v = vehicles.getVehicle(GetVehicleRequest.newBuilder().setId(x.getVehiculoId()).build()); list.add(HistorialResponse.newBuilder().setVehiculo(v.getMarca()+" "+v.getModelo()).setPatente(v.getPatente()).setFechaInicio(x.getFechaInicio().toString()).setFechaFin(x.getFechaFin().toString()).setCantidadDias(Math.max(1, ChronoUnit.DAYS.between(x.getFechaInicio(),x.getFechaFin()))).setImporteTotal(x.getImporteTotal()).setEstado(x.getEstado().name()).build()); }
      out.onNext(ConsultarHistorialClienteResponse.newBuilder().addAllReservas(list).build()); out.onCompleted();
    } catch (RuntimeException e) { out.onError(e); }
  }

  private LocalDateTime parse(String s) { if (s == null || s.isBlank()) throw invalid("Las fechas son obligatorias"); try { return LocalDateTime.parse(s); } catch (Exception e) { throw invalid("Formato de fecha inválido"); } }
  private IllegalArgumentException invalid(String m) { return new IllegalArgumentException(m); }
  private ReservaResponse toProto(Reserva x) { return ReservaResponse.newBuilder().setId(x.getId()).setClienteId(x.getClienteId()).setVehiculoId(x.getVehiculoId()).setFechaInicio(x.getFechaInicio().toString()).setFechaFin(x.getFechaFin().toString()).setPrecioDiario(x.getPrecioDiario()).setImporteTotal(x.getImporteTotal()).setEstado(ar.unla.rentar.grpc.rental.v1.EstadoReserva.valueOf(x.getEstado().name())).build(); }
  @PreDestroy public void close() { customerChannel.shutdown(); vehicleChannel.shutdown(); }
}
