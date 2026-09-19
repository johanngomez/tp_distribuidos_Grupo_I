package ar.unla.rentar.controller;

import ar.unla.rentar.model.Vehiculo;
import ar.unla.rentar.model.TipoVehiculo;
import ar.unla.rentar.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class VehiculoGraphQLController {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @QueryMapping
    public List<Vehiculo> consultarDisponibilidad(
            @Argument String fechaInicio,
            @Argument String fechaFin,
            @Argument String tipo,
            @Argument String marca,
            @Argument String modelo,
            @Argument Float precioMin,
            @Argument Float precioMax) {
        
        //parsear (convertir) las fechas. Pide "Fecha y hora", usamos el formato ISO 8601 (ej: 2026-10-01T10:00:00)
        LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
        LocalDateTime fin = LocalDateTime.parse(fechaFin);

        //convertir el String a Enum TipoVehiculo (solo si enviaron el filtro)
        TipoVehiculo tipoEnum = null;
        if (tipo != null && !tipo.isEmpty()) {
            tipoEnum = TipoVehiculo.valueOf(tipo.toUpperCase());
        }

        //adaptar los Float de GraphQL al Double que usa la clase Vehiculo
        Double pMin = precioMin != null ? precioMin.doubleValue() : null;
        Double pMax = precioMax != null ? precioMax.doubleValue() : null;

        //ejecutar la búsqueda en base de datos
        return vehiculoRepository.findDisponibles(inicio, fin, tipoEnum, marca, modelo, pMin, pMax);
    }
}