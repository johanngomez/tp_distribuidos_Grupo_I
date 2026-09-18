package ar.unla.rentar.dto;

import java.time.LocalDateTime;
import lombok.*;

import ar.unla.rentar.model.EstadoReserva;
import ar.unla.rentar.model.TipoVehiculo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaFiltroDTO {
    private Long clienteId;
    private Long vehiculoId;
    private TipoVehiculo tipoVehiculo;
    private EstadoReserva estado;
    private LocalDateTime fechaInicioDesde;
    private LocalDateTime fechaInicioHasta;
}
