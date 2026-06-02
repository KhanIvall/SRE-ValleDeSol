package com.valledelsol.bff.dto;

import lombok.Data;

@Data
public class RecursoDto {

    private Long id;
    private String nombre;
    private String tipo;
    private String estado;
    private String identificador;
    private Long incidenteAsignadoId;
}
