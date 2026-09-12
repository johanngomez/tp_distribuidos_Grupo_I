package ar.unla.rentar.dto;

public class LoginResponseDTO {

    private String token;
    private String tipo;
    private boolean esAdmin;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String tipo, boolean esAdmin) {
        this.token = token;
        this.tipo = tipo;
        this.esAdmin = esAdmin;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }
}
