package com.valledelsol.zonasriesgo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valledelsol.zonasriesgo.entities.NivelRiesgo;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ZonaRiesgoIntegrationTest {

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
    void postZonaRiesgo_valida_retorna201ConNivelCalculado() throws Exception {
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setNombre("Cerro Manquehue");
        zona.setLatitud(-33.38);
        zona.setLongitud(-70.57);

        mockMvc.perform(post("/zonas-riesgo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zona)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Cerro Manquehue")))
                .andExpect(jsonPath("$.nivelRiesgo", notNullValue()))
                .andExpect(jsonPath("$.condicionClimatica", notNullValue()))
                .andExpect(jsonPath("$.temperaturaCelsius", notNullValue()));
    }

    @Test
    void postZonaRiesgo_sinNombre_retorna400() throws Exception {
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setLatitud(-33.4);
        zona.setLongitud(-70.6);

        mockMvc.perform(post("/zonas-riesgo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zona)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postZonaRiesgo_sinCoordenadas_retorna400() throws Exception {
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setNombre("Zona sin coordenadas");

        mockMvc.perform(post("/zonas-riesgo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zona)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getZonaPorCoordenadas_encuentraZonaExistente() throws Exception {
        ZonaRiesgo zona = crearZonaEnBD("Valle Central", -33.45, -70.66);

        // Buscar con coordenadas dentro del margen de 0.01
        mockMvc.perform(get("/zonas-riesgo/coordenadas")
                        .param("latitud", "-33.45")
                        .param("longitud", "-70.66"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Valle Central")));
    }

    @Test
    void getZonaPorCoordenadas_fueraDeRango_retorna404() throws Exception {
        crearZonaEnBD("Valle Central", -33.45, -70.66);

        // Buscar con coordenadas lejos (fuera del margen 0.01)
        mockMvc.perform(get("/zonas-riesgo/coordenadas")
                        .param("latitud", "-35.00")
                        .param("longitud", "-72.00"))
                .andExpect(status().isNotFound());
    }

    @Test
    void recalibrarRiesgo_actualizaCondicionesClimaticas() throws Exception {
        ZonaRiesgo zona = crearZonaEnBD("Zona Recalibracion", -33.50, -70.70);

        String respuesta = mockMvc.perform(put("/zonas-riesgo/{id}/recalibrar", zona.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelRiesgo", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        ZonaRiesgo recalibrada = objectMapper.readValue(respuesta, ZonaRiesgo.class);
        assertNotNull(recalibrada.getNivelRiesgo());
        assertNotNull(recalibrada.getCondicionClimatica());
    }

    @Test
    void getZona_existente_retorna200() throws Exception {
        ZonaRiesgo zona = crearZonaEnBD("Zona Test", -33.40, -70.60);

        mockMvc.perform(get("/zonas-riesgo/{id}", zona.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Zona Test")));
    }

    private ZonaRiesgo crearZonaEnBD(String nombre, double latitud, double longitud) throws Exception {
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setNombre(nombre);
        zona.setLatitud(latitud);
        zona.setLongitud(longitud);

        String resp = mockMvc.perform(post("/zonas-riesgo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zona)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(resp, ZonaRiesgo.class);
    }
}
