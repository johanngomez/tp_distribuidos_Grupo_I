package ar.unla.rentar.dto;

import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.model.EstadoReserva;
import ar.unla.rentar.model.Vehiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {
    private Long id;
    private Cliente cliente;
    private Vehiculo vehiculo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Double precioDiario;
    private Double importeTotal;
    private EstadoReserva estado;
}