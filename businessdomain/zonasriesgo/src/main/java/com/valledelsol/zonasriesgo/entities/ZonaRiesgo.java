package com.valledelsol.zonasriesgo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ZonaRiesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRiesgo nivelRiesgo = NivelRiesgo.MEDIO;

    private String condicionClimatica;

    private Double temperaturaCelsius;

    private Double humedadPorcentaje;
}
