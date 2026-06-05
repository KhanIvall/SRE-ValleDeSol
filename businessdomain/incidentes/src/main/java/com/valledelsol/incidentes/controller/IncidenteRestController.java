package com.valledelsol.incidentes.controller;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import com.valledelsol.incidentes.service.IncidenteService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incidentes")
public class IncidenteRestController {

    private final IncidenteService incidenteService;

    public IncidenteRestController(IncidenteService incidenteService) {
        this.incidenteService = incidenteService;
    }

    @GetMapping
    public List<Incidente> listar() {
        return incidenteService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incidente> obtener(@PathVariable("id") Long id) {
        return incidenteService
                .obtenerPorId(id)
                .map(incidente -> ResponseEntity.ok(incidente))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Incidente> crear(@RequestBody Incidente incidente) throws BusinessRulesException {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenteService.crear(incidente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incidente> actualizar(@PathVariable("id") Long id, @RequestBody Incidente incidente)
            throws BusinessRulesException {
        return ResponseEntity.ok(incidenteService.actualizar(id, incidente));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Incidente> cambiarEstado(
            @PathVariable("id") Long id, @RequestBody Map<String, String> body) throws BusinessRulesException {
        EstadoIncidente nuevoEstado = EstadoIncidente.valueOf(body.get("estado"));
        return ResponseEntity.ok(incidenteService.cambiarEstado(id, nuevoEstado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        incidenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
