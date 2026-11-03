package co.edu.unicauca.bancopreguntas.contracts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser para deserializar eventos de dominio enviados como JSON/Map entre microservicios.
 */
public class EventParser {

    public static DomainEvent parse(Map<String, Object> payload) {
        if (payload == null) return null;
        String eventType = (String) payload.get("eventType");
        if (eventType == null) return null;

        String correlationId = (String) payload.get("correlationId");

        switch (eventType) {
            case RevisoresAsignadosEvent.EVENT_TYPE: {
                Long preguntaId = getLong(payload, "preguntaId");
                Long autorId = getLong(payload, "autorId");
                String autorNombre = (String) payload.get("autorNombre");
                String contexto = (String) payload.get("contexto");
                List<RevisoresAsignadosEvent.RevisorInfo> revisores = new ArrayList<>();
                if (payload.get("revisores") instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> m) {
                            Long revId = getLongFromMap(m, "revisorId");
                            String nombre = (String) m.get("nombreCompleto");
                            String email = (String) m.get("email");
                            revisores.add(new RevisoresAsignadosEvent.RevisorInfo(revId, nombre, email));
                        }
                    }
                }
                return new RevisoresAsignadosEvent(preguntaId, autorId, autorNombre, contexto, revisores, correlationId);
            }
            case PreguntaEnviadaARevisionEvent.EVENT_TYPE: {
                Long preguntaId = getLong(payload, "preguntaId");
                Long autorId = getLong(payload, "autorId");
                String autorNombre = (String) payload.get("autorNombre");
                String contexto = (String) payload.get("contexto");
                String preguntaDirecta = (String) payload.get("preguntaDirecta");
                return new PreguntaEnviadaARevisionEvent(preguntaId, autorId, autorNombre, contexto, preguntaDirecta, correlationId);
            }
            case PreguntaAprobadaEvent.EVENT_TYPE: {
                Long preguntaId = getLong(payload, "preguntaId");
                Long autorId = getLong(payload, "autorId");
                String autorNombre = (String) payload.get("autorNombre");
                String autorEmail = (String) payload.get("autorEmail");
                String contexto = (String) payload.get("contexto");
                String comentario = (String) payload.get("comentarioGeneral");
                return new PreguntaAprobadaEvent(preguntaId, autorId, autorNombre, autorEmail, contexto, comentario, correlationId);
            }
            case PreguntaRechazadaEvent.EVENT_TYPE: {
                Long preguntaId = getLong(payload, "preguntaId");
                Long autorId = getLong(payload, "autorId");
                String autorNombre = (String) payload.get("autorNombre");
                String autorEmail = (String) payload.get("autorEmail");
                String contexto = (String) payload.get("contexto");
                List<String> obs = new ArrayList<>();
                if (payload.get("observaciones") instanceof List<?> list) {
                    for (Object o : list) {
                        if (o != null) obs.add(o.toString());
                    }
                }
                return new PreguntaRechazadaEvent(preguntaId, autorId, autorNombre, autorEmail, contexto, obs, correlationId);
            }
            default:
                return null;
        }
    }

    private static Long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) {
            try { return Long.parseLong(s); } catch (Exception ignored) {}
        }
        return null;
    }

    private static Long getLongFromMap(Map<?, ?> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) {
            try { return Long.parseLong(s); } catch (Exception ignored) {}
        }
        return null;
    }
}

