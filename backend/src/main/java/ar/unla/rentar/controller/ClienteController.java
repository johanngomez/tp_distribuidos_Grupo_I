package ar.unla.rentar.controller;

import ar.unla.rentar.dto.ClienteCreateDto;
import ar.unla.rentar.dto.ClienteResponseDto;
import ar.unla.rentar.dto.ClienteUpdateDto;
import ar.unla.rentar.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.net.URI;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")                                                            //ruta base para todos los endpoints de clientes
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {                               //inyección de dependencias por constructor
        this.clienteService = clienteService;
    }

    @GetMapping                                                                             //endpoint para listar todos los clientes
    @Operation(summary = "Listar clientes", description = "Obtiene todos los clientes registrados.")
    @ApiResponse(responseCode = "200", description = "Clientes obtenidos correctamente")
    public List<ClienteResponseDto> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/me")                                                                      //endpoint para obtener los datos del cliente logueado
    public ResponseEntity<ClienteResponseDto> obtenerPropioPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return ResponseEntity.ok(clienteService.buscarPorEmail(email));
    }

    @GetMapping("/{id}")                                                                    //endpoint para buscar un cliente por su ID
    @Operation(summary = "Buscar cliente", description = "Obtiene un cliente por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDto> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping                                                                            //endpoint para crear un cliente nuevo
    @Operation(summary = "Crear cliente", description = "Registra un nuevo cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o cliente duplicado")
    })
    public ResponseEntity<?> crear(@RequestBody ClienteCreateDto clienteDto) {
        try {
            ClienteResponseDto nuevoCliente = clienteService.crear(clienteDto);
            return ResponseEntity.created(URI.create("/api/clientes/" + nuevoCliente.getId())).body(nuevoCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")                                                                    //endpoint para modificar un cliente existente
    @Operation(summary = "Modificar cliente", description = "Actualiza los datos de un cliente existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<?> modificar(@PathVariable Long id, @RequestBody ClienteUpdateDto clienteDto) {
        try {
            ClienteResponseDto clienteModificado = clienteService.modificar(id, clienteDto);
            return ResponseEntity.ok(clienteModificado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")                                                                 //endpoint para realizar la baja lógica de un cliente
    @Operation(summary = "Dar de baja cliente", description = "Realiza la baja lógica de un cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente dado de baja correctamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> bajaLogica(@PathVariable Long id) {
        try {
            clienteService.bajaLogica(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
