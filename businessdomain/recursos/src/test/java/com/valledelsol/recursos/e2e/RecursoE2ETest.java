package com.valledelsol.recursos.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valledelsol.recursos.entities.Recurso;
import com.valledelsol.recursos.entities.TipoRecurso;
import com.valledelsol.recursos.repository.RecursoRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba E2E: ciclo completo de un recurso de emergencia.
 * Crear → Verificar disponible → Asignar a incidente → Verificar desplegado → Liberar → Verificar disponible
 */
@SpringBootTest
class RecursoE2ETest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        recursoRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void flujoCompleto_recurso_asignarYLiberar() throws Exception {

        // 1. CREAR recurso (BRIGADA)
        Recurso nuevo = new Recurso();
        nuevo.setNombre("Brigada Cordillera Sur");
        nuevo.setTipo(TipoRecurso.BRIGADA);

        String cuerpo = mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("DISPONIBLE")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(cuerpo, Recurso.class).getId();

        // 2. VERIFICAR aparece en listado de disponibles
        mockMvc.perform(get("/recursos/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 3. ASIGNAR al incidente 5
        mockMvc.perform(put("/recursos/{id}/asignar", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("incidenteId", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("DESPLEGADO")))
                .andExpect(jsonPath("$.incidenteAsignadoId", is(5)));

        // 4. YA NO aparece en disponibles
        mockMvc.perform(get("/recursos/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 5. APARECE en recursos del incidente 5
        mockMvc.perform(get("/recursos/incidente/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Brigada Cordillera Sur")));

        // 6. LIBERAR recurso
        mockMvc.perform(put("/recursos/{id}/liberar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("DISPONIBLE")))
                .andExpect(jsonPath("$.incidenteAsignadoId", nullValue()));

        // 7. VUELVE a aparecer en disponibles
        mockMvc.perform(get("/recursos/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void flujoCompleto_vehiculo_conPatente() throws Exception {

        // 1. CREAR vehiculo con patente
        Recurso vehiculo = new Recurso();
        vehiculo.setNombre("Camion Cisterna 01");
        vehiculo.setTipo(TipoRecurso.VEHICULO);
        vehiculo.setIdentificador("ABCD-12");

        String cuerpo = mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehiculo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificador", is("ABCD-12")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(cuerpo, Recurso.class).getId();

        // 2. ASIGNAR a incidente 3
        mockMvc.perform(put("/recursos/{id}/asignar", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("incidenteId", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("DESPLEGADO")));

        // 3. INTENTAR asignar de nuevo (ya desplegado) → 412
        mockMvc.perform(put("/recursos/{id}/asignar", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("incidenteId", 9))))
                .andExpect(status().isPreconditionFailed());
    }
}
