package ar.unla.rentar.service;

import ar.edu.unla.rentar.customer.grpc.*; 
import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.repository.ClienteRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GrpcService
public class CustomerGrpcServiceImpl extends CustomerServiceGrpc.CustomerServiceImplBase {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public void validarCliente(ClienteRequest request, StreamObserver<ValidacionResponse> responseObserver) {
        //tomamos el ID que llega desde el API Gateway. 
        //lo casteamos a (long) porque en la base de datos lo definimos como Long.
        Long idBuscado = (long) request.getIdCliente();
        
        //busca el cliente real en MySQL
        Optional<Cliente> clienteOpt = clienteRepository.findById(idBuscado);
        
        ValidacionResponse response;
        
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            //construimos la respuesta con los datos verdaderos de la base
            response = ValidacionResponse.newBuilder()
                    .setExiste(true)
                    .setActivo(cliente.isActivo())
                    .setPuedeAlquilar(cliente.isActivo())
                    .build();
        } else {
            //si el ID no existe en la base, devolvemos todo en false
            response = ValidacionResponse.newBuilder()
                    .setExiste(false)
                    .setActivo(false)
                    .setPuedeAlquilar(false)
                    .build();
        }

        //enviamos la respuesta de vuelta por la red y cerramos el canal
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}