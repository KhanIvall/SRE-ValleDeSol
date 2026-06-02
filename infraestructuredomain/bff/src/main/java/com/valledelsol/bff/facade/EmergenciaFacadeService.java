package com.valledelsol.bff.facade;

import com.valledelsol.bff.dto.EmergenciaResumenDto;
import com.valledelsol.bff.dto.IncidenteDto;
import com.valledelsol.bff.dto.RecursoDto;
import com.valledelsol.bff.dto.ZonaRiesgoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Collections;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Facade BFF: agrega en paralelo incidentes, recursos y zona de riesgo para el frontend.
 */
@Service
public class EmergenciaFacadeService {

    private final WebClient.Builder webClientBuilder;

    public EmergenciaFacadeService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @CircuitBreaker(name = "bffEmergencia", fallbackMethod = "resumenFallback")
    public Mono<EmergenciaResumenDto> obtenerResumen(Long incidenteId) {
        WebClient client = webClientBuilder.build();

        Mono<IncidenteDto> incidenteMono = client.get()
                .uri("http://INCIDENTES/incidentes/{id}", incidenteId)
                .retrieve()
                .bodyToMono(IncidenteDto.class);

        Mono<List<RecursoDto>> recursosMono = client.get()
                .uri("http://RECURSOS/recursos/incidente/{id}", incidenteId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RecursoDto>>() {})
                .onErrorReturn(WebClientResponseException.NotFound.class, Collections.emptyList())
                .defaultIfEmpty(Collections.emptyList());

        return incidenteMono.flatMap(incidente -> {
            Mono<ZonaRiesgoDto> zonaMono = obtenerZonaPorCoordenadas(client, incidente);
            return Mono.zip(recursosMono, zonaMono.defaultIfEmpty(new ZonaRiesgoDto()))
                    .map(tuple -> construirResumen(incidenteId, incidente, tuple.getT1(), tuple.getT2()));
        });
    }

    private Mono<ZonaRiesgoDto> obtenerZonaPorCoordenadas(WebClient client, IncidenteDto incidente) {
        if (incidente.getLatitud() == null || incidente.getLongitud() == null) {
            return Mono.empty();
        }
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("ZONASRIESGO")
                        .path("/zonas-riesgo/coordenadas")
                        .queryParam("latitud", incidente.getLatitud())
                        .queryParam("longitud", incidente.getLongitud())
                        .build())
                .retrieve()
                .bodyToMono(ZonaRiesgoDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty());
    }

    private EmergenciaResumenDto construirResumen(
            Long incidenteId, IncidenteDto incidente, List<RecursoDto> recursos, ZonaRiesgoDto zona) {
        EmergenciaResumenDto resumen = new EmergenciaResumenDto();
        resumen.setIncidenteId(incidenteId);
        resumen.setIncidente(incidente);
        resumen.setRecursosAsignados(recursos);
        if (zona.getId() != null || zona.getNivelRiesgo() != null) {
            resumen.setZonaRiesgo(zona);
        }
        resumen.setDatosContingencia(false);
        return resumen;
    }

    Mono<EmergenciaResumenDto> resumenFallback(Long incidenteId, Throwable throwable) {
        EmergenciaResumenDto resumen = new EmergenciaResumenDto();
        resumen.setIncidenteId(incidenteId);
        resumen.setDatosContingencia(true);
        resumen.setMensajeContingencia(
                "Respuesta de contingencia: uno o mas servicios no estan disponibles (" + throwable.getMessage() + ")");
        resumen.setRecursosAsignados(Collections.emptyList());

        IncidenteDto incidenteStub = new IncidenteDto();
        incidenteStub.setId(incidenteId);
        incidenteStub.setEstado("DESCONOCIDO");
        incidenteStub.setTipo("CONTINGENCIA");
        resumen.setIncidente(incidenteStub);

        ZonaRiesgoDto zonaStub = new ZonaRiesgoDto();
        zonaStub.setNivelRiesgo("MEDIO");
        zonaStub.setCondicionClimatica("CACHE_FALLBACK");
        resumen.setZonaRiesgo(zonaStub);

        return Mono.just(resumen);
    }
}
