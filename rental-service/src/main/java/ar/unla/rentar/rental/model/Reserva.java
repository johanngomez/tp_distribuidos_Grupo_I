package ar.unla.rentar.rental.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="reserva")
public class Reserva {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long clienteId;
 @Column(nullable=false) private Long vehiculoId;
 @Column(nullable=false) private LocalDateTime fechaInicio;
 @Column(nullable=false) private LocalDateTime fechaFin;
 @Column(nullable=false) private Double precioDiario;
 @Column(nullable=false) private Double importeTotal;
 @Enumerated(EnumType.STRING)
 @Column private ResultadoMantenimiento resultadoMantenimiento;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private EstadoReserva estado;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public Long getClienteId(){return clienteId;} public void setClienteId(Long v){clienteId=v;}
 public Long getVehiculoId(){return vehiculoId;} public void setVehiculoId(Long v){vehiculoId=v;}
 public LocalDateTime getFechaInicio(){return fechaInicio;} public void setFechaInicio(LocalDateTime v){fechaInicio=v;}
 public LocalDateTime getFechaFin(){return fechaFin;} public void setFechaFin(LocalDateTime v){fechaFin=v;}
 public Double getPrecioDiario(){return precioDiario;} public void setPrecioDiario(Double v){precioDiario=v;}
 public Double getImporteTotal(){return importeTotal;} public void setImporteTotal(Double v){importeTotal=v;}
 public EstadoReserva getEstado(){return estado;} public void setEstado(EstadoReserva v){estado=v;}
 public ResultadoMantenimiento getResultadoManteniento() {return resultadoMantenimiento;} public void setResultadoMantenimiento(ResultadoMantenimiento v){resultadoMantenimiento=v;}
}
