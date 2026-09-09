package ar.unla.rentar.controller;

import ar.unla.rentar.dto.LoginRequestDTO;
import ar.unla.rentar.dto.LoginResponseDTO;
import ar.unla.rentar.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    // Endpoint para manejar el inicio de sesión
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO request) {
        // Llamar al servicio de autenticación para procesar el inicio de sesión
        LoginResponseDTO response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}