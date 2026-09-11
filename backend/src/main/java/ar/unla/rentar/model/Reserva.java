package ar.unla.rentar.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Data                    
@NoArgsConstructor       
@AllArgsConstructor    
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    //private Vehiculo vehiculo;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private Double precioDiario;

    @Column(nullable = false)
    private Double importeTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;
}