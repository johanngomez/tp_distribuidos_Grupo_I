package ar.unla.rentar;


import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.model.EstadoReserva;
import ar.unla.rentar.model.Reserva;
import ar.unla.rentar.model.Vehiculo;
import ar.unla.rentar.model.TipoVehiculo;
import ar.unla.rentar.model.EstadoVehiculo;

import ar.unla.rentar.repository.ReservaRepository;
import ar.unla.rentar.repository.VehiculoRepository;
import ar.unla.rentar.repository.ClienteRepository;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest

class ReservaTest {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private ClienteRepository clienteRepository;


@Test
    void testCrearReservaConClienteYVehiculo() {

        reservaRepository.deleteAll();
        clienteRepository.deleteAll();
        vehiculoRepository.deleteAll();

        // 1. Instanciar Cliente
        Cliente cliente = new Cliente();
        cliente.setDocumento("40123458");
        cliente.setNombre("Carlos");
        cliente.setApellido("Gómez");
        cliente.setEmail("algo21@algo.com");

        // 2. Instanciar Vehículo
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente("AA123BB");
        vehiculo.setMarca("TOYOTA");
        vehiculo.setModelo("Corolla");
        vehiculo.setAnio(2020);
        vehiculo.setPrecioDiario(45000.0);
        vehiculo.setTipo(TipoVehiculo.COUPE);
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setActivo(true);

        // 3. Crear Reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setVehiculo(vehiculo);
        reserva.setImporteTotal(435000.0);
        reserva.setPrecioDiario(45000.0);
        reserva.setFechaInicio(LocalDateTime.now());
        reserva.setFechaFin(LocalDateTime.now().plusDays(3));
        reserva.setEstado(EstadoReserva.CONFIRMADA);


        clienteRepository.save(cliente);
        vehiculoRepository.save(vehiculo);
        reservaRepository.save(reserva);

        // 4. Verificaciones
        Assertions.assertEquals("40123458", reserva.getCliente().getDocumento());
        Assertions.assertEquals("AA123BB", reserva.getVehiculo().getPatente());
        Assertions.assertTrue(reserva.getFechaFin().isAfter(reserva.getFechaInicio()));
        Assertions.assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

        


}
