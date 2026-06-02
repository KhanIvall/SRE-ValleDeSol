package com.valledelsol.bff.dto;

import lombok.Data;

@Data
public class ZonaRiesgoDto {

    private Long id;
    private String nombre;
    private Double latitud;
    private Double longitud;
    private String nivelRiesgo;
    private String condicionClimatica;
    private Double temperaturaCelsius;
    private Double humedadPorcentaje;
}
