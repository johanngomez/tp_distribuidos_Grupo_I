package ar.unla.rentar.customer.events;

public class EventEnvelope {
    private String eventId;
    private String eventType;
    private String correlationId;
    private String occurredAt;
    private Object payload;

    public EventEnvelope() {
    }

    public EventEnvelope(String eventId, String eventType, String correlationId, String occurredAt, Object payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}