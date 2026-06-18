package com.valledelsol.incidentes.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Prueba E2E: flujo completo del ciclo de vida de un incidente.
 * REPORTADO → EN_PROGRESO → CONTROLADO → CERRADO
 */
@SpringBootTest
class IncidenteE2ETest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private IncidenteRepository incidenteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        incidenteRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void flujoCompleto_cicloDeVidaIncidente() throws Exception {

        // 1. CREAR incidente (estado inicial: REPORTADO)
        Incidente nuevo = new Incidente();
        nuevo.setTipo("INCENDIO_FORESTAL");
        nuevo.setDescripcion("Foco activo en Parque Nacional La Campana");
        nuevo.setLatitud(-32.95);
        nuevo.setLongitud(-71.08);

        String cuerpoCreacion = mockMvc.perform(post("/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("REPORTADO")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(cuerpoCreacion, Incidente.class).getId();

        // 2. CAMBIAR a EN_PROGRESO (requiere coordenadas — ya las tiene)
        mockMvc.perform(put("/incidentes/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "EN_PROGRESO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_PROGRESO")));

        // 3. CAMBIAR a CONTROLADO
        mockMvc.perform(put("/incidentes/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "CONTROLADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CONTROLADO")));

        // 4. CAMBIAR a CERRADO
        mockMvc.perform(put("/incidentes/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "CERRADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CERRADO")));

        // 5. VERIFICAR estado final en base de datos
        mockMvc.perform(get("/incidentes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CERRADO")))
                .andExpect(jsonPath("$.tipo", is("INCENDIO_FORESTAL")));
    }

    @Test
    void flujoInvalido_controlado_sinPasarPorEnProgreso_retorna412() throws Exception {

        Incidente nuevo = new Incidente();
        nuevo.setTipo("DERRUMBE");
        nuevo.setLatitud(-33.4);
        nuevo.setLongitud(-70.6);

        String cuerpo = mockMvc.perform(post("/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(cuerpo, Incidente.class).getId();

        // Intentar saltar directamente a CONTROLADO desde REPORTADO → debe fallar
        mockMvc.perform(put("/incidentes/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "CONTROLADO"))))
                .andExpect(status().isPreconditionFailed());
    }
}
