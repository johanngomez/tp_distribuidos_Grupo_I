package ar.unla.rentar.vehicle.events;

public class VehiculoExternoValidacionPatente {
    
    private Long vehiculoExternoId;
    private String patente;
    private String resultado; // VALIDADO o RECHAZADO

    public VehiculoExternoValidacionPatente() {
    }

    public VehiculoExternoValidacionPatente(Long vehiculoExternoId, String patente, String resultado) {
        this.vehiculoExternoId = vehiculoExternoId;
        this.patente = patente;
        this.resultado = resultado;
    }

    public Long getVehiculoExternoId() {
        return vehiculoExternoId;
    }

    public void setVehiculoExternoId(Long vehiculoExternoId) {
        this.vehiculoExternoId = vehiculoExternoId;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}