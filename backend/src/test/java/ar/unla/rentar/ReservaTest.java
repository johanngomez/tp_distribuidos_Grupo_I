package ar.unla.rentar;


import ar.unla.rentar.model.EstadoReserva;
import ar.unla.rentar.model.Reserva;
import ar.unla.rentar.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReservaTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    void testCrearReservaSinUsuario() {

        Reserva reserva = new Reserva();

        reserva.setEstado(EstadoReserva.CONFIRMADA);

        



    }

}
