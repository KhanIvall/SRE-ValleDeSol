package com.valledelsol.zonasriesgo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import com.valledelsol.zonasriesgo.adapter.WeatherDataPort;
import com.valledelsol.zonasriesgo.adapter.WeatherSnapshot;
import com.valledelsol.zonasriesgo.entities.NivelRiesgo;
import com.valledelsol.zonasriesgo.entities.ZonaRiesgo;
import com.valledelsol.zonasriesgo.repository.ZonaRiesgoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ZonaRiesgoServiceTest {

    @Mock
    private ZonaRiesgoRepository zonaRiesgoRepository;

    @Mock
    private WeatherDataPort weatherDataPort;

    @InjectMocks
    private ZonaRiesgoService zonaRiesgoService;

    @Test
    void crear_conClimaCritico_asignaNivelCritico() throws Exception {
        when(weatherDataPort.obtenerCondiciones(anyDouble(), anyDouble()))
                .thenReturn(new WeatherSnapshot(25, 75, 18, "SECO_VIENTOSO"));
        when(zonaRiesgoRepository.save(org.mockito.ArgumentMatchers.any(ZonaRiesgo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ZonaRiesgo entrada = new ZonaRiesgo();
        entrada.setNombre("Cerro Alto");
        entrada.setLatitud(-33.4);
        entrada.setLongitud(-70.6);

        ZonaRiesgo resultado = zonaRiesgoService.crear(entrada);

        assertEquals(NivelRiesgo.CRITICO, resultado.getNivelRiesgo());
        assertEquals("SECO_VIENTOSO", resultado.getCondicionClimatica());
    }
}
