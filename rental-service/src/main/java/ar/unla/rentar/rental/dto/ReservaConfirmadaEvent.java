package ar.unla.rentar.rental.dto;

import java.time.LocalDateTime;

public record ReservaConfirmadaEvent(
    Long reservaId,
    Long vehiculoId,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin
) {}
    

