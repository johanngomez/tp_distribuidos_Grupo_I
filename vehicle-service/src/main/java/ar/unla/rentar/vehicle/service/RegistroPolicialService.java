package ar.unla.rentar.vehicle.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RegistroPolicialService {

    // Lista simulada de patentes con pedido de captura / robadas para las pruebas
    private static final List<String> PATENTES_ROBADAS = List.of("ROBADO01", "AAA123", "XYZ999");

    public String verificarVehiculo(String patente) {
        if (patente == null) {
            return "VALIDADO";
        }
        
        // Verificamos si la patente está en la lista de robados (ignorando mayúsculas/minúsculas)
        boolean esRobado = PATENTES_ROBADAS.stream()
                .anyMatch(p -> p.equalsIgnoreCase(patente.trim()));

        if (esRobado) {
            return "ROBADO";
        }
        
        return "VALIDADO";
    }
}
