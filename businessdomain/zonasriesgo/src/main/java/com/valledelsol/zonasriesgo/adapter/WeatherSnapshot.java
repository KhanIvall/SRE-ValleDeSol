package com.valledelsol.zonasriesgo.adapter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherSnapshot {

    private double temperaturaCelsius;
    private double humedadPorcentaje;
    private double velocidadVientoKmh;
    private String condicion;
}
