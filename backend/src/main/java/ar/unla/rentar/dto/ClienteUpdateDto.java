package ar.unla.rentar.dto;

import java.time.LocalDate;

///Para cuando modificás datos permitidos de un cliente existente

public class ClienteUpdateDto {
    private String nombre;
    private String apellido;
    private String telefono;
    private LocalDate fechaNacimiento;

    public ClienteUpdateDto() {}

    // Getters y Setters
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
    
}