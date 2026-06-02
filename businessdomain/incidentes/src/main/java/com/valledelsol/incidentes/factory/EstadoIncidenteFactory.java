package com.valledelsol.incidentes.factory;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Factory Method: centraliza validaciones y transiciones de estado del incidente.
 */
@Component
public class EstadoIncidenteFactory {

    private final Map<EstadoIncidente, EstadoIncidenteHandler> handlers;

    public EstadoIncidenteFactory(List<EstadoIncidenteHandler> handlerList) {
        handlers = new EnumMap<>(EstadoIncidente.class);
        for (EstadoIncidenteHandler handler : handlerList) {
            handlers.put(handler.getEstado(), handler);
        }
    }

    public void aplicarEstado(Incidente incidente, EstadoIncidente nuevoEstado) throws BusinessRulesException {
        EstadoIncidenteHandler handler = handlers.get(nuevoEstado);
        if (handler == null) {
            throw new BusinessRulesException(
                    "INC-000",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Estado no soportado: " + nuevoEstado);
        }
        handler.validarTransicion(incidente);
        incidente.setEstado(nuevoEstado);
    }
}
