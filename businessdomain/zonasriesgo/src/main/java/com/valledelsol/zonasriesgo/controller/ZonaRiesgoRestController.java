package com.valledelsol.zonasriesgo.controller;

import com.valledelsol.zonasriesgo.common.BusinessRulesException;
import com.valledelsol.zonasriesgo.entities.ZonaRiesgo;
import com.valledelsol.zonasriesgo.service.ZonaRiesgoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zonas-riesgo")
public class ZonaRiesgoRestController {

    private final ZonaRiesgoService zonaRiesgoService;

    public ZonaRiesgoRestController(ZonaRiesgoService zonaRiesgoService) {
        this.zonaRiesgoService = zonaRiesgoService;
    }

    @GetMapping
    public List<ZonaRiesgo> listar() {
        return zonaRiesgoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZonaRiesgo> obtener(@PathVariable Long id) {
        return zonaRiesgoService
                .obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/coordenadas")
    public ResponseEntity<ZonaRiesgo> porCoordenadas(
            @RequestParam double latitud, @RequestParam double longitud) {
        return zonaRiesgoService
                .obtenerPorCoordenadas(latitud, longitud)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ZonaRiesgo> crear(@RequestBody ZonaRiesgo zona) throws BusinessRulesException {
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaRiesgoService.crear(zona));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZonaRiesgo> actualizar(@PathVariable Long id, @RequestBody ZonaRiesgo zona)
            throws BusinessRulesException {
        return ResponseEntity.ok(zonaRiesgoService.actualizar(id, zona));
    }

    @PutMapping("/{id}/recalibrar")
    public ResponseEntity<ZonaRiesgo> recalibrar(@PathVariable Long id) throws BusinessRulesException {
        return ResponseEntity.ok(zonaRiesgoService.recalibrarRiesgo(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        zonaRiesgoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
