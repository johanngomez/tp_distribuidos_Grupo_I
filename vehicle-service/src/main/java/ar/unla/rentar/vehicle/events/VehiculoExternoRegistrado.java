package ar.unla.rentar.vehicle.events;

public class VehiculoExternoRegistrado {

    private Long vehiculoExternoId; 
    private Long clienteId;
    private String patente;

    public VehiculoExternoRegistrado() {
    }

    public VehiculoExternoRegistrado(Long vehiculoExternoId, Long clienteId, String patente) {
        this.vehiculoExternoId = vehiculoExternoId;
        this.clienteId = clienteId;
        this.patente = patente;
    }

    public Long getVehiculoExternoId() {
        return vehiculoExternoId;
    }

    public void setVehiculoExternoId(Long vehiculoExternoId) {
        this.vehiculoExternoId = vehiculoExternoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }
}