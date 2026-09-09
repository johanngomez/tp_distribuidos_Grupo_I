package ar.unla.rentar.model;

public class Usuario {

    private Long id;
    private String email;
    private String password;
    private Rol rol;
    private Boolean activo;

    public Usuario() {
    }

    public Usuario(Long id, String email, String password, Rol rol, Boolean activo) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Rol getRol() {
        return rol;
    }

    public Boolean getActivo() {
        return activo;
    }
}