package ar.unla.rentar.repository;

import ar.unla.rentar.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository                                                                  
public interface ClienteRepository extends JpaRepository<Cliente, Long> {       //indica que esta interfaz maneja la persistencia de datos con Spring

    Optional<Cliente> findByDocumento(String documento);                        //busca automáticamente un cliente por su documento

    Optional<Cliente> findByEmail(String email);                                //busca automáticamente un cliente por su email
}