package com.valledelsol.zonasriesgo.repository;

import com.valledelsol.zonasriesgo.entities.NivelRiesgo;
import com.valledelsol.zonasriesgo.entities.ZonaRiesgo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZonaRiesgoRepository extends JpaRepository<ZonaRiesgo, Long> {

    List<ZonaRiesgo> findByNivelRiesgo(NivelRiesgo nivelRiesgo);
}
