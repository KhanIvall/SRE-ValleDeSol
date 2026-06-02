package com.valledelsol.incidentes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import com.valledelsol.incidentes.factory.EstadoIncidenteFactory;
import com.valledelsol.incidentes.repository.IncidenteRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class IncidenteServiceTest {

    @Mock
    private IncidenteRepository incidenteRepository;

    @Mock
    private EstadoIncidenteFactory estadoIncidenteFactory;

    @InjectMocks
    private IncidenteService incidenteService;

    @Test
    void crear_incidenteValido_persisteConEstadoReportado() throws BusinessRulesException {
        Incidente entrada = new Incidente();
        entrada.setTipo("INCENDIO_FORESTAL");

        when(incidenteRepository.save(any(Incidente.class))).thenAnswer(inv -> {
            Incidente saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Incidente resultado = incidenteService.crear(entrada);

        assertEquals(EstadoIncidente.REPORTADO, resultado.getEstado());
        assertEquals(1L, resultado.getId());
        verify(estadoIncidenteFactory).aplicarEstado(any(Incidente.class), eq(EstadoIncidente.REPORTADO));
    }

    @Test
    void cambiarEstado_incidenteInexistente_lanzaExcepcion() {
        when(incidenteRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessRulesException ex =
                assertThrows(BusinessRulesException.class, () -> incidenteService.cambiarEstado(99L, EstadoIncidente.EN_PROGRESO));

        assertEquals("INC-404", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void cambiarEstado_sinCoordenadas_propagaReglaDeNegocio() throws BusinessRulesException {
        Incidente incidente = new Incidente();
        incidente.setId(1L);
        incidente.setTipo("INCENDIO");
        incidente.setEstado(EstadoIncidente.REPORTADO);

        when(incidenteRepository.findById(1L)).thenReturn(Optional.of(incidente));
        doThrow(new BusinessRulesException("INC-002", HttpStatus.PRECONDITION_FAILED, "Coordenadas requeridas"))
                .when(estadoIncidenteFactory)
                .aplicarEstado(incidente, EstadoIncidente.EN_PROGRESO);

        assertThrows(
                BusinessRulesException.class,
                () -> incidenteService.cambiarEstado(1L, EstadoIncidente.EN_PROGRESO));
    }
}
