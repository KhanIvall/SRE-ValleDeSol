package com.valledelsol.bff.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class EmergenciaResumenDto {

    private Long incidenteId;
    private IncidenteDto incidente;
    private List<RecursoDto> recursosAsignados = new ArrayList<>();
    private ZonaRiesgoDto zonaRiesgo;
    private boolean datosContingencia;
    private String mensajeContingencia;
}
