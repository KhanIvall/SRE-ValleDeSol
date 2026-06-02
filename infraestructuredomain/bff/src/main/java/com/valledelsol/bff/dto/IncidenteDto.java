package com.valledelsol.bff.dto;

import lombok.Data;

@Data
public class IncidenteDto {

    private Long id;
    private String tipo;
    private String estado;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String fechaReporte;
}
