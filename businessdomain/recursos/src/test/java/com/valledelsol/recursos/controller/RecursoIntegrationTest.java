package com.valledelsol.recursos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valledelsol.recursos.entities.EstadoRecurso;
import com.valledelsol.recursos.entities.Recurso;
import com.valledelsol.recursos.entities.TipoRecurso;
import com.valledelsol.recursos.repository.RecursoRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RecursoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        recursoRepository.deleteAll();
    }

    @Test
    void postRecurso_brigadaValida_retorna201() throws Exception {
        Recurso recurso = new Recurso();
        recurso.setNombre("Brigada Andes Norte");
        recurso.setTipo(TipoRecurso.BRIGADA);

        mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recurso)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Brigada Andes Norte")))
                .andExpect(jsonPath("$.estado", is("DISPONIBLE")))
                .andExpect(jsonPath("$.tipo", is("BRIGADA")));
    }

    @Test
    void postRecurso_vehiculoSinPatente_retorna400() throws Exception {
        Recurso recurso = new Recurso();
        recurso.setNombre("Camion Cisterna");
        recurso.setTipo(TipoRecurso.VEHICULO);
        // sin identificador (patente)

        mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recurso)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postRecurso_sinNombre_retorna400() throws Exception {
        Recurso recurso = new Recurso();
        recurso.setTipo(TipoRecurso.EQUIPO);

        mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recurso)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDisponibles_filtraSoloDisponibles() throws Exception {
        crearRecursoEnBD("Brigada Sur", TipoRecurso.BRIGADA, null);
        Recurso desplegado = crearRecursoEnBD("Brigada Norte", TipoRecurso.BRIGADA, null);

        // Asignar uno para que quede DESPLEGADO
        mockMvc.perform(put("/recursos/{id}/asignar", desplegado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("incidenteId", 1))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/recursos/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado", is("DISPONIBLE")));
    }

    @Test
    void asignarRecurso_disponible_cambiaADesplegado() throws Exception {
        Recurso recurso = crearRecursoEnBD("Equipo Rescate", TipoRecurso.EQUIPO, null);

        mockMvc.perform(put("/recursos/{id}/asignar", recurso.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("incidenteId", 42))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("DESPLEGADO")))
                .andExpect(jsonPath("$.incidenteAsignadoId", is(42)));
    }

    @Test
    void liberarRecurso_desplegado_vuelveADisponible() throws Exception {
        Recurso recurso = crearRecursoEnBD("Equipo Rescate 2", TipoRecurso.EQUIPO, null);

        mockMvc.perform(put("/recursos/{id}/asignar", recurso.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("incidenteId", 7))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/recursos/{id}/liberar", recurso.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("DISPONIBLE")))
                .andExpect(jsonPath("$.incidenteAsignadoId").doesNotExist());
    }

    @Test
    void getRecursosPorIncidente_retornaSoloLosAsignados() throws Exception {
        Recurso r1 = crearRecursoEnBD("Brigada A", TipoRecurso.BRIGADA, null);
        Recurso r2 = crearRecursoEnBD("Brigada B", TipoRecurso.BRIGADA, null);
        crearRecursoEnBD("Brigada C", TipoRecurso.BRIGADA, null);

        mockMvc.perform(put("/recursos/{id}/asignar", r1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("incidenteId", 10))))
                .andExpect(status().isOk());
        mockMvc.perform(put("/recursos/{id}/asignar", r2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("incidenteId", 10))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/recursos/incidente/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    private Recurso crearRecursoEnBD(String nombre, TipoRecurso tipo, String identificador) throws Exception {
        Recurso recurso = new Recurso();
        recurso.setNombre(nombre);
        recurso.setTipo(tipo);
        recurso.setIdentificador(identificador);

        String resp = mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(resp, Recurso.class);
    }
}
