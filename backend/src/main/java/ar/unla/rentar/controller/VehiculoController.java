package ar.unla.rentar.controller;

import ar.unla.rentar.dto.VehiculoCreateDTO;
import ar.unla.rentar.dto.VehiculoResponseDTO;
import ar.unla.rentar.dto.VehiculoUpdateDTO;
import ar.unla.rentar.service.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController // anotación que indica que esta clase es un controlador de Spring MVC y que sus métodos manejarán solicitudes HTTP y devolverán respuestas HTTP.
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // Get para listar todos los vehículos
    @GetMapping
    @Operation(summary = "Listar vehículos", description = "Obtiene todos los vehículos registrados.")
    @ApiResponse(responseCode = "200", description = "Vehículos obtenidos correctamente")
    public ResponseEntity<List<VehiculoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    // Get para buscar un vehículo por su ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar vehículo", description = "Obtiene un vehículo por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehiculoResponseDTO> buscarPorId(@PathVariable Long id) {
        return vehiculoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Post para crear un nuevo vehículo
    @PostMapping
    @Operation(summary = "Crear vehículo", description = "Registra un nuevo vehículo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehículo creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno al guardar el vehículo")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody VehiculoCreateDTO dto) {
        try {
            VehiculoResponseDTO nuevoVehiculo = vehiculoService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoVehiculo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Error al guardar el vehículo.");
        }
    }

    // Put para modificar un vehículo existente
    @PutMapping("/{id}")
    @Operation(summary = "Modificar vehículo", description = "Actualiza los datos de un vehículo existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<?> modificar(
            @PathVariable Long id,
            @Valid @RequestBody VehiculoUpdateDTO dto) {

        try {
            VehiculoResponseDTO vehiculoModificado = vehiculoService.modificar(id, dto);
            return ResponseEntity.ok(vehiculoModificado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete para eliminar un vehículo (baja lógica)
    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja vehículo", description = "Realiza la baja lógica de un vehículo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehículo dado de baja correctamente"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            vehiculoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
