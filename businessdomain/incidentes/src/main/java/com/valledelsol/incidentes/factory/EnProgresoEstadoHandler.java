package com.valledelsol.incidentes.factory;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class EnProgresoEstadoHandler implements EstadoIncidenteHandler {

    @Override
    public EstadoIncidente getEstado() {
        return EstadoIncidente.EN_PROGRESO;
    }

    @Override
    public void validarTransicion(Incidente incidente) throws BusinessRulesException {
        if (incidente.getLatitud() == null || incidente.getLongitud() == null) {
            throw new BusinessRulesException(
                    "INC-002",
                    HttpStatus.PRECONDITION_FAILED,
                    "Se requieren coordenadas geoespaciales antes de despachar brigadas");
        }
    }
}
