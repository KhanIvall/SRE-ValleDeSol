package com.valledelsol.zonasriesgo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import com.valledelsol.zonasriesgo.adapter.WeatherDataPort;
import com.valledelsol.zonasriesgo.adapter.WeatherSnapshot;
import com.valledelsol.zonasriesgo.common.BusinessRulesException;
import com.valledelsol.zonasriesgo.entities.NivelRiesgo;
import com.valledelsol.zonasriesgo.entities.ZonaRiesgo;
import com.valledelsol.zonasriesgo.repository.ZonaRiesgoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ZonaRiesgoRecalibrarTest {

    @Mock
    private ZonaRiesgoRepository zonaRiesgoRepository;

    @Mock
    private WeatherDataPort weatherDataPort;

    @InjectMocks
    private ZonaRiesgoService zonaRiesgoService;

    @Test
    void recalibrar_zonaExistente_actualizaNivel() throws BusinessRulesException {
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setId(1L);
        zona.setNombre("Sector Sur");
        zona.setLatitud(-33.5);
        zona.setLongitud(-70.6);
        zona.setNivelRiesgo(NivelRiesgo.BAJO);

        when(zonaRiesgoRepository.findById(1L)).thenReturn(java.util.Optional.of(zona));
        when(weatherDataPort.obtenerCondiciones(anyDouble(), anyDouble()))
                .thenReturn(new WeatherSnapshot(32, 72, 16, "SECO_VIENTOSO"));
        when(zonaRiesgoRepository.save(org.mockito.ArgumentMatchers.any(ZonaRiesgo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ZonaRiesgo resultado = zonaRiesgoService.recalibrarRiesgo(1L);

        assertEquals(NivelRiesgo.CRITICO, resultado.getNivelRiesgo());
    }

    @Test
    void recalibrar_zonaInexistente_lanzaExcepcion() {
        when(zonaRiesgoRepository.findById(404L)).thenReturn(java.util.Optional.empty());

        assertThrows(BusinessRulesException.class, () -> zonaRiesgoService.recalibrarRiesgo(404L));
    }
}
