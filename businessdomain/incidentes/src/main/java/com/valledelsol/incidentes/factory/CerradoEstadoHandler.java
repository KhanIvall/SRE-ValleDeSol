package com.valledelsol.incidentes.factory;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CerradoEstadoHandler implements EstadoIncidenteHandler {

    @Override
    public EstadoIncidente getEstado() {
        return EstadoIncidente.CERRADO;
    }

    @Override
    public void validarTransicion(Incidente incidente) throws BusinessRulesException {
        if (incidente.getEstado() != EstadoIncidente.CONTROLADO) {
            throw new BusinessRulesException(
                    "INC-004",
                    HttpStatus.PRECONDITION_FAILED,
                    "Solo un incidente controlado puede cerrarse");
        }
    }
}
