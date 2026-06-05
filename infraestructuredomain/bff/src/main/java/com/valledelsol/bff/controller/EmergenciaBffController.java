package com.valledelsol.bff.controller;

import com.valledelsol.bff.dto.EmergenciaResumenDto;
import com.valledelsol.bff.facade.EmergenciaFacadeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff")
public class EmergenciaBffController {

    private final EmergenciaFacadeService emergenciaFacadeService;

    public EmergenciaBffController(EmergenciaFacadeService emergenciaFacadeService) {
        this.emergenciaFacadeService = emergenciaFacadeService;
    }

    @GetMapping("/emergencias/{incidenteId}/resumen")
    public Mono<EmergenciaResumenDto> resumen(@PathVariable("incidenteId") Long incidenteId) {
        return emergenciaFacadeService.obtenerResumen(incidenteId);
    }
}
