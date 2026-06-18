package com.valledelsol.zonasriesgo.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valledelsol.zonasriesgo.entities.ZonaRiesgo;
import com.valledelsol.zonasriesgo.repository.ZonaRiesgoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba E2E: ciclo completo de monitoreo de una zona de riesgo.
 * Crear zona → Consultar por coordenadas → Recalibrar → Verificar nivel actualizado
 */
@SpringBootTest
class ZonaRiesgoE2ETest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ZonaRiesgoRepository zonaRiesgoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        zonaRiesgoRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void flujoCompleto_monitoreoDeZonaRiesgo() throws Exception {

        // 1. CREAR zona con coordenadas conocidas
        ZonaRiesgo nueva = new ZonaRiesgo();
        nueva.setNombre("Sector Cordillera Poniente");
        nueva.setLatitud(-33.55);
        nueva.setLongitud(-70.75);

        String cuerpoCreacion = mockMvc.perform(post("/zonas-riesgo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Sector Cordillera Poniente")))
                .andExpect(jsonPath("$.nivelRiesgo", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        ZonaRiesgo creada = objectMapper.readValue(cuerpoCreacion, ZonaRiesgo.class);
        Long id = creada.getId();
        String nivelInicial = creada.getNivelRiesgo().name();

        // 2. BUSCAR por coordenadas exactas
        mockMvc.perform(get("/zonas-riesgo/coordenadas")
                        .param("latitud", "-33.55")
                        .param("longitud", "-70.75"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Sector Cordillera Poniente")))
                .andExpect(jsonPath("$.id", is(id.intValue())));

        // 3. BUSCAR por coordenadas aproximadas (dentro del margen 0.01)
        mockMvc.perform(get("/zonas-riesgo/coordenadas")
                        .param("latitud", "-33.555")
                        .param("longitud", "-70.748"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Sector Cordillera Poniente")));

        // 4. OBTENER por ID
        mockMvc.perform(get("/zonas-riesgo/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperaturaCelsius", notNullValue()))
                .andExpect(jsonPath("$.humedadPorcentaje", notNullValue()));

        // 5. RECALIBRAR riesgo (FakeWeatherAdapter es determinista, nivel debe ser consistente)
        String cuerpoRecalibrado = mockMvc.perform(put("/zonas-riesgo/{id}/recalibrar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelRiesgo", notNullValue()))
                .andExpect(jsonPath("$.condicionClimatica", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        ZonaRiesgo recalibrada = objectMapper.readValue(cuerpoRecalibrado, ZonaRiesgo.class);

        // FakeWeatherAdapter es determinista → nivel debe ser igual al inicial
        org.junit.jupiter.api.Assertions.assertEquals(
                nivelInicial, recalibrada.getNivelRiesgo().name(),
                "FakeWeatherAdapter determinista: nivel no debe cambiar en recalibración");

        // 6. LISTAR y verificar que la zona sigue existiendo
        mockMvc.perform(get("/zonas-riesgo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void flujoActualizacion_datosZona_recalculaNivel() throws Exception {

        // 1. Crear zona inicial
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setNombre("Zona Monitoreo");
        zona.setLatitud(-33.40);
        zona.setLongitud(-70.60);

        String cuerpo = mockMvc.perform(post("/zonas-riesgo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zona)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(cuerpo, ZonaRiesgo.class).getId();

        // 2. ACTUALIZAR coordenadas (mueve la zona a otro sector)
        ZonaRiesgo actualizacion = new ZonaRiesgo();
        actualizacion.setLatitud(-32.00);
        actualizacion.setLongitud(-71.50);

        mockMvc.perform(put("/zonas-riesgo/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelRiesgo", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Zona Monitoreo")));

        // 3. Verificar que el GET por coordenadas originales ya no encuentra la zona
        mockMvc.perform(get("/zonas-riesgo/coordenadas")
                        .param("latitud", "-33.40")
                        .param("longitud", "-70.60"))
                .andExpect(status().isNotFound());

        // 4. Verificar que la zona aparece en las nuevas coordenadas
        mockMvc.perform(get("/zonas-riesgo/coordenadas")
                        .param("latitud", "-32.00")
                        .param("longitud", "-71.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())));
    }
}
