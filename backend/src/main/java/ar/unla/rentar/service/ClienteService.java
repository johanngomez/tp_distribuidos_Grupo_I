package ar.unla.rentar.service;

import ar.unla.rentar.dto.ClienteCreateDto;
import ar.unla.rentar.dto.ClienteResponseDto;
import ar.unla.rentar.dto.ClienteUpdateDto;
import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service                                                                                        //indica que esta clase maneja la lógica de negocio
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {                                //inyección de dependencias por constructor
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteResponseDto> listarTodos() {                                             //lista todos los clientes
        return clienteRepository.findAll().stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<ClienteResponseDto> buscarPorId(Long id) {                                  //busca un cliente por ID
        return clienteRepository.findById(id)
                .map(this::mapearAResponseDto);
    }

    public ClienteResponseDto crear(ClienteCreateDto dto) {                                     //crea un cliente nuevo (valida si hay duplicados por documento o email)
        
        if (clienteRepository.findByDocumento(dto.getDocumento()).isPresent()) {                //validamos si ya existe el documento
            throw new RuntimeException("Ya existe un cliente con ese documento.");
        }
        
        if (clienteRepository.findByEmail(dto.getEmail()).isPresent()) {                        //validamos si ya existe el email
            throw new RuntimeException("Ya existe un cliente con ese email.");
        }

        Cliente cliente = new Cliente();                                                        //CreateDto a Cliente para guardar en la bd
        cliente.setDocumento(dto.getDocumento());
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setActivo(true);                                                        //esta activo de principio

        Cliente clienteGuardado = clienteRepository.save(cliente);
        return mapearAResponseDto(clienteGuardado);
    }

    public ClienteResponseDto modificar(Long id, ClienteUpdateDto dto) {                        //modificar un cliente que ya existe
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTelefono(dto.getTelefono());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());

        Cliente clienteActualizado = clienteRepository.save(cliente);
        return mapearAResponseDto(clienteActualizado);
    }

    public void bajaLogica(Long id) {                                                           //baja lógica (cambiar activo a false) 
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        
        cliente.setActivo(false);                                                       //se desactiva pero no se borra de la bd
        clienteRepository.save(cliente);
    }

    private ClienteResponseDto mapearAResponseDto(Cliente cliente) {                            //método privado (auxiliar)para transformar a Cliente en un DTO de resp.
        ClienteResponseDto dto = new ClienteResponseDto();
        dto.setId(cliente.getId());
        dto.setDocumento(cliente.getDocumento());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setFechaNacimiento(cliente.getFechaNacimiento());
        dto.setActivo(cliente.isActivo());
        return dto;
    }
}