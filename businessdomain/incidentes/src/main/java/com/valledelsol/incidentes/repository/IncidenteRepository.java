package com.valledelsol.incidentes.repository;

import com.valledelsol.incidentes.entities.Incidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidenteRepository extends JpaRepository<Incidente, Long> {
}
