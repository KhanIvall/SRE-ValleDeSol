package com.valledelsol.incidentes.factory;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ControladoEstadoHandler implements EstadoIncidenteHandler {

    @Override
    public EstadoIncidente getEstado() {
        return EstadoIncidente.CONTROLADO;
    }

    @Override
    public void validarTransicion(Incidente incidente) throws BusinessRulesException {
        if (incidente.getEstado() != EstadoIncidente.EN_PROGRESO) {
            throw new BusinessRulesException(
                    "INC-003",
                    HttpStatus.PRECONDITION_FAILED,
                    "Solo un incidente en progreso puede pasar a controlado");
        }
    }
}
