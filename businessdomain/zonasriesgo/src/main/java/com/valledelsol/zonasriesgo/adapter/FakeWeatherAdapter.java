package com.valledelsol.zonasriesgo.adapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adaptador de simulacion para validar logica sin API externa.
 */
@Component
@ConditionalOnProperty(name = "sre.weather.adapter", havingValue = "fake", matchIfMissing = true)
public class FakeWeatherAdapter implements WeatherDataPort {

    @Override
    public WeatherSnapshot obtenerCondiciones(double latitud, double longitud) {
        double factor = Math.abs(latitud) + Math.abs(longitud);
        double temperatura = 18 + (factor % 12);
        double humedad = 40 + (factor % 35);
        double viento = 5 + (factor % 20);
        String condicion = humedad > 60 ? "SECO_VIENTOSO" : "ESTABLE";
        return new WeatherSnapshot(temperatura, humedad, viento, condicion);
    }
}
