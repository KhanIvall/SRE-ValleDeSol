package com.valledelsol.recursos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valledelsol.recursos.common.BusinessRulesException;
import com.valledelsol.recursos.entities.EstadoRecurso;
import com.valledelsol.recursos.entities.Recurso;
import com.valledelsol.recursos.entities.TipoRecurso;
import com.valledelsol.recursos.repository.RecursoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecursoServiceTest {

    @Mock
    private RecursoRepository recursoRepository;

    @InjectMocks
    private RecursoService recursoService;

    @Test
    void asignar_recursoNoDisponible_lanzaExcepcion() {
        Recurso recurso = new Recurso();
        recurso.setId(1L);
        recurso.setNombre("Brigada Norte");
        recurso.setTipo(TipoRecurso.BRIGADA);
        recurso.setEstado(EstadoRecurso.DESPLEGADO);

        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));

        assertThrows(BusinessRulesException.class, () -> recursoService.asignarAIncidente(1L, 99L));
    }

    @Test
    void asignar_recursoDisponible_actualizaEstado() throws BusinessRulesException {
        Recurso recurso = new Recurso();
        recurso.setId(1L);
        recurso.setNombre("Brigada Norte");
        recurso.setTipo(TipoRecurso.BRIGADA);
        recurso.setEstado(EstadoRecurso.DISPONIBLE);

        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurso resultado = recursoService.asignarAIncidente(1L, 42L);

        assertEquals(EstadoRecurso.DESPLEGADO, resultado.getEstado());
        assertEquals(42L, resultado.getIncidenteAsignadoId());
        verify(recursoRepository).save(recurso);
    }

    @Test
    void crear_sinNombre_lanzaExcepcion() {
        Recurso recurso = new Recurso();
        recurso.setTipo(TipoRecurso.BRIGADA);

        assertThrows(BusinessRulesException.class, () -> recursoService.crear(recurso));
    }

    @Test
    void liberar_recursoDesplegado_vuelveADisponible() throws BusinessRulesException {
        Recurso recurso = new Recurso();
        recurso.setId(2L);
        recurso.setNombre("Vehiculo 01");
        recurso.setTipo(TipoRecurso.VEHICULO);
        recurso.setEstado(EstadoRecurso.DESPLEGADO);
        recurso.setIncidenteAsignadoId(5L);

        when(recursoRepository.findById(2L)).thenReturn(Optional.of(recurso));
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurso resultado = recursoService.liberar(2L);

        assertEquals(EstadoRecurso.DISPONIBLE, resultado.getEstado());
        assertEquals(null, resultado.getIncidenteAsignadoId());
    }
}
