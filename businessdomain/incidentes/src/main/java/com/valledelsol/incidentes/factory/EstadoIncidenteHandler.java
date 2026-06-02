package com.valledelsol.incidentes.factory;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;

public interface EstadoIncidenteHandler {

    EstadoIncidente getEstado();

    void validarTransicion(Incidente incidente) throws BusinessRulesException;
}
