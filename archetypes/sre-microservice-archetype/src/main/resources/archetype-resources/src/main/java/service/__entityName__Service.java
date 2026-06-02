package ${package}.service;

import ${package}.common.BusinessRulesException;
import ${package}.entities.${entityName};
import ${package}.repository.${entityName}Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ${entityName}Service {

    private final ${entityName}Repository repository;

    public ${entityName}Service(${entityName}Repository repository) {
        this.repository = repository;
    }

    public List<${entityName}> listar() {
        return repository.findAll();
    }

    public Optional<${entityName}> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public ${entityName} crear(${entityName} entidad) throws BusinessRulesException {
        if (entidad.getNombre() == null || entidad.getNombre().isBlank()) {
            throw new BusinessRulesException("VAL-001", HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        return repository.save(entidad);
    }

    @Transactional
    public ${entityName} actualizar(Long id, ${entityName} datos) throws BusinessRulesException {
        ${entityName} existente = repository
                .findById(id)
                .orElseThrow(() -> new BusinessRulesException(
                        "VAL-404", HttpStatus.NOT_FOUND, "Registro no encontrado"));
        if (datos.getNombre() != null) {
            existente.setNombre(datos.getNombre());
        }
        if (datos.getDescripcion() != null) {
            existente.setDescripcion(datos.getDescripcion());
        }
        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
