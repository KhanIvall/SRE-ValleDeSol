package ${package}.controller;

import ${package}.common.BusinessRulesException;
import ${package}.entities.${entityName};
import ${package}.service.${entityName}Service;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${artifactId}")
public class ${entityName}RestController {

    private final ${entityName}Service service;

    public ${entityName}RestController(${entityName}Service service) {
        this.service = service;
    }

    @GetMapping
    public List<${entityName}> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<${entityName}> obtener(@PathVariable Long id) {
        return service.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<${entityName}> crear(@RequestBody ${entityName} body) throws BusinessRulesException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<${entityName}> actualizar(@PathVariable Long id, @RequestBody ${entityName} body)
            throws BusinessRulesException {
        return ResponseEntity.ok(service.actualizar(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
