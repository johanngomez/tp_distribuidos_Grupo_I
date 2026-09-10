package ar.unla.rentar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity                                                     //clase representa una tabla en la base de datos
@Table(name = "clientes")                                   //como lo llamaos en la tabla

public class Cliente {

    @Id                                                     //clave Primaria (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY)     //ID sea autoincremental (1,2,3...)
    private Long id;


    @NotBlank(message = "El documento es obligatorio")      //el documento es obligatorio (nullable = false) y único (unique = true)
    @Column(nullable = false, unique = true, length = 20)
    private String documento;


    @Column(nullable = false, length = 50)                  //el nombre y apellido son obligatorios
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;


    @Column(nullable = false, unique = true, length = 100)  //el email también es obligatorio y único 
    private String email;

    private String telefono;                                //al no tener @Column restrictivo, puede permitir nulos


    @Column(name = "fecha_nacimiento")                      //nombre de la columna en la BD con guion bajo (snake_case)
    private LocalDate fechaNacimiento;                      //en java con camelCase (fechaNacimiento)


    @Column(nullable = false)
    private boolean activo = true;                          //arranca activo por defecto


    public Cliente() {}

    public Cliente(String documento, String nombre, String apellido, String email, String telefono, LocalDate fechaNacimiento) {
        this.documento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.activo = true;
    }

    /// GETTERS Y SETTERS (leer y modificar los datos)
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getDocumento() {
        return documento; 
    }

    public void setDocumento(String documento) {
        this.documento = documento; 
    }

    public String getNombre() {
        return nombre; 
    }

    public void setNombre(String nombre) {
        this.nombre = nombre; 
    }

    public String getApellido() { 
        return apellido; 
    }

    public void setApellido(String apellido) {
        this.apellido = apellido; 
    }

    public String getEmail() {
        return email; 
    }

    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getTelefono() { 
        return telefono; 
    }

    public void setTelefono(String telefono) { 
        this.telefono = telefono; 
    }

    public LocalDate getFechaNacimiento() { 
        return fechaNacimiento; 
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) { 
        this.fechaNacimiento = fechaNacimiento; 
    }

    public boolean isActivo() { 
        return activo; 
    }

    public void setActivo(boolean activo) { 
        this.activo = activo; 
    }
}