package com.valledelsol.bff.facade;

import com.valledelsol.bff.dto.EmergenciaResumenDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class EmergenciaFacadeServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Test
    void resumenFallback_retornaDatosContingencia() {
        EmergenciaFacadeService facadeService = new EmergenciaFacadeService(webClientBuilder);

        StepVerifier.create(facadeService.resumenFallback(7L, new RuntimeException("timeout")))
                .assertNext(dto -> {
                    org.junit.jupiter.api.Assertions.assertTrue(dto.isDatosContingencia());
                    org.junit.jupiter.api.Assertions.assertEquals(7L, dto.getIncidenteId());
                    org.junit.jupiter.api.Assertions.assertEquals("CONTINGENCIA", dto.getIncidente().getTipo());
                })
                .verifyComplete();
    }
}
