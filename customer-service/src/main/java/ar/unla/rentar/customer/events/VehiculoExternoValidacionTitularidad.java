package ar.unla.rentar.customer.events;

public class VehiculoExternoValidacionTitularidad {
    
    private Long vehiculoExternoId;
    private Long clienteId;
    private String resultado; // VALIDADO o RECHAZADO

    public VehiculoExternoValidacionTitularidad() {
    }

    public VehiculoExternoValidacionTitularidad(Long vehiculoExternoId, Long clienteId, String resultado) {
        this.vehiculoExternoId = vehiculoExternoId;
        this.clienteId = clienteId;
        this.resultado = resultado;
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

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}