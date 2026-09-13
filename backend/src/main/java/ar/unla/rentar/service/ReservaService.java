package ar.unla.rentar.service;

import ar.unla.rentar.dto.ReservaResponseDTO;
import ar.unla.rentar.dto.ReservaUpdateDTO;
import ar.unla.rentar.model.*;
import ar.unla.rentar.repository.ClienteRepository;
import ar.unla.rentar.repository.ReservaRepository;
import ar.unla.rentar.repository.VehiculoRepository;
import ar.unla.rentar.dto.ReservaCreateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    public List<Reserva> obtenerTodas() {
        return reservaRepository.findAll();
    }

    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
    }

    @Transactional
    public ReservaResponseDTO crearReserva(ReservaCreateDTO dto)
    {
        if (dto.getFechaFin().isBefore(dto.getFechaInicio()) || dto.getFechaInicio().isEqual(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin.");
        }
        if (dto.getFechaInicio().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden realizar reservas para fechas pasadas.");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getClienteId()));
        
        if (!Boolean.TRUE.equals(cliente.isActivo())) {
            throw new IllegalStateException("El cliente ingresado se encuentra inactivo.");
        }

        // 3. Obtener y validar Vehículo
        Vehiculo vehiculo = vehiculoRepository.findById(dto.getVehiculoId())
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con ID: " + dto.getVehiculoId()));

        if (!Boolean.TRUE.equals(vehiculo.getActivo())) {
            throw new IllegalStateException("El vehículo inactivo no puede utilizarse para nuevos alquileres.");
        }

        // 4. Validar solapamiento de fechas
        List<EstadoReserva> estadosActivos = List.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO); //Buscar solamente reservas que esten activas
        boolean existeReserva = reservaRepository.existsByVehiculoIdAndEstadoInAndFechaInicioLessThanAndFechaFinGreaterThan(dto.getVehiculoId(), estadosActivos, dto.getFechaFin(), dto.getFechaInicio());
        
        if (existeReserva) {
            throw new IllegalStateException("El vehículo ya posee una reserva en el rango de fechas seleccionadas.");
        }

        // 5. Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setVehiculo(vehiculo);
        reserva.setFechaInicio(dto.getFechaInicio());
        reserva.setFechaFin(dto.getFechaFin());
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        // 5. Calcular importe total según la duración de la reserva y el precio diario del vehículo
        Double importeTotal = calcularImporteTotal(dto.getFechaInicio(), dto.getFechaFin(), vehiculo.getPrecioDiario());
        reserva.setImporteTotal(importeTotal);

        // 6. Actualizar el estado del vehículo a RESERVADO
        vehiculo.setEstado(EstadoVehiculo.RESERVADO);
        vehiculoRepository.save(vehiculo);

        // 6. Guardar en BD y retornar DTO
        Reserva reservaGuardada = reservaRepository.save(reserva);
        return mapearADTO(reservaGuardada);
    }

    private Double calcularImporteTotal(LocalDateTime inicio, LocalDateTime fin, Double precioDiario) {
        long horas = Duration.between(inicio, fin).toHours(); //calcular la cantidad de horas entre las fechas de inicio y fin
        long dias = (long) Math.ceil((double) horas / 24.0); //conseguir los decimales de ese valor
        if (dias < 1) dias = 1; //si fueron menos de 24 horas, se cobra de todas maneras un dia completo
        return dias * precioDiario; //multiplicar la cantidad de dias por el precio diario del vehiculo
    }

    private ReservaResponseDTO mapearADTO(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getCliente(),
                reserva.getVehiculo(),
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                reserva.getPrecioDiario(),
                reserva.getImporteTotal(),
                reserva.getEstado()
        );
    }

        // --- ACTUALIZAR RESERVA ---
    @Transactional
    public ReservaResponseDTO actualizarReserva(Long reservaId, ReservaUpdateDTO dto) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + reservaId));

                LocalDateTime nuevaFechaInicio = dto.getFechaInicio() != null ? dto.getFechaInicio() : reserva.getFechaInicio();
                LocalDateTime nuevaFechaFin = dto.getFechaFin() != null ? dto.getFechaFin() : reserva.getFechaFin();

        if (nuevaFechaFin.isBefore(nuevaFechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        reserva.setFechaInicio(nuevaFechaInicio);
            reserva.setFechaFin(nuevaFechaFin);
            reserva.setImporteTotal(calcularImporteTotal(nuevaFechaInicio, nuevaFechaFin, reserva.getPrecioDiario()));

            Reserva reservaActualizada = reservaRepository.save(reserva);
        return mapearADTO(reservaActualizada);
    
    }

    @Transactional
    public ReservaResponseDTO cancelarReserva(Long reservaId) {

        // 1. Buscar la reserva
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + reservaId));

            // 2. Validar que no haya comenzado
            if (LocalDateTime.now().isAfter(reserva.getFechaInicio())) {
                throw new IllegalStateException("No se puede cancelar una reserva que ya comenzó o finalizó.");
            }

            // 3. Modificar estado a CANCELADA
            reserva.setEstado(EstadoReserva.CANCELADA);

            // 4. Guardar cambios y retornar
            Reserva reservaActualizada = reservaRepository.save(reserva);
            return mapearADTO(reservaActualizada);
    }
}

