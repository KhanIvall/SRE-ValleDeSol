package com.valledelsol.recursos.repository;

import com.valledelsol.recursos.entities.EstadoRecurso;
import com.valledelsol.recursos.entities.Recurso;
import com.valledelsol.recursos.entities.TipoRecurso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    List<Recurso> findByEstado(EstadoRecurso estado);

    List<Recurso> findByTipo(TipoRecurso tipo);

    List<Recurso> findByIncidenteAsignadoId(Long incidenteId);
}
