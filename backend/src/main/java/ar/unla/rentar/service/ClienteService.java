package ar.unla.rentar.service;

import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service                                                            //indica que esta clase maneja la lógica de negocio
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {    //inyección de dependencias por constructor
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarTodos() {                             //lista todos los clientes
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {                  //busca un cliente por ID
        return clienteRepository.findById(id);
    }

    public Cliente crear(Cliente cliente) {                         //crea un cliente nuevo (valida si hay duplicados por documento o email)
       
        if (clienteRepository.findByDocumento(cliente.getDocumento()).isPresent()) {                 //validamos si ya existe el documento
            throw new RuntimeException("Ya existe un cliente con ese documento.");
        }
        
        if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {                        //validamos si ya existe el email
            throw new RuntimeException("Ya existe un cliente con ese email.");
        }

        cliente.setActivo(true);                                                            //esta activo de principio
        return clienteRepository.save(cliente);
    }

    public Cliente modificar(Long id, Cliente clienteActualizado) {                                 //modificar un cliente que ya existe
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setApellido(clienteActualizado.getApellido());
        cliente.setTelefono(clienteActualizado.getTelefono());
        cliente.setFechaNacimiento(clienteActualizado.getFechaNacimiento());

        return clienteRepository.save(cliente);
    }

    public void bajaLogica(Long id) {                                                               //baja lógica (cambiar activo a false) 
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        
        cliente.setActivo(false);                                                           //se desactiva pero no se borra de la bd
        clienteRepository.save(cliente);
    }
}