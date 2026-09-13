package ar.unla.rentar.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaCreateDTO {
    private Long vehiculoId;
    private Long clienteId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}