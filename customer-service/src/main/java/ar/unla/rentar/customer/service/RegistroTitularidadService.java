package ar.unla.rentar.customer.service;

import org.springframework.stereotype.Service;

@Service
public class RegistroTitularidadService {

    //lógica para validar la titularidad del cliente
    public String validarTitularidad(Long clienteId) {
        //simulamos que si el clienteId es válido (mayor a 0), la titularidad es correcta.
        if (clienteId != null && clienteId > 0) {
            return "VALIDADO";
        }
        return "RECHAZADO";
    }
}