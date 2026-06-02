package com.valledelsol.incidentes.factory;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import org.springframework.stereotype.Component;

@Component
public class ReportadoEstadoHandler implements EstadoIncidenteHandler {

    @Override
    public EstadoIncidente getEstado() {
        return EstadoIncidente.REPORTADO;
    }

    @Override
    public void validarTransicion(Incidente incidente) throws BusinessRulesException {
        if (incidente.getTipo() == null || incidente.getTipo().isBlank()) {
            throw new BusinessRulesException(
                    "INC-001",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "El tipo de incidente es obligatorio al reportar");
        }
    }
}
