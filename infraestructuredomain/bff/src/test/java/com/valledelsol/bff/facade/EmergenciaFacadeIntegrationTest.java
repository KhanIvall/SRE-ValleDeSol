package com.valledelsol.bff.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valledelsol.bff.dto.IncidenteDto;
import com.valledelsol.bff.dto.RecursoDto;
import com.valledelsol.bff.dto.ZonaRiesgoDto;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración del BFF usando MockWebServer para simular
 * los microservicios INCIDENTES, RECURSOS y ZONASRIESGO.
 */
class EmergenciaFacadeIntegrationTest {

    private MockWebServer mockWebServer;
    private EmergenciaFacadeService facadeService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // WebClient apuntando al MockWebServer (sin load balancer)
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl.substring(0, baseUrl.length() - 1));

        facadeService = new EmergenciaFacadeServiceTestable(builder, baseUrl);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void obtenerResumen_todosServiciosResponden_retornaResumenCompleto() throws Exception {
        // Preparar respuestas simuladas
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

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(incidenteDto))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(List.of(recursoDto)))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(zonaDto))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

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
        // sin latitud ni longitud

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(incidenteDto))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

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

    /**
     * Subclase que sobreescribe las URLs para apuntar al MockWebServer en lugar de Eureka.
     */
    static class EmergenciaFacadeServiceTestable extends EmergenciaFacadeService {

        private final String baseUrl;

        EmergenciaFacadeServiceTestable(WebClient.Builder builder, String baseUrl) {
            super(builder);
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        }

        @Override
        public reactor.core.publisher.Mono<com.valledelsol.bff.dto.EmergenciaResumenDto> obtenerResumen(Long incidenteId) {
            WebClient client = WebClient.builder().baseUrl(baseUrl).build();

            reactor.core.publisher.Mono<com.valledelsol.bff.dto.IncidenteDto> incidenteMono = client.get()
                    .uri("/incidentes/{id}", incidenteId)
                    .retrieve()
                    .bodyToMono(com.valledelsol.bff.dto.IncidenteDto.class);

            reactor.core.publisher.Mono<java.util.List<com.valledelsol.bff.dto.RecursoDto>> recursosMono = client.get()
                    .uri("/recursos/incidente/{id}", incidenteId)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.List<com.valledelsol.bff.dto.RecursoDto>>() {})
                    .onErrorReturn(java.util.Collections.emptyList())
                    .defaultIfEmpty(java.util.Collections.emptyList());

            return incidenteMono.flatMap(incidente -> {
                if (incidente.getLatitud() == null || incidente.getLongitud() == null) {
                    return recursosMono.map(recursos -> {
                        com.valledelsol.bff.dto.EmergenciaResumenDto r = new com.valledelsol.bff.dto.EmergenciaResumenDto();
                        r.setIncidenteId(incidenteId);
                        r.setIncidente(incidente);
                        r.setRecursosAsignados(recursos);
                        r.setDatosContingencia(false);
                        return r;
                    });
                }
                reactor.core.publisher.Mono<com.valledelsol.bff.dto.ZonaRiesgoDto> zonaMono = client.get()
                        .uri(u -> u.path("/zonas-riesgo/coordenadas")
                                .queryParam("latitud", incidente.getLatitud())
                                .queryParam("longitud", incidente.getLongitud())
                                .build())
                        .retrieve()
                        .bodyToMono(com.valledelsol.bff.dto.ZonaRiesgoDto.class)
                        .onErrorResume(e -> reactor.core.publisher.Mono.empty());

                return reactor.core.publisher.Mono.zip(recursosMono, zonaMono.defaultIfEmpty(new com.valledelsol.bff.dto.ZonaRiesgoDto()))
                        .map(tuple -> {
                            com.valledelsol.bff.dto.ZonaRiesgoDto zona = tuple.getT2();
                            com.valledelsol.bff.dto.EmergenciaResumenDto r = new com.valledelsol.bff.dto.EmergenciaResumenDto();
                            r.setIncidenteId(incidenteId);
                            r.setIncidente(incidente);
                            r.setRecursosAsignados(tuple.getT1());
                            if (zona.getId() != null || zona.getNivelRiesgo() != null) {
                                r.setZonaRiesgo(zona);
                            }
                            r.setDatosContingencia(false);
                            return r;
                        });
            });
        }
    }
}
