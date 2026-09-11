package ar.unla.rentar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import lombok.*;

@Entity                                                     //clase representa una tabla en la base de datos
@Table(name = "cliente")                                   //como lo llamamos en la tabla
@Data

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

}