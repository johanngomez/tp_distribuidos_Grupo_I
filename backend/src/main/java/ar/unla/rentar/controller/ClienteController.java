package ar.unla.rentar.controller;

import ar.unla.rentar.dto.ClienteCreateDto;
import ar.unla.rentar.dto.ClienteResponseDto;
import ar.unla.rentar.dto.ClienteUpdateDto;
import ar.unla.rentar.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")                                                            //ruta base para todos los endpoints de clientes
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {                               //inyección de dependencias por constructor
        this.clienteService = clienteService;
    }

    @GetMapping                                                                             //endpoint para listar todos los clientes
    public List<ClienteResponseDto> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/{id}")                                                                    //endpoint para buscar un cliente por su ID
    public ResponseEntity<ClienteResponseDto> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping                                                                            //endpoint para crear un cliente nuevo
    public ResponseEntity<?> crear(@RequestBody ClienteCreateDto clienteDto) {
        try {
            ClienteResponseDto nuevoCliente = clienteService.crear(clienteDto);
            return ResponseEntity.ok(nuevoCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")                                                                    //endpoint para modificar un cliente existente
    public ResponseEntity<?> modificar(@PathVariable Long id, @RequestBody ClienteUpdateDto clienteDto) {
        try {
            ClienteResponseDto clienteModificado = clienteService.modificar(id, clienteDto);
            return ResponseEntity.ok(clienteModificado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")                                                                 //endpoint para realizar la baja lógica de un cliente
    public ResponseEntity<?> bajaLogica(@PathVariable Long id) {
        try {
            clienteService.bajaLogica(id);
            return ResponseEntity.ok("Cliente dado de baja lógicamente con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}