package ${package}.facade;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Facade de ejemplo: reemplace las URLs por sus microservicios Eureka.
 */
@Service
public class SampleFacadeService {

    private final WebClient.Builder webClientBuilder;

    public SampleFacadeService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @CircuitBreaker(name = "bffSample", fallbackMethod = "fallbackMensaje")
    public Mono<String> obtenerSaludo() {
        return webClientBuilder
                .build()
                .get()
                .uri("http://INCIDENTES/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> "BFF operativo - incidentes responde");
    }

    @SuppressWarnings("unused")
    Mono<String> fallbackMensaje(Throwable ex) {
        return Mono.just("BFF en contingencia: " + ex.getMessage());
    }
}
