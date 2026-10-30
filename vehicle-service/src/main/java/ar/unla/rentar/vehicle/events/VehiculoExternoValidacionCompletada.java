package ar.unla.rentar.vehicle.events;

public class VehiculoExternoValidacionCompletada {
    
    private Long vehiculoExternoId;
    private String resultadoFinal; //VALIDADO o RECHAZADO

    public VehiculoExternoValidacionCompletada() {
    }

    public VehiculoExternoValidacionCompletada(Long vehiculoExternoId, String resultadoFinal) {
        this.vehiculoExternoId = vehiculoExternoId;
        this.resultadoFinal = resultadoFinal;
    }

    public Long getVehiculoExternoId() {
        return vehiculoExternoId;
    }

    public void setVehiculoExternoId(Long vehiculoExternoId) {
        this.vehiculoExternoId = vehiculoExternoId;
    }

    public String getResultadoFinal() {
        return resultadoFinal;
    }

    public void setResultadoFinal(String resultadoFinal) {
        this.resultadoFinal = resultadoFinal;
    }
}