package ${package}.controller;

import ${package}.facade.SampleFacadeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff")
public class SampleBffController {

    private final SampleFacadeService sampleFacadeService;

    public SampleBffController(SampleFacadeService sampleFacadeService) {
        this.sampleFacadeService = sampleFacadeService;
    }

    @GetMapping("/health-check")
    public Mono<String> healthCheck() {
        return sampleFacadeService.obtenerSaludo();
    }
}
