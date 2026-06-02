package com.valledelsol.recursos.service;

import com.valledelsol.recursos.common.BusinessRulesException;
import com.valledelsol.recursos.entities.EstadoRecurso;
import com.valledelsol.recursos.entities.Recurso;
import com.valledelsol.recursos.entities.TipoRecurso;
import com.valledelsol.recursos.repository.RecursoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecursoService {

    private final RecursoRepository recursoRepository;

    public RecursoService(RecursoRepository recursoRepository) {
        this.recursoRepository = recursoRepository;
    }

    public List<Recurso> listar() {
        return recursoRepository.findAll();
    }

    public List<Recurso> listarDisponibles() {
        return recursoRepository.findByEstado(EstadoRecurso.DISPONIBLE);
    }

    public List<Recurso> listarPorIncidente(Long incidenteId) {
        return recursoRepository.findByIncidenteAsignadoId(incidenteId);
    }

    public Optional<Recurso> obtenerPorId(Long id) {
        return recursoRepository.findById(id);
    }

    @Transactional
    public Recurso crear(Recurso recurso) throws BusinessRulesException {
        validarRecurso(recurso);
        if (recurso.getEstado() == null) {
            recurso.setEstado(EstadoRecurso.DISPONIBLE);
        }
        return recursoRepository.save(recurso);
    }

    @Transactional
    public Recurso actualizar(Long id, Recurso datos) throws BusinessRulesException {
        Recurso existente = recursoRepository
                .findById(id)
                .orElseThrow(() -> new BusinessRulesException(
                        "REC-404", HttpStatus.NOT_FOUND, "Recurso no encontrado"));

        if (datos.getNombre() != null) {
            existente.setNombre(datos.getNombre());
        }
        if (datos.getTipo() != null) {
            existente.setTipo(datos.getTipo());
        }
        if (datos.getIdentificador() != null) {
            existente.setIdentificador(datos.getIdentificador());
        }

        return recursoRepository.save(existente);
    }

    @Transactional
    public Recurso asignarAIncidente(Long recursoId, Long incidenteId) throws BusinessRulesException {
        Recurso recurso = recursoRepository
                .findById(recursoId)
                .orElseThrow(() -> new BusinessRulesException(
                        "REC-404", HttpStatus.NOT_FOUND, "Recurso no encontrado"));

        if (recurso.getEstado() != EstadoRecurso.DISPONIBLE) {
            throw new BusinessRulesException(
                    "REC-001",
                    HttpStatus.PRECONDITION_FAILED,
                    "Solo recursos disponibles pueden asignarse a un incidente");
        }

        recurso.setEstado(EstadoRecurso.DESPLEGADO);
        recurso.setIncidenteAsignadoId(incidenteId);
        return recursoRepository.save(recurso);
    }

    @Transactional
    public Recurso liberar(Long recursoId) throws BusinessRulesException {
        Recurso recurso = recursoRepository
                .findById(recursoId)
                .orElseThrow(() -> new BusinessRulesException(
                        "REC-404", HttpStatus.NOT_FOUND, "Recurso no encontrado"));

        recurso.setEstado(EstadoRecurso.DISPONIBLE);
        recurso.setIncidenteAsignadoId(null);
        return recursoRepository.save(recurso);
    }

    @Transactional
    public void eliminar(Long id) {
        recursoRepository.deleteById(id);
    }

    private void validarRecurso(Recurso recurso) throws BusinessRulesException {
        if (recurso.getNombre() == null || recurso.getNombre().isBlank()) {
            throw new BusinessRulesException("REC-002", HttpStatus.BAD_REQUEST, "El nombre del recurso es obligatorio");
        }
        if (recurso.getTipo() == null) {
            throw new BusinessRulesException("REC-003", HttpStatus.BAD_REQUEST, "El tipo de recurso es obligatorio");
        }
        if (recurso.getTipo() == TipoRecurso.VEHICULO
                && (recurso.getIdentificador() == null || recurso.getIdentificador().isBlank())) {
            throw new BusinessRulesException(
                    "REC-004", HttpStatus.BAD_REQUEST, "Los vehiculos requieren patente o identificador");
        }
    }
}
