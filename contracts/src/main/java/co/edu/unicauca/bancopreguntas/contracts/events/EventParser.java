package co.edu.unicauca.bancopreguntas.contracts.events;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Parser para deserializar eventos de dominio enviados como JSON/Map entre microservicios.
 */
public class EventParser {

    public static DomainEvent parse(Map<String, Object> payload) {
        if (payload == null) return null;
        String eventType = (String) payload.get("eventType");
        if (eventType == null) return null;

        String rawEventId = payload.get("eventId") instanceof String s && !s.isBlank()
                ? s
                : UUID.randomUUID().toString();
        LocalDateTime occurredOn = parseLocalDateTime(payload.get("occurredOn"));
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
                return new RevisoresAsignadosEvent(rawEventId, preguntaId, autorId, autorNombre, contexto, revisores, occurredOn, correlationId);
            }
            case PreguntaEnviadaARevisionEvent.EVENT_TYPE: {
                Long preguntaId = getLong(payload, "preguntaId");
                Long autorId = getLong(payload, "autorId");
                String autorNombre = (String) payload.get("autorNombre");
                String contexto = (String) payload.get("contexto");
                String preguntaDirecta = (String) payload.get("preguntaDirecta");
                Long verLong = getLong(payload, "version");
                Integer version = verLong != null ? verLong.intValue() : 1;
                List<String> revisoresAnteriores = new ArrayList<>();
                if (payload.get("revisoresAnteriores") instanceof List<?> list) {
                    for (Object o : list) {
                        if (o != null && !o.toString().isBlank()) {
                            revisoresAnteriores.add(o.toString());
                        }
                    }
                }
                return new PreguntaEnviadaARevisionEvent(rawEventId, preguntaId, autorId, autorNombre, contexto, preguntaDirecta, version, revisoresAnteriores, occurredOn, correlationId);
            }
            case PreguntaAprobadaEvent.EVENT_TYPE: {
                Long preguntaId = getLong(payload, "preguntaId");
                Long autorId = getLong(payload, "autorId");
                String autorNombre = (String) payload.get("autorNombre");
                String autorEmail = (String) payload.get("autorEmail");
                String contexto = (String) payload.get("contexto");
                String comentario = (String) payload.get("comentarioGeneral");
                return new PreguntaAprobadaEvent(rawEventId, preguntaId, autorId, autorNombre, autorEmail, contexto, comentario, occurredOn, correlationId);
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
                return new PreguntaRechazadaEvent(rawEventId, preguntaId, autorId, autorNombre, autorEmail, contexto, obs, occurredOn, correlationId);
            }
            default:
                return null;
        }
    }

    private static LocalDateTime parseLocalDateTime(Object val) {
        if (val instanceof String s && !s.isBlank()) {
            try {
                return LocalDateTime.parse(s);
            } catch (Exception ignored) {}
        }
        if (val instanceof List<?> parts && parts.size() >= 5) {
            try {
                int y = ((Number) parts.get(0)).intValue();
                int m = ((Number) parts.get(1)).intValue();
                int d = ((Number) parts.get(2)).intValue();
                int h = ((Number) parts.get(3)).intValue();
                int min = ((Number) parts.get(4)).intValue();
                int sec = parts.size() > 5 ? ((Number) parts.get(5)).intValue() : 0;
                int nano = parts.size() > 6 ? ((Number) parts.get(6)).intValue() : 0;
                return LocalDateTime.of(y, m, d, h, min, sec, nano);
            } catch (Exception ignored) {}
        }
        return LocalDateTime.now();
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

