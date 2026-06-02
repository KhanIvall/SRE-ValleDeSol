package com.valledelsol.zonasriesgo.service;

import com.valledelsol.zonasriesgo.adapter.WeatherDataPort;
import com.valledelsol.zonasriesgo.adapter.WeatherSnapshot;
import com.valledelsol.zonasriesgo.common.BusinessRulesException;
import com.valledelsol.zonasriesgo.entities.NivelRiesgo;
import com.valledelsol.zonasriesgo.entities.ZonaRiesgo;
import com.valledelsol.zonasriesgo.repository.ZonaRiesgoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZonaRiesgoService {

    private final ZonaRiesgoRepository zonaRiesgoRepository;
    private final WeatherDataPort weatherDataPort;

    public ZonaRiesgoService(ZonaRiesgoRepository zonaRiesgoRepository, WeatherDataPort weatherDataPort) {
        this.zonaRiesgoRepository = zonaRiesgoRepository;
        this.weatherDataPort = weatherDataPort;
    }

    public List<ZonaRiesgo> listar() {
        return zonaRiesgoRepository.findAll();
    }

    public Optional<ZonaRiesgo> obtenerPorId(Long id) {
        return zonaRiesgoRepository.findById(id);
    }

    public Optional<ZonaRiesgo> obtenerPorCoordenadas(double latitud, double longitud) {
        return zonaRiesgoRepository.findAll().stream()
                .filter(z -> Math.abs(z.getLatitud() - latitud) < 0.01
                        && Math.abs(z.getLongitud() - longitud) < 0.01)
                .findFirst();
    }

    @Transactional
    public ZonaRiesgo crear(ZonaRiesgo zona) throws BusinessRulesException {
        validarZona(zona);
        enriquecerConClima(zona);
        return zonaRiesgoRepository.save(zona);
    }

    @Transactional
    public ZonaRiesgo actualizar(Long id, ZonaRiesgo datos) throws BusinessRulesException {
        ZonaRiesgo existente = zonaRiesgoRepository
                .findById(id)
                .orElseThrow(() -> new BusinessRulesException(
                        "ZON-404", HttpStatus.NOT_FOUND, "Zona de riesgo no encontrada"));

        if (datos.getNombre() != null) {
            existente.setNombre(datos.getNombre());
        }
        if (datos.getLatitud() != null) {
            existente.setLatitud(datos.getLatitud());
        }
        if (datos.getLongitud() != null) {
            existente.setLongitud(datos.getLongitud());
        }

        enriquecerConClima(existente);
        return zonaRiesgoRepository.save(existente);
    }

    @Transactional
    public ZonaRiesgo recalibrarRiesgo(Long id) throws BusinessRulesException {
        ZonaRiesgo zona = zonaRiesgoRepository
                .findById(id)
                .orElseThrow(() -> new BusinessRulesException(
                        "ZON-404", HttpStatus.NOT_FOUND, "Zona de riesgo no encontrada"));
        enriquecerConClima(zona);
        return zonaRiesgoRepository.save(zona);
    }

    @Transactional
    public void eliminar(Long id) {
        zonaRiesgoRepository.deleteById(id);
    }

    private void enriquecerConClima(ZonaRiesgo zona) {
        WeatherSnapshot clima = weatherDataPort.obtenerCondiciones(zona.getLatitud(), zona.getLongitud());
        zona.setTemperaturaCelsius(clima.getTemperaturaCelsius());
        zona.setHumedadPorcentaje(clima.getHumedadPorcentaje());
        zona.setCondicionClimatica(clima.getCondicion());
        zona.setNivelRiesgo(calcularNivel(clima));
    }

    private NivelRiesgo calcularNivel(WeatherSnapshot clima) {
        if (clima.getHumedadPorcentaje() > 70 && clima.getVelocidadVientoKmh() > 15) {
            return NivelRiesgo.CRITICO;
        }
        if (clima.getHumedadPorcentaje() > 55 || clima.getVelocidadVientoKmh() > 12) {
            return NivelRiesgo.ALTO;
        }
        if (clima.getTemperaturaCelsius() > 30) {
            return NivelRiesgo.MEDIO;
        }
        return NivelRiesgo.BAJO;
    }

    private void validarZona(ZonaRiesgo zona) throws BusinessRulesException {
        if (zona.getNombre() == null || zona.getNombre().isBlank()) {
            throw new BusinessRulesException("ZON-001", HttpStatus.BAD_REQUEST, "El nombre de la zona es obligatorio");
        }
        if (zona.getLatitud() == null || zona.getLongitud() == null) {
            throw new BusinessRulesException(
                    "ZON-002", HttpStatus.BAD_REQUEST, "Latitud y longitud son obligatorias");
        }
    }
}
