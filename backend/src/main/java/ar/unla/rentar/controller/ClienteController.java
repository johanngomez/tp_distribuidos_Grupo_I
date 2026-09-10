package ar.unla.rentar.controller;

import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")                                         //ruta base para todos los endpoints de clientes
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {            //inyección de dependencias por constructor
        this.clienteService = clienteService;
    }

    @GetMapping                                                          //GET: listar todos los clientes
    public List<Cliente> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/{id}")                                                 //GET: buscar un cliente por ID
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping                                                         //POST: crear un cliente nuevo (Alta)
    public ResponseEntity<?> crear(@RequestBody Cliente cliente) {
        try {
            Cliente nuevoCliente = clienteService.crear(cliente);
            return ResponseEntity.ok(nuevoCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")                                                //PUT: modificar un cliente que ya existe
    public ResponseEntity<?> modificar(@PathVariable Long id, @RequestBody Cliente cliente) {
        try {
            Cliente clienteModificado = clienteService.modificar(id, cliente);
            return ResponseEntity.ok(clienteModificado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")                                             //DELETE: baja lógica de un cliente
    public ResponseEntity<?> bajaLogica(@PathVariable Long id) {
        try {
            clienteService.bajaLogica(id);
            return ResponseEntity.ok("Cliente dado de 'baja'  con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
