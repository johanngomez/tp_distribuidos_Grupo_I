package ar.unla.rentar.vehicle.events;
/* Payload para el evento de solicitud de verificación de vehículo. (P9)*/
public class VehiculoVerificacionSolicitada {
    private Long vehiculoId;
    private String patente;

    public VehiculoVerificacionSolicitada() {}

    public VehiculoVerificacionSolicitada(Long vehiculoId, String patente) {
        this.vehiculoId = vehiculoId;
        this.patente = patente;
    }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }
}
