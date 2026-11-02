package ar.unla.rentar.vehicle.events;

public class EventEnvelope {
    private String eventId;
    private String eventType;
    private String correlationId;
    private String occurredAt;
    private Object payload;

    public EventEnvelope() {}

    public EventEnvelope(String eventId, String eventType, String correlationId, String occurredAt, Object payload) {
        this.eventId = eventId;    // Identifica este evento concreto
        this.eventType = eventType;  // Indica qué evento es.
        this.correlationId = correlationId; // Relaciona los distintos eventos del mismo proceso.
        this.occurredAt = occurredAt;  // Indica cuándo ocurrió el evento.
        this.payload = payload;  // Contiene los datos de negocio necesarios para procesarlo.
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}
