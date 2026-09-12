package ar.unla.rentar.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import ar.unla.rentar.dto.LoginRequestDTO;
import ar.unla.rentar.dto.LoginResponseDTO;
import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.repository.ClienteRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder) {

        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {

        Cliente cliente = clienteRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Credenciales inválidas"));

        if (!cliente.isActivo()) {
            throw new RuntimeException("El usuario está inactivo");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                cliente.getPassword())) {

            throw new RuntimeException("Credenciales inválidas");
        }

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("rentar")
                .subject(cliente.getEmail())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(2, ChronoUnit.HOURS))
                .claim("esAdmin", cliente.isEsAdmin())
                .build();

        JwsHeader jwsHeader =
                JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(
                        jwsHeader,
                        claims
                ))
                .getTokenValue();

        return new LoginResponseDTO(
                token,
                "Bearer",
                cliente.isEsAdmin()
        );
    }
}
