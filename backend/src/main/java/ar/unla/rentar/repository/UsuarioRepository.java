package ar.unla.rentar.repository;

import ar.unla.rentar.model.Rol;
import ar.unla.rentar.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepository {

    public Optional<Usuario> findByEmail(String email) {

        if ("admin@rentar.com".equals(email)) {

            String passwordHash = new BCryptPasswordEncoder()
                    .encode("123456");

            Usuario usuario = new Usuario(
                    1L,
                    "admin@rentar.com",
                    passwordHash,
                    Rol.ADMINISTRADOR,
                    true
            );

            return Optional.of(usuario);
        }

        return Optional.empty();
    }
}