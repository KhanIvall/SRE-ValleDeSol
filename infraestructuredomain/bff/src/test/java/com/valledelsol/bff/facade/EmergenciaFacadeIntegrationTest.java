package com.valledelsol.bff.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valledelsol.bff.dto.IncidenteDto;
import com.valledelsol.bff.dto.RecursoDto;
import com.valledelsol.bff.dto.ZonaRiesgoDto;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del facade real {@link EmergenciaFacadeService} redirigiendo hosts Eureka al MockWebServer.
 */
class EmergenciaFacadeIntegrationTest {

    private MockWebServer mockWebServer;
    private EmergenciaFacadeService facadeService;
    private ObjectMapper objectMapper;
    private String mockBase;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockBase = mockWebServer.url("/").toString();
        if (mockBase.endsWith("/")) {
            mockBase = mockBase.substring(0, mockBase.length() - 1);
        }

        ExchangeFilterFunction redirectToMock = (request, next) -> {
            URI uri = request.url();
            String target = mockBase + uri.getRawPath()
                    + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
            return next.exchange(ClientRequest.from(request).url(URI.create(target)).build());
        };

        WebClient.Builder builder = WebClient.builder().filter(redirectToMock);
        facadeService = new EmergenciaFacadeService(builder);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void obtenerResumen_todosServiciosResponden_retornaResumenCompleto() throws Exception {
        IncidenteDto incidenteDto = new IncidenteDto();
        incidenteDto.setId(1L);
        incidenteDto.setTipo("INCENDIO_FORESTAL");
        incidenteDto.setEstado("EN_PROGRESO");
        incidenteDto.setLatitud(-33.45);
        incidenteDto.setLongitud(-70.66);

        RecursoDto recursoDto = new RecursoDto();
        recursoDto.setId(10L);
        recursoDto.setNombre("Brigada Norte");
        recursoDto.setEstado("DESPLEGADO");

        ZonaRiesgoDto zonaDto = new ZonaRiesgoDto();
        zonaDto.setId(5L);
        zonaDto.setNivelRiesgo("ALTO");
        zonaDto.setCondicionClimatica("SECO_VIENTOSO");

        mockWebServer.enqueue(jsonResponse(incidenteDto));
        mockWebServer.enqueue(jsonResponse(List.of(recursoDto)));
        mockWebServer.enqueue(jsonResponse(zonaDto));

        StepVerifier.create(facadeService.obtenerResumen(1L))
                .assertNext(resumen -> {
                    assertFalse(resumen.isDatosContingencia());
                    assertEquals(1L, resumen.getIncidenteId());
                    assertEquals("INCENDIO_FORESTAL", resumen.getIncidente().getTipo());
                    assertEquals(1, resumen.getRecursosAsignados().size());
                    assertEquals("Brigada Norte", resumen.getRecursosAsignados().get(0).getNombre());
                    assertNotNull(resumen.getZonaRiesgo());
                    assertEquals("ALTO", resumen.getZonaRiesgo().getNivelRiesgo());
                })
                .verifyComplete();
    }

    @Test
    void obtenerResumen_incidenteSinCoordenadas_zonaRiesgoEsNull() throws Exception {
        IncidenteDto incidenteDto = new IncidenteDto();
        incidenteDto.setId(2L);
        incidenteDto.setTipo("DERRUMBE");
        incidenteDto.setEstado("REPORTADO");

        mockWebServer.enqueue(jsonResponse(incidenteDto));
        mockWebServer.enqueue(jsonResponse(List.of()));

        StepVerifier.create(facadeService.obtenerResumen(2L))
                .assertNext(resumen -> {
                    assertFalse(resumen.isDatosContingencia());
                    assertEquals(2L, resumen.getIncidenteId());
                    assertNull(resumen.getZonaRiesgo());
                    assertTrue(resumen.getRecursosAsignados().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void obtenerResumen_incidenteNoEncontrado_propaga404() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        StepVerifier.create(facadeService.obtenerResumen(99L))
                .expectErrorSatisfies(ex -> {
                    assertInstanceOf(ResponseStatusException.class, ex);
                    assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) ex).getStatusCode());
                })
                .verify();
    }

    @Test
    void obtenerResumen_zonaNoEncontrada_continuaSinZona() throws Exception {
        IncidenteDto incidenteDto = new IncidenteDto();
        incidenteDto.setId(3L);
        incidenteDto.setTipo("INCENDIO");
        incidenteDto.setLatitud(-33.45);
        incidenteDto.setLongitud(-70.66);

        mockWebServer.enqueue(jsonResponse(incidenteDto));
        mockWebServer.enqueue(jsonResponse(List.of()));
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        StepVerifier.create(facadeService.obtenerResumen(3L))
                .assertNext(resumen -> {
                    assertNull(resumen.getZonaRiesgo());
                    assertEquals(3L, resumen.getIncidenteId());
                })
                .verifyComplete();
    }

    @Test
    void resumenFallback_activado_retornaDatosContingencia() {
        StepVerifier.create(facadeService.resumenFallback(7L, new RuntimeException("servicio no disponible")))
                .assertNext(resumen -> {
                    assertTrue(resumen.isDatosContingencia());
                    assertEquals(7L, resumen.getIncidenteId());
                    assertNotNull(resumen.getMensajeContingencia());
                    assertEquals("CONTINGENCIA", resumen.getIncidente().getTipo());
                    assertEquals("DESCONOCIDO", resumen.getIncidente().getEstado());
                    assertEquals("MEDIO", resumen.getZonaRiesgo().getNivelRiesgo());
                    assertTrue(resumen.getRecursosAsignados().isEmpty());
                })
                .verifyComplete();
    }

    private MockResponse jsonResponse(Object body) throws Exception {
        return new MockResponse()
                .setBody(objectMapper.writeValueAsString(body))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }
}
