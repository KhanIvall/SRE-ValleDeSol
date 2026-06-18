package com.valledelsol.incidentes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valledelsol.incidentes.entities.EstadoIncidente;
import com.valledelsol.incidentes.entities.Incidente;
import com.valledelsol.incidentes.repository.IncidenteRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
class IncidenteIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private IncidenteRepository incidenteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiarBD() {
        incidenteRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void postIncidente_datosValidos_retorna201YPersiste() throws Exception {
        Incidente incidente = new Incidente();
        incidente.setTipo("INCENDIO_FORESTAL");
        incidente.setDescripcion("Foco de incendio en cerro Manquehue");
        incidente.setLatitud(-33.45);
        incidente.setLongitud(-70.66);

        mockMvc.perform(post("/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("REPORTADO")))
                .andExpect(jsonPath("$.tipo", is("INCENDIO_FORESTAL")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getIncidente_existente_retorna200ConDatos() throws Exception {
        Incidente guardado = crearIncidenteEnBD("DERRUMBE", -33.40, -70.60);

        mockMvc.perform(get("/incidentes/{id}", guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo", is("DERRUMBE")))
                .andExpect(jsonPath("$.estado", is("REPORTADO")));
    }

    @Test
    void getIncidente_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/incidentes/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cambiarEstado_aEnProgreso_conCoordenadas_retorna200() throws Exception {
        Incidente guardado = crearIncidenteEnBD("INUNDACION", -33.50, -70.70);

        mockMvc.perform(put("/incidentes/{id}/estado", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "EN_PROGRESO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_PROGRESO")));
    }

    @Test
    void cambiarEstado_aEnProgreso_sinCoordenadas_retorna412() throws Exception {
        Incidente incidente = new Incidente();
        incidente.setTipo("SISMO");
        incidente.setDescripcion("Movimiento telúrico zona centro");
        mockMvc.perform(post("/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidente)))
                .andExpect(status().isCreated());

        Long id = incidenteRepository.findAll().get(0).getId();

        mockMvc.perform(put("/incidentes/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "EN_PROGRESO"))))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void listarIncidentes_retornaListaCompleta() throws Exception {
        crearIncidenteEnBD("INCENDIO", -33.4, -70.6);
        crearIncidenteEnBD("DERRUMBE", -33.5, -70.7);

        mockMvc.perform(get("/incidentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));
    }

    private Incidente crearIncidenteEnBD(String tipo, double lat, double lon) throws Exception {
        Incidente incidente = new Incidente();
        incidente.setTipo(tipo);
        incidente.setLatitud(lat);
        incidente.setLongitud(lon);

        String respuesta = mockMvc.perform(post("/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidente)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(respuesta, Incidente.class);
    }
}
