package ar.unla.rentar.rental.service;

import org.springframework.stereotype.Service;

import ar.unla.rentar.rental.model.ResultadoMantenimiento;

@Service
public class MantenimientoService {

    public ResultadoMantenimiento simularMantenimiento(Long vehiculoId) {

        boolean exito = Math.random() > 0.2; //el hecho de que el mantenimiento sea un exito es al azar

        if (exito) {
            return ResultadoMantenimiento.REALIZADO;
        } else {
            return ResultadoMantenimiento.NO_REALIZADO;
        }
    }
}
