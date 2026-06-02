package com.valledelsol.recursos.controller;

import com.valledelsol.recursos.common.BusinessRulesException;
import com.valledelsol.recursos.entities.Recurso;
import com.valledelsol.recursos.service.RecursoService;
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
@RequestMapping("/recursos")
public class RecursoRestController {

    private final RecursoService recursoService;

    public RecursoRestController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping
    public List<Recurso> listar() {
        return recursoService.listar();
    }

    @GetMapping("/disponibles")
    public List<Recurso> listarDisponibles() {
        return recursoService.listarDisponibles();
    }

    @GetMapping("/incidente/{incidenteId}")
    public List<Recurso> listarPorIncidente(@PathVariable Long incidenteId) {
        return recursoService.listarPorIncidente(incidenteId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recurso> obtener(@PathVariable Long id) {
        return recursoService
                .obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Recurso> crear(@RequestBody Recurso recurso) throws BusinessRulesException {
        return ResponseEntity.status(HttpStatus.CREATED).body(recursoService.crear(recurso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recurso> actualizar(@PathVariable Long id, @RequestBody Recurso recurso)
            throws BusinessRulesException {
        return ResponseEntity.ok(recursoService.actualizar(id, recurso));
    }

    @PutMapping("/{id}/asignar")
    public ResponseEntity<Recurso> asignar(@PathVariable Long id, @RequestBody Map<String, Long> body)
            throws BusinessRulesException {
        return ResponseEntity.ok(recursoService.asignarAIncidente(id, body.get("incidenteId")));
    }

    @PutMapping("/{id}/liberar")
    public ResponseEntity<Recurso> liberar(@PathVariable Long id) throws BusinessRulesException {
        return ResponseEntity.ok(recursoService.liberar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        recursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
