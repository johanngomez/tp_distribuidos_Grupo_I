package ar.unla.rentar.grpc;

import ar.edu.unla.rentar.customer.grpc.ClienteEmailRequest;
import ar.edu.unla.rentar.customer.grpc.ClienteRequest;
import ar.edu.unla.rentar.customer.grpc.ClienteResponse;
import ar.edu.unla.rentar.customer.grpc.CrearClienteRequest;
import ar.edu.unla.rentar.customer.grpc.CustomerServiceGrpc;
import ar.edu.unla.rentar.customer.grpc.DarDeBajaClienteRequest;
import ar.edu.unla.rentar.customer.grpc.ListarClientesRequest;
import ar.edu.unla.rentar.customer.grpc.ListarClientesResponse;
import ar.edu.unla.rentar.customer.grpc.LoginRequest;
import ar.edu.unla.rentar.customer.grpc.LoginResponse;
import ar.edu.unla.rentar.customer.grpc.ModificarClienteRequest;
import ar.edu.unla.rentar.customer.grpc.OperacionClienteResponse;
import ar.edu.unla.rentar.customer.grpc.ValidacionResponse;
import ar.unla.rentar.dto.ClienteCreateDto;
import ar.unla.rentar.dto.ClienteResponseDto;
import ar.unla.rentar.dto.ClienteUpdateDto;
import ar.unla.rentar.service.AuthService;
import ar.unla.rentar.service.ClienteService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@GrpcService
public class CustomerGrpcServiceImpl extends CustomerServiceGrpc.CustomerServiceImplBase {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AuthService authService;

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            var loginRequest = new ar.unla.rentar.dto.LoginRequestDTO();
            loginRequest.setEmail(request.getEmail());
            loginRequest.setPassword(request.getPassword());
            var login = authService.login(loginRequest);
            responseObserver.onNext(LoginResponse.newBuilder()
                    .setToken(login.getToken())
                    .setTipo(login.getTipo())
                    .setEsAdmin(login.isEsAdmin())
                    .build());
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            error(responseObserver, Status.UNAUTHENTICATED, "Credenciales inválidas");
        }
    }

    @Override
    public void listarClientes(ListarClientesRequest request,
                               StreamObserver<ListarClientesResponse> responseObserver) {
        List<ClienteResponse> clientes = clienteService.listarTodos().stream()
                .map(this::toProto)
                .toList();
        responseObserver.onNext(ListarClientesResponse.newBuilder().addAllClientes(clientes).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getCliente(ClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
        clienteService.buscarPorId(request.getId()).ifPresentOrElse(
                cliente -> complete(responseObserver, toProto(cliente)),
                () -> error(responseObserver, Status.NOT_FOUND, "Cliente no encontrado")
        );
    }

    @Override
    public void obtenerClientePorEmail(ClienteEmailRequest request,
                                       StreamObserver<ClienteResponse> responseObserver) {
        try {
            complete(responseObserver, toProto(clienteService.buscarPorEmail(request.getEmail())));
        } catch (RuntimeException exception) {
            error(responseObserver, Status.NOT_FOUND, exception.getMessage());
        }
    }

    @Override
    public void crearCliente(CrearClienteRequest request,
                             StreamObserver<ClienteResponse> responseObserver) {


        try {
            ClienteCreateDto dto = new ClienteCreateDto();
            dto.setDocumento(request.getDocumento());
            dto.setNombre(request.getNombre());
            dto.setApellido(request.getApellido());
            dto.setEmail(request.getEmail());
            dto.setTelefono(request.getTelefono());
            dto.setFechaNacimiento(parseDate(request.getFechaNacimiento()));
            dto.setPassword(request.getPassword());
            complete(responseObserver, toProto(clienteService.crear(dto)));
        } catch (RuntimeException exception) {
            error(responseObserver, Status.INVALID_ARGUMENT, exception.getMessage());
        }
    }

    @Override
    public void modificarCliente(ModificarClienteRequest request,
                                  StreamObserver<ClienteResponse> responseObserver) {
        try {
            ClienteUpdateDto dto = new ClienteUpdateDto();
            dto.setNombre(request.getNombre());
            dto.setApellido(request.getApellido());
            dto.setTelefono(request.getTelefono());
            dto.setFechaNacimiento(parseDate(request.getFechaNacimiento()));
            complete(responseObserver, toProto(clienteService.modificar(request.getId(), dto)));
        } catch (RuntimeException exception) {
            error(responseObserver, Status.NOT_FOUND, exception.getMessage());
        }
    }

    @Override
    public void darDeBajaCliente(DarDeBajaClienteRequest request,
                                 StreamObserver<OperacionClienteResponse> responseObserver) {
        try {
            clienteService.bajaLogica(request.getId());
            responseObserver.onNext(OperacionClienteResponse.newBuilder()
                    .setExito(true)
                    .setMensaje("Cliente dado de baja lógicamente")
                    .build());
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            error(responseObserver, Status.NOT_FOUND, exception.getMessage());
        }
    }

    @Override
    public void validarCliente(ClienteRequest request,
                               StreamObserver<ValidacionResponse> responseObserver) {
        boolean existe = clienteService.buscarPorId(request.getId()).isPresent();
        boolean activo = existe && clienteService.buscarPorId(request.getId()).orElseThrow().isActivo();
        responseObserver.onNext(ValidacionResponse.newBuilder()
                .setExiste(existe)
                .setActivo(activo)
                .setPuedeAlquilar(activo)
                .build());
        responseObserver.onCompleted();
    }

    private ClienteResponse toProto(ClienteResponseDto cliente) {
        ClienteResponse.Builder builder = ClienteResponse.newBuilder()
                .setId(cliente.getId())
                .setDocumento(value(cliente.getDocumento()))
                .setNombre(value(cliente.getNombre()))
                .setApellido(value(cliente.getApellido()))
                .setEmail(value(cliente.getEmail()))
                .setTelefono(value(cliente.getTelefono()))
                .setActivo(cliente.isActivo())
                .setEsAdmin(cliente.isEsAdmin());
        if (cliente.getFechaNacimiento() != null) {
            builder.setFechaNacimiento(cliente.getFechaNacimiento().toString());
        }
        return builder.build();
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private <T> void complete(StreamObserver<T> observer, T response) {
        observer.onNext(response);
        observer.onCompleted();
    }

    private void error(StreamObserver<?> observer, Status status, String message) {
        observer.onError(status.withDescription(message == null ? "Error de cliente" : message)
                .asRuntimeException());
    }
}
