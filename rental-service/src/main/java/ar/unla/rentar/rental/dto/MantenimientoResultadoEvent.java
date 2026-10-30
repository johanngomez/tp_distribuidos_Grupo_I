package ar.unla.rentar.rental.dto;

import ar.unla.rentar.rental.model.ResultadoMantenimiento;

public record MantenimientoResultadoEvent (
    Long reservaId,
    Long vehiculoId,
    ResultadoMantenimiento resultado // "REALIZADO" o "NO_REALIZADO"
) {}
