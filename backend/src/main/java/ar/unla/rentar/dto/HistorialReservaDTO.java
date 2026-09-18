package ar.unla.rentar.dto;

public record HistorialReservaDTO(          //record nos evita escribir los getters y setters a mano
    String vehiculo,
    String patente,
    String fechaInicio,
    String fechaFin,
    Integer cantidadDias,
    Double importeTotal,
    String estado
) {}
