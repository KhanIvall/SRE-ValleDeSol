package com.valledelsol.incidentes.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstadoIncidenteFactoryTest {

    private EstadoIncidenteFactory factory;

    @BeforeEach
    void setUp() {
        factory = new EstadoIncidenteFactory(List.of(
                new ReportadoEstadoHandler(),
                new EnProgresoEstadoHandler(),
                new ControladoEstadoHandler(),
                new CerradoEstadoHandler()));
    }

    @Test
    void enProgreso_sinCoordenadas_lanzaExcepcion() {
        Incidente incidente = new Incidente();
        incidente.setTipo("INCENDIO_FORESTAL");
        incidente.setEstado(EstadoIncidente.REPORTADO);

        assertThrows(BusinessRulesException.class, () -> factory.aplicarEstado(incidente, EstadoIncidente.EN_PROGRESO));
    }

    @Test
    void enProgreso_conCoordenadas_actualizaEstado() throws BusinessRulesException {
        Incidente incidente = new Incidente();
        incidente.setTipo("INCENDIO_FORESTAL");
        incidente.setEstado(EstadoIncidente.REPORTADO);
        incidente.setLatitud(-33.45);
        incidente.setLongitud(-70.66);

        factory.aplicarEstado(incidente, EstadoIncidente.EN_PROGRESO);

        assertEquals(EstadoIncidente.EN_PROGRESO, incidente.getEstado());
    }
}
