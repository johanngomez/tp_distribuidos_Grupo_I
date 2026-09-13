package ar.unla.rentar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaUpdateDTO {
    private Long id;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}