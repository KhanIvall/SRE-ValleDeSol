package com.valledelsol.zonasriesgo.adapter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FakeWeatherAdapterTest {

    private final FakeWeatherAdapter adapter = new FakeWeatherAdapter();

    @Test
    void obtenerCondiciones_retornaSnapshotValido() {
        WeatherSnapshot snapshot = adapter.obtenerCondiciones(-33.45, -70.66);

        assertNotNull(snapshot.getCondicion());
        assertTrue(snapshot.getTemperaturaCelsius() >= 18);
        assertTrue(snapshot.getHumedadPorcentaje() >= 40);
    }
}
