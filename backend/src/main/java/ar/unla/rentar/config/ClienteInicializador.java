package ar.unla.rentar.config;

import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.repository.ClienteRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ClienteInicializador {

    @Bean
    CommandLineRunner crearAdministradorDePrueba(
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            Cliente admin = clienteRepository.findByEmail("admin@rentar.com")
                    .orElseGet(Cliente::new);

            if (admin.getId() != null && admin.isEsAdmin()
                    && admin.getPassword() != null && !admin.getPassword().isBlank()) {
                return;
            }

            admin.setDocumento("10000001");
            admin.setNombre("Admin");
            admin.setApellido("Sistema");
            admin.setEmail("admin@rentar.com");
            admin.setTelefono("1100000001");
            admin.setFechaNacimiento(LocalDate.of(1980, 1, 1));
            admin.setActivo(true);
            admin.setEsAdmin(true);
            admin.setPassword(passwordEncoder.encode("123456"));
            clienteRepository.save(admin);
        };
    }
}
