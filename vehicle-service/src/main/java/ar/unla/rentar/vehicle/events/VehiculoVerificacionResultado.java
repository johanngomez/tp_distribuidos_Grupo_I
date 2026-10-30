package ar.unla.rentar.vehicle.events;
/* Payload para el evento de resultado de verificación de vehículo. (P9)*/

public class VehiculoVerificacionResultado {
    private Long vehiculoId;
    private String patente;
    private String resultado; // "VALIDADO" o "ROBADO"

    public VehiculoVerificacionResultado() {}

    public VehiculoVerificacionResultado(Long vehiculoId, String patente, String resultado) {
        this.vehiculoId = vehiculoId;
        this.patente = patente;
        this.resultado = resultado;
    }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
}
