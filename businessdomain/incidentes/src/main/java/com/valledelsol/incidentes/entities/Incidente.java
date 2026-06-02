package com.valledelsol.incidentes.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Data;

@Data
@Entity
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoIncidente estado = EstadoIncidente.REPORTADO;

    private String descripcion;

    private Double latitud;

    private Double longitud;

    @Column(nullable = false)
    private Instant fechaReporte = Instant.now();
}
