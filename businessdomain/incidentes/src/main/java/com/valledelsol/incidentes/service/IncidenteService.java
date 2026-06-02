package com.valledelsol.incidentes.service;

import com.valledelsol.incidentes.common.BusinessRulesException;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import com.valledelsol.incidentes.factory.EstadoIncidenteFactory;
import com.valledelsol.incidentes.repository.IncidenteRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final EstadoIncidenteFactory estadoIncidenteFactory;

    public IncidenteService(
            IncidenteRepository incidenteRepository, EstadoIncidenteFactory estadoIncidenteFactory) {
        this.incidenteRepository = incidenteRepository;
        this.estadoIncidenteFactory = estadoIncidenteFactory;
    }

    public List<Incidente> listar() {
        return incidenteRepository.findAll();
    }

    public Optional<Incidente> obtenerPorId(Long id) {
        return incidenteRepository.findById(id);
    }

    @Transactional
    public Incidente crear(Incidente incidente) throws BusinessRulesException {
        incidente.setEstado(EstadoIncidente.REPORTADO);
        incidente.setFechaReporte(incidente.getFechaReporte() != null ? incidente.getFechaReporte() : Instant.now());
        estadoIncidenteFactory.aplicarEstado(incidente, EstadoIncidente.REPORTADO);
        return incidenteRepository.save(incidente);
    }

    @Transactional
    public Incidente actualizar(Long id, Incidente datos) throws BusinessRulesException {
        Incidente existente = incidenteRepository
                .findById(id)
                .orElseThrow(() -> new BusinessRulesException(
                        "INC-404", org.springframework.http.HttpStatus.NOT_FOUND, "Incidente no encontrado"));

        if (datos.getTipo() != null) {
            existente.setTipo(datos.getTipo());
        }
        if (datos.getDescripcion() != null) {
            existente.setDescripcion(datos.getDescripcion());
        }
        if (datos.getLatitud() != null) {
            existente.setLatitud(datos.getLatitud());
        }
        if (datos.getLongitud() != null) {
            existente.setLongitud(datos.getLongitud());
        }

        return incidenteRepository.save(existente);
    }

    @Transactional
    public Incidente cambiarEstado(Long id, EstadoIncidente nuevoEstado) throws BusinessRulesException {
        Incidente incidente = incidenteRepository
                .findById(id)
                .orElseThrow(() -> new BusinessRulesException(
                        "INC-404", org.springframework.http.HttpStatus.NOT_FOUND, "Incidente no encontrado"));
        estadoIncidenteFactory.aplicarEstado(incidente, nuevoEstado);
        return incidenteRepository.save(incidente);
    }

    @Transactional
    public void eliminar(Long id) {
        incidenteRepository.deleteById(id);
    }
}
