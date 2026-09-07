package ar.unla.rentar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;


@Entity /// esta clase representa una entidad de base de datos
@Table( /// como queremos llamar a la tabla
    name = "vehiculo",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehiculo_patente", columnNames = "patente")
    }
)


public class Vehiculo {

    @Id ///PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) /// genera el valor de la PK de manera automática
    private Long id;

    @NotBlank(message = "La patente es obligatoria")
    @Column(nullable = false, unique = true, updatable = false) /// no puede ser nula, debe ser única y no se puede actualizar
    private String patente;

    @NotBlank(message = "La marca es obligatoria")
    @Column(nullable = false)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Column(nullable = false)
    private String modelo;

    @NotNull(message = "El año es obligatorio")
    private Integer anio;

    private String color;

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING) /// representa el tipo de vehículo (por ejemplo, sedán, SUV, etc.)
    private TipoVehiculo tipo;

    @Positive(message = "El precio diario debe ser mayor a cero")
    private double precioDiario;

    @Enumerated(EnumType.STRING) /// representa el estado del vehículo (por ejemplo, disponible, reservado, en alquiler)
    @Column(nullable = false)
    private EstadoVehiculo estado;

    @Column(nullable = false)
    private Boolean activo;

    public Vehiculo() {
    }

    public Long getId() {
        return id;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVehiculo tipo) {
        this.tipo = tipo;
    }

    public double getPrecioDiario() {
        return precioDiario;
    }

    public void setPrecioDiario(double precioDiario) {
        this.precioDiario = precioDiario;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}