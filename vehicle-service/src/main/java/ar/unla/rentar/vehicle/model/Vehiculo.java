package ar.unla.rentar.vehicle.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String patente;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(name = "anio")
    private Integer anio;

    private String color;

    @Enumerated(EnumType.STRING)
    private TipoVehiculo tipo;

    @Column(name = "precio_diario", nullable = false)
    private Double precioDiario;

    @Enumerated(EnumType.STRING)
    private EstadoVehiculo estado;

    private Boolean activo;

    public Vehiculo() {}

    public Vehiculo(Long id, String patente, String marca, String modelo, Integer anio, String color, TipoVehiculo tipo, Double precioDiario, EstadoVehiculo estado, Boolean activo) {
        this.id = id;
        this.patente = patente;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.tipo = tipo;
        this.precioDiario = precioDiario;
        this.estado = estado;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public TipoVehiculo getTipo() { return tipo; }
    public void setTipo(TipoVehiculo tipo) { this.tipo = tipo; }

    public Double getPrecioDiario() { return precioDiario; }
    public void setPrecioDiario(Double precioDiario) { this.precioDiario = precioDiario; }

    public EstadoVehiculo getEstado() { return estado; }
    public void setEstado(EstadoVehiculo estado) { this.estado = estado; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
